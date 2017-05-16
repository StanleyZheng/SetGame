import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.sql.*;

//5-03-2017 21:48
public class GameServer {
  
    final private ServerSocket mainServerSocket;
    final public static int MAX_CLIENTS = 200;
    final public static int MAX_PLAYERS_PER = 4;        // 4 players per game room
    final public static int NUM_GAMEROOM_VARIABLES = 6; // game name, game password, numPlayers, TopOfDeck, numCardsOnField, submitLock
    final public static int NUM_PLAYER_VARIABLES = 4;
    final private SubServer[] arrayClientConnections = new SubServer[ MAX_CLIENTS ];
    final private Object[][] CurrentGames = new Object[MAX_CLIENTS][NUM_GAMEROOM_VARIABLES + NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER + 81];
    private PrintWriter[] writers = new PrintWriter[MAX_CLIENTS];
    private int clientsOnServer = 0;
    private int FillingRoomNum = 0;
    
    public GameServer( int port ) throws IOException {
        this.mainServerSocket = new ServerSocket( port );
    }
    
    public static void main(String[] args) throws Exception {
        GameServer game = new GameServer(9898);
        System.out.println("Game Server is running...");
    
        try {
            while (true) {
                Socket connection = game.mainServerSocket.accept();
                System.out.println("Received Connection!");
                game.startServer( connection );
            }
        } finally {
            game.mainServerSocket.close();
        }
    }// end main()

    // Check submitted Login Info
    public void startServer( Socket connection ) throws IOException {
    	clientsOnServer++;
        assignConnectionToSubServer( connection );
    }// end startServer()
    
    
    // helper function to add Connection to arrayClientConnections
    public void assignConnectionToSubServer( Socket connection ) {
        for ( int i = 0 ; i < MAX_CLIENTS ; i++ ) {
            if ( this.arrayClientConnections[ i ] == null ) {
                this.arrayClientConnections[ i ] = new SubServer( connection , i );
                break;
            }
        }
    }
    
    
    //SubServer Class, SubServer == Client Sockets
    //
    protected class SubServer extends Thread {
        final private int user_id;
        final private Socket user_connection;
        BufferedReader in;
        PrintWriter out;
    
        public SubServer( Socket connection , int id ) {
            this.user_id = id;
            this.user_connection = connection;
            start();
        }
    
        @Override
        public void run(){  
            try {
                in = new BufferedReader( new InputStreamReader(this.user_connection.getInputStream()) );
                GameServer.this.writers[this.user_id] = new PrintWriter(this.user_connection.getOutputStream(), true);
                out = writers[this.user_id];
                while(!this.interrupted()){
                    process(in.readLine());
                }
            } catch (Exception ex){ }
        }
      
        // process ALL commands received from Clients
        public void process( String message ) throws IOException{
            //***** CHECKLOGIN *****//
            if (message.equals("CHECKLOGIN")){
                String usertxt = in.readLine();
                String passtxt = in.readLine();
                if (GameServer.this.clientsOnServer >= MAX_CLIENTS){
                    out.println("TOOMANY");
                    return;
                }

                // validate username/password combination by checking database
                Connection conn = null;
                Statement stmt = null;
                ResultSet rset = null;
                try{
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/SetGameDB", "root", "qweqwe");
                    stmt = conn.createStatement();
                    String sqlStr = "SELECT * FROM Users WHERE username = '" + usertxt +"'";
                    rset = stmt.executeQuery(sqlStr);
                    if (rset.next()){
                        if ( BCrypt.checkpw(passtxt, rset.getString("password")) ){
                            out.println("VALID!");
                            return;
                        } else {
	                        out.println("BADPASSWORD");
	                        return;
                        }
                    } else {
                        out.println("NOUSER");
                        return;
                    }
                } catch (Exception ex){ System.out.println("Failed to connect to Database"); }
            }

            //***** CREATE *****//
            if (message.equals("CREATE")){
                String roomName = in.readLine();
                String roomPassword = in.readLine();

                // check if game with name already exists
                if (findGameRoomNum(roomName) != -1){
                    out.println("ROOMALREADYEXISTS");
                    return;
                }

                while( GameServer.this.FillingRoomNum < MAX_CLIENTS ) {
                    if (GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][0] == null){
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][0] = roomName;     //room Name
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][1] = roomPassword;   //room Password
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][2] = 0;        //number of Players
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][3] = NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER;
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][4] = 0;        //number of cards on field
                        GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][5] = new int[]{-1,-1,-1};        //submit lock

                        for(int i = 0; i < 81; i++){
                            GameServer.this.CurrentGames[GameServer.this.FillingRoomNum][NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER+i] = i;
                        }
                        shuffleCards(GameServer.this.FillingRoomNum);
                        out.println("ROOMCREATED");
                        break;
                    }
                    GameServer.this.FillingRoomNum++;
                    if (GameServer.this.FillingRoomNum >= MAX_CLIENTS){
                        GameServer.this.FillingRoomNum = 0;
                    }
                }
            }
        
            //***** JOIN *****//
            if (message.equals("JOIN")){
                String gameRoomName = in.readLine();
                String gamePassSubmitted = in.readLine();
                String gameUsername = in.readLine();
                boolean joinSuccess = false;

                // find gameRoomNum
                int gameRoomNum = findGameRoomNum(gameRoomName);
                if (gameRoomNum == -1){
                    out.println("BADGAMENAME");
                    return;
                }

                // check GameRoom password
                if ( !(gamePassSubmitted.equals(GameServer.this.CurrentGames[gameRoomNum][1])) ) {
                    out.println("BADGAMEPASS");
                    return;
                }

                // check if GameRoom has already started
                if ( (int)GameServer.this.CurrentGames[gameRoomNum][3] != NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER) {
                    out.println("GAMESTARTEDALREADY");
                    return;
                }

                //check if player is already in this game
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null && GameServer.this.CurrentGames[gameRoomNum][i+MAX_PLAYERS_PER].equals(gameUsername)){
                        out.println("USERALREADYINSIDE");
                        return;
                    }
                }

                // add player into slot if there are available spots
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] == null){
                        GameServer.this.CurrentGames[gameRoomNum][i] = this.user_id;
                        GameServer.this.CurrentGames[gameRoomNum][i+MAX_PLAYERS_PER] = gameUsername;
                        GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER] = 0;
                        GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 12345;
                        joinSuccess = true;
                        break;
                    }
                }

                if (joinSuccess == false){
                    out.println("NOMORESPACE");
                } else {
                    GameServer.this.CurrentGames[gameRoomNum][2] = (int) GameServer.this.CurrentGames[gameRoomNum][2] + 1;
                    out.println("JOINSUCCESS");
                    updatePlayersIn(gameRoomNum);
                }
                return;
            }
        
            //***** STARTGAME *****//
            if (message.equals("STARTGAME")){
                String gameRoomName = in.readLine();
                String userName = in.readLine();
                boolean allThinkNoSet = true;
                int gameRoomNum = findGameRoomNum(gameRoomName);
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null && 
                        GameServer.this.CurrentGames[gameRoomNum][i+MAX_PLAYERS_PER].equals(userName)){
                        GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 6789;
                        break;
                    }
                }
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                        if ((int)GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] != 6789){
                            allThinkNoSet = false;
                            break;
                        }
                    }
                }
                
                if (allThinkNoSet){
	                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
	                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
	                    	GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 12345;
	                        GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("GAMESTARTING");
	                        for(int j = 0; j < 12; j++){
	                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(
	                            GameServer.this.CurrentGames[gameRoomNum][(int)GameServer.this.CurrentGames[gameRoomNum][3] + j]);
	                        }
	                    }
	                }
	                GameServer.this.CurrentGames[gameRoomNum][3] = (int) GameServer.this.CurrentGames[gameRoomNum][3] + 12;
	                GameServer.this.CurrentGames[gameRoomNum][4] = 12;
                }

                updatePlayersIn(gameRoomNum);
            }
        
            //***** SUBMITTINGSET *****//
            if (message.equals("SUBMITTINGSET")){
                String gameRoomName = in.readLine();
                int card1 = Integer.parseInt(in.readLine());
                int card2 = Integer.parseInt(in.readLine());
                int card3 = Integer.parseInt(in.readLine());
                String user = in.readLine();
                int gameRoomNum = findGameRoomNum(gameRoomName);
                System.out.println("Set was submitted by: " + user);
                
                if (((int[])GameServer.this.CurrentGames[gameRoomNum][5])[0] == card1 ||
                		((int[])GameServer.this.CurrentGames[gameRoomNum][5])[0] == card2 ||
                				((int[])GameServer.this.CurrentGames[gameRoomNum][5])[0] == card3){
                	System.out.println("samecard was submitted at 0");
                }
                else if (((int[])GameServer.this.CurrentGames[gameRoomNum][5])[1] == card1 ||
                		((int[])GameServer.this.CurrentGames[gameRoomNum][5])[1] == card2 ||
                				((int[])GameServer.this.CurrentGames[gameRoomNum][5])[1] == card3){
                	System.out.println("samecard was submitted at 1");
                }
                else if (((int[])GameServer.this.CurrentGames[gameRoomNum][5])[2] == card1 ||
                		((int[])GameServer.this.CurrentGames[gameRoomNum][5])[2] == card2 ||
                				((int[])GameServer.this.CurrentGames[gameRoomNum][5])[2] == card3){
                	System.out.println("samecard was submitted at 2");
                }
                
                else if (checkSet(card1, card2, card3)){
                	((int[])GameServer.this.CurrentGames[gameRoomNum][5])[0] = card1;
                	((int[])GameServer.this.CurrentGames[gameRoomNum][5])[1] = card2;
                	((int[])GameServer.this.CurrentGames[gameRoomNum][5])[2] = card3;
                	System.out.println("inside Set was submitted by: " + user);
                	System.out.println(((int[])GameServer.this.CurrentGames[gameRoomNum][5])[0]);
                	System.out.println(((int[])GameServer.this.CurrentGames[gameRoomNum][5])[1]);
                	System.out.println(((int[])GameServer.this.CurrentGames[gameRoomNum][5])[2]);
                    for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                        if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                            GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 12345; 
                        }
                    }
                    addScoreTo(user, gameRoomNum);
                    updatePlayersIn(gameRoomNum);

                    //tell clients to remove cards from their fields
                    for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                        if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("SETWASSUBMITTED");
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(user);
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(card1);
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(card2);
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(card3);
                        }
                    }

                    if ((int)GameServer.this.CurrentGames[gameRoomNum][3] < NUM_GAMEROOM_VARIABLES + NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER + 80){
                        //if there were only 12 cards on the field, add 3 more cards back onto table, else subtract 3 from numOnField
                        if ((int)GameServer.this.CurrentGames[gameRoomNum][4] == 12){
                            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                                if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                                    GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("ADDCARDS");
                                    for(int j = 0; j < 3; j++){
                                        GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(GameServer.this.CurrentGames[gameRoomNum][(int)GameServer.this.CurrentGames[gameRoomNum][3] + j]);
                                    }
                                }
                            }
                            GameServer.this.CurrentGames[gameRoomNum][3] = (int) GameServer.this.CurrentGames[gameRoomNum][3] + 3;
                        } else {
                            GameServer.this.CurrentGames[gameRoomNum][4] = (int) GameServer.this.CurrentGames[gameRoomNum][4] - 3;
                        }
                    } else {
                        GameServer.this.CurrentGames[gameRoomNum][4] = (int) GameServer.this.CurrentGames[gameRoomNum][4] - 3;
                    }

                    if ((int)GameServer.this.CurrentGames[gameRoomNum][4] == 0){
                        sendGameResults(gameRoomNum);
                    }
                }
                else {
                	System.out.println("inside Set was invalid: " + user);
                    out.println("NOTAVALIDSET");
                }
                System.out.println("---outside---");
            }
        
            //***** NOMORESET *****//
            if (message.equals("NOMORESET")){
                String gameRoomName = in.readLine();
                String userName = in.readLine();
                boolean allThinkNoSet = true;
                int gameRoomNum = findGameRoomNum(gameRoomName);
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null && 
                        GameServer.this.CurrentGames[gameRoomNum][i+MAX_PLAYERS_PER].equals(userName)){
                        GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 6789;
                        break;
                    }
                }
                for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                    if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                        if ((int)GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] != 6789){
                            allThinkNoSet = false;
                            break;
                        }
                    }
                }


                if (allThinkNoSet){
                    if ((int)GameServer.this.CurrentGames[gameRoomNum][3] < NUM_GAMEROOM_VARIABLES + NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER + 80){  
                        if((int)GameServer.this.CurrentGames[gameRoomNum][4] < 21){
                            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                                if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                                    GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 12345;
                                    GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("ADDCARDS");
                                    for(int j = 0; j < 3; j++){
                                        GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println(
                                        GameServer.this.CurrentGames[gameRoomNum][(int)GameServer.this.CurrentGames[gameRoomNum][3] + j]);
                                    }
                                }
                            }
                            GameServer.this.CurrentGames[gameRoomNum][3] = (int) GameServer.this.CurrentGames[gameRoomNum][3] + 3;
                            GameServer.this.CurrentGames[gameRoomNum][4] = (int) GameServer.this.CurrentGames[gameRoomNum][4] + 3;
                        }
                    } else {
                    	for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                            if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                                GameServer.this.CurrentGames[gameRoomNum][i+3*MAX_PLAYERS_PER] = 12345;
                            }
                    	}
                        sendGameResults(gameRoomNum);
                    }
                }// end if (allThinkNoSet)
                updatePlayersIn(gameRoomNum);
            }
      
            //***** LEAVEGAME *****//
            if (message.equals("LEAVEGAME")){
                String userName = in.readLine();
                String gameName = in.readLine();
                removePlayerFromGame(userName, gameName);
                out.println("LEAVESUCCESSFUL");
            }
        
            //***** CLOSECONNECTION *****//
            if (message.equals("CLOSECONNECTION")){
                System.out.println("Closed connection to: " + this.user_id);
                this.close();
                return;
            } 
        }// process(message)
      
        public void addScoreTo(String user, int gameRoomNum){
            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                if (GameServer.this.CurrentGames[gameRoomNum][i] != null && 
                    GameServer.this.CurrentGames[gameRoomNum][i+MAX_PLAYERS_PER].equals(user)){
                    GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER] = (int)GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER] + 1;
                    break;
                }
            }
        }
      
        public void updatePlayersIn(int gameRoomNum){
            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                    GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("UPDATEPLAYERINFO");
                    for (int j = NUM_GAMEROOM_VARIABLES; j < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; j++){
                        if (GameServer.this.CurrentGames[gameRoomNum][j] != null){
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println( GameServer.this.CurrentGames[gameRoomNum][j+MAX_PLAYERS_PER]);
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println( GameServer.this.CurrentGames[gameRoomNum][j+2*MAX_PLAYERS_PER]);
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println( GameServer.this.CurrentGames[gameRoomNum][j+3*MAX_PLAYERS_PER]);
                        } else {
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("User N/A");
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("Score N/A");
                            GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("Think N/A");
                        }
                    }
                }
            }
        }
      
        public int findGameRoomNum(String GameName){
            int retVal = -1;
            for (int i = 0; i < MAX_CLIENTS; i++){
                if (GameServer.this.CurrentGames[i][0] != null && GameServer.this.CurrentGames[i][0].equals(GameName)){
                    retVal = i;
                    break;
                }
            }
            return retVal;
        }
      
        public void shuffleCards(int gameRoomNum){
            for(int i = 0; i < 81; i++){
                int a = (int) (Math.random()*81) + NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER;
                int b = (int) (Math.random()*81) + NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER;
                int temp = (int) GameServer.this.CurrentGames[gameRoomNum][a];
                GameServer.this.CurrentGames[gameRoomNum][a] = (int) GameServer.this.CurrentGames[gameRoomNum][b];
                GameServer.this.CurrentGames[gameRoomNum][b] = temp;
            }
        }
      
        public void printGameRoomInfo(int gameRoomNum){
            for(int i = 0; i < NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER + 81; i++){
                System.out.println(gameRoomNum + " " + i + ": " + GameServer.this.CurrentGames[gameRoomNum][i]);
            }
        }     
      
        public boolean checkSet(int card1, int card2, int card3){
            boolean isSet = true;
            int modder = 81;
            for (int i = 0; i < 4; i++){
                int cardValue1 = (int) ((card1 % modder) / (modder / 3));
                int cardValue2 = (int) ((card2 % modder) / (modder / 3));
                int cardValue3 = (int) ((card3 % modder) / (modder / 3));
                int sum = cardValue1 + cardValue2 + cardValue3;
                if ((sum % 3) != 0){
                    isSet = false;
                    break;
                }
                modder = modder / 3;
            }
            return isSet;
        }
      
        public void removePlayerFromGame(String user, String game){
            int roomIndex = findGameRoomNum(game);
            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES+MAX_PLAYERS_PER; i++){
                if (GameServer.this.CurrentGames[roomIndex][i+MAX_PLAYERS_PER] != null && GameServer.this.CurrentGames[roomIndex][i+MAX_PLAYERS_PER].equals(user)){
                    GameServer.this.CurrentGames[roomIndex][i] = null;
                    GameServer.this.CurrentGames[roomIndex][i+MAX_PLAYERS_PER] = null;
                    GameServer.this.CurrentGames[roomIndex][i+2*MAX_PLAYERS_PER] = null;
                    GameServer.this.CurrentGames[roomIndex][i+3*MAX_PLAYERS_PER] = null; 
                    GameServer.this.CurrentGames[roomIndex][2] =  (int) GameServer.this.CurrentGames[roomIndex][2] - 1;
                    break;
                }
            }
            if ( (int) GameServer.this.CurrentGames[roomIndex][2] == 0 ){
                closeGameRoom(roomIndex);
            }
            else {
	            //check if all users have clicked noMoreSets when a player leaves
	            boolean allThinkNoSet = true;
	            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
	            	if (GameServer.this.CurrentGames[roomIndex][i] != null){
	            		if ((int)GameServer.this.CurrentGames[roomIndex][i+3*MAX_PLAYERS_PER] != 6789){
	            			allThinkNoSet = false;
	            			break;
	            		}
	            	}
	            }
	
	            if (allThinkNoSet){
	            	if ((int)GameServer.this.CurrentGames[roomIndex][3] == NUM_GAMEROOM_VARIABLES+NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER){
	            		System.out.println("inside add 12 cards");
	            		for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
		                    if (GameServer.this.CurrentGames[roomIndex][i] != null){
		                    	GameServer.this.CurrentGames[roomIndex][i+3*MAX_PLAYERS_PER] = 12345;
		                        GameServer.this.writers[(int) GameServer.this.CurrentGames[roomIndex][i]].println("GAMESTARTING");
		                        for(int j = 0; j < 12; j++){
		                            GameServer.this.writers[(int) GameServer.this.CurrentGames[roomIndex][i]].println(
		                            GameServer.this.CurrentGames[roomIndex][(int)GameServer.this.CurrentGames[roomIndex][3] + j]);
		                        }
		                    }
		                }
		                GameServer.this.CurrentGames[roomIndex][3] = (int) GameServer.this.CurrentGames[roomIndex][3] + 12;
		                GameServer.this.CurrentGames[roomIndex][4] = 12;
	            	}
	            	else if ((int)GameServer.this.CurrentGames[roomIndex][3] < NUM_GAMEROOM_VARIABLES + NUM_PLAYER_VARIABLES*MAX_PLAYERS_PER + 80){
	            		System.out.println("inside add 3 cards");
	            		if((int)GameServer.this.CurrentGames[roomIndex][4] < 21){
	            			for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
	            				if (GameServer.this.CurrentGames[roomIndex][i] != null){
	            					GameServer.this.CurrentGames[roomIndex][i+3*MAX_PLAYERS_PER] = 12345;
	            					GameServer.this.writers[(int) GameServer.this.CurrentGames[roomIndex][i]].println("ADDCARDS");
	            					for(int j = 0; j < 3; j++){
	            						GameServer.this.writers[(int) GameServer.this.CurrentGames[roomIndex][i]].println(
	            						GameServer.this.CurrentGames[roomIndex][(int)GameServer.this.CurrentGames[roomIndex][3] + j]);
	            					}
	            				}
	            			}
	            			GameServer.this.CurrentGames[roomIndex][3] = (int) GameServer.this.CurrentGames[roomIndex][3] + 3;
	            			GameServer.this.CurrentGames[roomIndex][4] = (int) GameServer.this.CurrentGames[roomIndex][4] + 3;
	            		}
	            	} else {
	            		sendGameResults(roomIndex);
	            	}
	            }// end if (allThinkNoSet)
            }
            
            updatePlayersIn(roomIndex);
            return;
        }

        public void sendGameResults(int gameRoomNum){
            int winScore = 0;
            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                    if ((int) GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER] > winScore)
                        winScore = (int) GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER];
                }   
            }

            for (int i = NUM_GAMEROOM_VARIABLES; i < NUM_GAMEROOM_VARIABLES + MAX_PLAYERS_PER; i++){
                if (GameServer.this.CurrentGames[gameRoomNum][i] != null){
                    GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("GAMEOVER");
                    if ((int)GameServer.this.CurrentGames[gameRoomNum][i+2*MAX_PLAYERS_PER] != winScore){
                        GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("YOULOSE");
                    } else{
                        GameServer.this.writers[(int) GameServer.this.CurrentGames[gameRoomNum][i]].println("YOUWON");
                    }
                }
            }
        }
  
        public void closeGameRoom(int gameRoomNum){
            GameServer.this.CurrentGames[gameRoomNum][0] = null;
            GameServer.this.CurrentGames[gameRoomNum][1] = null;
        }
      
        // terminates the connection with this client (i.e. stops serving him)
        public void close() {
            try { 
                this.user_connection.close(); 
                GameServer.this.arrayClientConnections[ this.user_id ] = null;
                clientsOnServer--;
                System.out.println("Client On Server: " + clientsOnServer);
            } catch ( IOException e ) { /*ignore*/ }
        }
      
    }//end SubServer Class
    
}//end GameServer Class
