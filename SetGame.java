import java.awt.*;
import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.Timer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

//5-03-2017 9:51
public class SetGame extends JFrame{
	
	private static String HOST_IP = "199.98.20.119";
//	private static String HOST_IP = "127.0.0.1";
	private static int PORT = 9898;
	
	private BufferedReader in;
    private PrintWriter out;
    private boolean isValidated = false;
    private boolean serverConnStatus = false;
    private String userStr = "";
    private String inGameRoom = "";
    private boolean isInGame = false;
    private String roomName = "";
    private boolean sentNoSet = false;
    private boolean canClick = true;

    private int[] cardOrder = new int[21];
    private int[] cardValuesArray = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
    private JButton[] cardButtonsArray = new JButton[21];
    private int[] setBuffer = new int[]{0,-1,-1,-1};		//first value holds number of cards chosen - max 3 cards;
    private JLabel prevUserSubmit = new JLabel();
    private JLabel[] prevSetSubmit = new JLabel[3];
    
    private JLabel[] playerNames = new JLabel[4];
    private JLabel[] playerScores = new JLabel[4];
    private JLabel[] playerThinks = new JLabel[4];
    
	
	JPanel cards = new JPanel(new CardLayout());
	//***** Variables for Login Panel *****
	JPanel LoginPanel = new JPanel();
	JLabel userLabel = new JLabel("Username:");
	JTextField txuser = new JTextField(15);
	JLabel passwordLabel = new JLabel("Password:");
	JPasswordField pass = new JPasswordField(15);
	JButton loginButton = new JButton("Login");
	//***** Variables for Decision Panel *****
	JPanel DecisionPanel = new JPanel();
		//Join Game side of Decision Panel
	JPanel openGamesPanel = new JPanel();
	JLabel joinGameNameLabel = new JLabel("Room Name:");
	JTextField joinGameName = new JTextField(15);
	JLabel joinGamePassLabel = new JLabel("Password:");
	JTextField joinGamePass = new JPasswordField(15);
	JButton joinGameButton = new JButton("Join Game");
		//Create Game side of Decision Panel
	JLabel createGameNameLabel = new JLabel("Room Name:");
	JTextField createGameName = new JTextField(15);
	JLabel createGamePassLabel = new JLabel("Password (Optional):");
	JTextField createGamePass = new JPasswordField(15);
	JButton createGameButton = new JButton("Create Game");
		//Logout Button of Decision Panel
	JButton logoutButton = new JButton("Exit");
	//***** Variables for GameRoom Panel *****
	JPanel GameRoomPanel = new JPanel();
	JLabel playerInfoName = new JLabel("Player");
	JLabel playerInfoScore = new JLabel("Score");
	JLabel playerInfoThink = new JLabel("Start?");
	JButton quitGame = new JButton("Quit");
	JButton startGame = new JButton("Start Game");
	JButton submitSet = new JButton("Submit Set");
	JButton noMoreSets = new JButton("No More Sets");
	
	
	
	public SetGame(){
		setTitle("Login");
		setSize(1200,675);
		
		//******************************
		// CREATE PANELS
		//******************************
		
		//***** LOGIN PANEL *****
		LoginPanel.setLayout (null);
		int loginLeftOffset = 525;
		int loginTopOffset = 230;
		int loginWidth = 150;
		userLabel.setBounds(	loginLeftOffset, loginTopOffset,	loginWidth, 20);
		txuser.setBounds(		loginLeftOffset, loginTopOffset+20,	loginWidth, 20);
		passwordLabel.setBounds(loginLeftOffset, loginTopOffset+50,	loginWidth, 20);
		pass.setBounds(			loginLeftOffset, loginTopOffset+70, loginWidth, 20);	
		loginButton.setBounds(560,345,80,35);
		userLabel.setHorizontalAlignment(JLabel.CENTER);
	    userLabel.setVerticalAlignment(JLabel.CENTER);
	    passwordLabel.setHorizontalAlignment(JLabel.CENTER);
	    passwordLabel.setVerticalAlignment(JLabel.CENTER);
		LoginPanel.add(userLabel);
		LoginPanel.add(txuser);
		LoginPanel.add(passwordLabel);
		LoginPanel.add(pass);
		LoginPanel.add(loginButton);
		cards.add(LoginPanel, "Login panel");
		
		
		//***** DECISION PANEL *****//
		DecisionPanel.setLayout (null);

		openGamesPanel.setMinimumSize(new Dimension(100, 800));
		openGamesPanel.setPreferredSize(new Dimension(100, 800));
		openGamesPanel.setMaximumSize(new Dimension(100,800));


			// DECISION PANEL - Join Game Form
		int joinGameOffsetX = 350;
		int joinGameOffsetY = 180;
		int joinGameWindowWidth = 150;
		int joinGameElementHeight = 20;	
		joinGameNameLabel.setBounds(	joinGameOffsetX, joinGameOffsetY,		joinGameWindowWidth, joinGameElementHeight);
		joinGameName.setBounds(			joinGameOffsetX, joinGameOffsetY+20,	joinGameWindowWidth, joinGameElementHeight);
		joinGamePassLabel.setBounds(	joinGameOffsetX, joinGameOffsetY+50,	joinGameWindowWidth, joinGameElementHeight);
		joinGamePass.setBounds(			joinGameOffsetX, joinGameOffsetY+70,	joinGameWindowWidth, joinGameElementHeight);
		joinGameButton.setBounds(		joinGameOffsetX, joinGameOffsetY+120,	joinGameWindowWidth, 60);
		joinGameNameLabel.setHorizontalAlignment(JLabel.CENTER);
		joinGameNameLabel.setVerticalAlignment(JLabel.CENTER);
		joinGamePassLabel.setHorizontalAlignment(JLabel.CENTER);
		joinGamePassLabel.setVerticalAlignment(JLabel.CENTER);
		DecisionPanel.add(joinGameNameLabel);
		DecisionPanel.add(joinGameName);
		DecisionPanel.add(joinGamePassLabel);
		DecisionPanel.add(joinGamePass);
		DecisionPanel.add(joinGameButton);
			// DECISION PANEL - Create Game Form
		int createGameOffsetX = 700;
		int createGameOffsetY = 180;
		int createGameWindowWidth = 150;
		int createGameElementHeight = 20;
		createGameNameLabel.setBounds(	createGameOffsetX, createGameOffsetY,		createGameWindowWidth, createGameElementHeight);
		createGameName.setBounds(		createGameOffsetX, createGameOffsetY+20,	createGameWindowWidth, createGameElementHeight);
		createGamePassLabel.setBounds(	createGameOffsetX, createGameOffsetY+50,	createGameWindowWidth, createGameElementHeight);
		createGamePass.setBounds(		createGameOffsetX, createGameOffsetY+70,	createGameWindowWidth, createGameElementHeight);
		createGameButton.setBounds(		createGameOffsetX, createGameOffsetY+120,	createGameWindowWidth, 60);
		createGameNameLabel.setHorizontalAlignment(JLabel.CENTER);
		createGameNameLabel.setVerticalAlignment(JLabel.CENTER);
		createGamePassLabel.setHorizontalAlignment(JLabel.CENTER);
		createGamePassLabel.setVerticalAlignment(JLabel.CENTER);
		DecisionPanel.add(createGameNameLabel);
		DecisionPanel.add(createGameName);
		DecisionPanel.add(createGamePassLabel);
		DecisionPanel.add(createGamePass);
		DecisionPanel.add(createGameButton);
			// DECISION PANEL - Logout Button
		logoutButton.setBounds(1200-110,675-70,100,40);
		DecisionPanel.add(logoutButton);
		cards.add(DecisionPanel, "Decision panel");
		
		
		//***** GAMEROOM PANEL *****//
		GameRoomPanel.setLayout (null);	
			// GAMEROOM PANEL - Player Info
		int PlayerInfoOffsetX = 30;
		int PlayerInfoOffsetY = 50;
		playerInfoName.setBounds(PlayerInfoOffsetX,PlayerInfoOffsetY,100,20);
		playerInfoScore.setBounds(PlayerInfoOffsetX+110,PlayerInfoOffsetY,50,20);
		playerInfoThink.setBounds(PlayerInfoOffsetX+170,PlayerInfoOffsetY,50,20);
		GameRoomPanel.add(playerInfoName);
		GameRoomPanel.add(playerInfoScore);
		GameRoomPanel.add(playerInfoThink);
		
		for(int i = 0; i < 4; i++){
			playerNames[i] = new JLabel("");
			playerNames[i].setBounds(PlayerInfoOffsetX, 	 PlayerInfoOffsetY+(i+1)*30, 100, 20);
			playerScores[i] = new JLabel("");
			playerScores[i].setBounds(PlayerInfoOffsetX+110, PlayerInfoOffsetY+(i+1)*30, 50, 20);
			playerThinks[i] = new JLabel("");
			playerThinks[i].setBounds(PlayerInfoOffsetX+170, PlayerInfoOffsetY+(i+1)*30, 50, 20);
			GameRoomPanel.add(playerNames[i]);
			GameRoomPanel.add(playerScores[i]);
			GameRoomPanel.add(playerThinks[i]);
		}

			// GAMEROOM PANEL - Cards On Field
		int CardWidth = 100;
		int CardHeight = 150;
		int CardSpacing = 10;
		int CardOffsetX = 300;
		int CardOffsetY = 50;
		/*		|	1	|	2	|	3	|	4	|	5	|	6	|	7	|
		 * 		|	8	|	9	|	10	|	11	|	12	|	13	|	14	|
		 *		|	15	|	16	|	17	|	18	|	19	|	20	|	21	|
		 */
		/*		|	16	|	1	|	2	|	3	|	4	|	13	|	19	|
		 * 		|	17	|	5	|	6	|	7	|	8	|	14	|	20	|
		 *		|	18	|	9	|	10	|	11	|	12	|	15	|	21	|
		 */
		cardOrder = new int[]{2,3,4,5,9,10,11,12,16,17,18,19,6,13,20,1,8,15,7,14,21};
		int cardnum = 0;
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 7; j++){
				cardButtonsArray[cardnum] = new JButton();
				cardButtonsArray[cardnum].setBounds( CardOffsetX + j*(CardWidth+CardSpacing), CardOffsetY + i*(CardHeight+CardSpacing), CardWidth, CardHeight);
				cardnum++;
			}
		}
			// GAMEROOM PANEL - Other Buttons
		quitGame.setBounds(PlayerInfoOffsetX-5,675-70,60,30);
		startGame.setBounds(480,250,240,100);
		submitSet.setBounds(CardOffsetX+165,CardOffsetY+500,150,60);
		noMoreSets.setBounds(CardOffsetX+165+170,CardOffsetY+500,150,60);
		GameRoomPanel.add(quitGame);
		GameRoomPanel.add(startGame);
		cards.add(GameRoomPanel, "GameRoom panel");
		
		//Finalize setting up all Cards onto the Frame
		add(cards);
		((CardLayout) cards.getLayout()).show(cards, "Login panel");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);	//position Application on center of screen
		setResizable(false);
		setVisible(true);
		
		
		
		//******************************
		// EVENT LISTENERS
		//******************************
		
		// Window - On Close Event
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.NO_OPTION)
					return;
				else if (dialogResult == JOptionPane.YES_OPTION){
					if (!serverConnStatus){
						dispose();
						return;
					}
					if (isInGame){
						out.println("LEAVEGAME");
						out.println(userStr);
						out.println(inGameRoom);
						isInGame = false;
						inGameRoom = "";
					}
					out.println("CLOSECONNECTION");
					dispose();
				}
			}
		});
		
		// Login Panel - Login Button
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String puname = txuser.getText();
				String ppaswd = pass.getText();
				if (puname.equals("")) {
					JOptionPane.showMessageDialog(null, "Please Enter a Value for 'Username'");
					txuser.requestFocus();
					return;
				}
				else if (ppaswd.equals("")){
					JOptionPane.showMessageDialog(null, "Please Enter a Value for 'Password'");
					pass.requestFocus();
					return;
				}
				
				//check to avoid double click event
				if (!isValidated){
			        // Make connection and initialize streams
					if (serverConnStatus){
						out.println("CHECKLOGIN");
						out.println(puname);
				        out.println(ppaswd);
				        userStr = puname;
					}
					else {
						JOptionPane.showMessageDialog(null,"Error: Connection to Server\nPlease contact an admin and rerun the application");
						dispose();
						return;
					}
				}
				else {
					System.out.println("Client is already connected");
				}
			}
			
		});

		// Decision Panel - JoinGame Button
		joinGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				try {
					joinRoom(joinGameName.getText(), joinGamePass.getText());
				} catch (Exception e) { System.out.println("Failed to Join Game Room"); }
			}
		});
	
		// Decision Panel - CreateGame Button
		createGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				if (createGameName.getText().equals("")){
					JOptionPane.showMessageDialog(null,"Please enter a value for the Game Room Name");
					return;
				}
				out.println("CREATE");
				out.println(createGameName.getText());
				out.println(createGamePass.getText());
			}
		});
		
		// Decision Panel - Logout Button
		logoutButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION){
					out.println("CLOSECONNECTION");
					dispose();
				}
			}
		});
		
		// GameRoom Panel - QuitGame Button
		quitGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to Quit?", "Quit", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION){	
					((CardLayout) cards.getLayout()).show(cards, "Decision panel");
					out.println("LEAVEGAME");
					out.println(userStr);
					out.println(inGameRoom);
					isInGame = false;
					inGameRoom = "";
					GameRoomPanel.remove(startGame);
					GameRoomPanel.add(startGame);
				}
			}
		});
		
		// GameRoom Panel - StartGame Button
		startGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				out.println("STARTGAME");
				out.println(inGameRoom);
				out.println(userStr);
			}
		});
		
		// GameRoom Panel - Card Buttons 
		for(int i = 0; i < 21; i++){
			final int temp = i;
			cardButtonsArray[temp].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae) {
					if (setBuffer[0] == -1 || !canClick){
						return;
					}
					boolean alreadyInSetBuffer = false;
					for(int j = 1; j < 4; j++){
						if (setBuffer[j] == cardValuesArray[temp]){
							setBuffer[j] = -1;
							alreadyInSetBuffer = true;
							setBuffer[0]--;
							cardButtonsArray[temp].setBorder(null);
							break;
						}
					}
					if (!alreadyInSetBuffer && setBuffer[0] != 3){
						if (setBuffer[1] == -1) {
							setBuffer[1] = cardValuesArray[temp];
						} else if (setBuffer[2] == -1){
							setBuffer[2] = cardValuesArray[temp];
						} else if (setBuffer[3] == -1){
							setBuffer[3] = cardValuesArray[temp];
						}
						cardButtonsArray[temp].setBorder(new LineBorder(Color.BLUE, 5));
						setBuffer[0]++;
					}
				}
			});
		}
		
		// GameRoom Panel -  Submit Set Button
		submitSet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				if (setBuffer[0] != 3)
					return;
				out.println("SUBMITTINGSET");
				out.println(inGameRoom);
				out.println(setBuffer[1]);
				out.println(setBuffer[2]);
				out.println(setBuffer[3]);
				out.println(userStr);
			}
		});
		
		// GameRoom Panel -  No More Sets Button
		noMoreSets.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				if (!sentNoSet){
					out.println("NOMORESET");
					out.println(inGameRoom);
					out.println(userStr);
					sentNoSet = true;
				}
			}
		});
		
	}// end SetGame()

	
	
	//******************************
	// HELPER FUNCTIONS
	//******************************
	
	public void joinRoom(String roomNameParam, String pswdParam) throws IOException{
		out.println("JOIN");
		out.println(roomNameParam);
		out.println(pswdParam);
		out.println(this.userStr);
		roomName = roomNameParam;
	}
	
	public void removeCards(int card1, int card2, int card3){
		int index1 = findCardIndex(card1);
		int index2 = findCardIndex(card2);
		int index3 = findCardIndex(card3);
		GameRoomPanel.remove(cardButtonsArray[index1]);
		GameRoomPanel.remove(cardButtonsArray[index2]);
		GameRoomPanel.remove(cardButtonsArray[index3]);
		cardValuesArray[index1] = -2;
		cardValuesArray[index2] = -2;
		cardValuesArray[index3] = -2;
		revalidate();
		repaint();
	}
	
	public void addCards(int cardnum){
		int i = findCardIndex(-2);
		if (i == -1){
			i = findCardIndex(-1);
		}
		String imagePath = "/img/" + cardnum + ".png";
		URL url = SetGame.class.getResource(imagePath);
		ImageIcon icon = new ImageIcon(url);
		cardButtonsArray[i].setIcon(new ImageIcon(((icon.getImage().getScaledInstance(100, 150, java.awt.Image.SCALE_SMOOTH)))));
		GameRoomPanel.add(cardButtonsArray[i]);
		cardValuesArray[i] = cardnum;
	}
	
	public int findCardIndex(int cardnum){
		int retVal = -1;
		for (int i = 0; i < 21; i++){
			if (cardValuesArray[cardOrder[i]-1] == cardnum){
				retVal = cardOrder[i]-1;
				break;
			}
		}
		return retVal;
	}

	// run() - main function to listen to server and process what is received
	public void run(){  
        try {
        	Socket socket = new Socket(HOST_IP,PORT);
            in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
	        out = new PrintWriter(socket.getOutputStream(), true);
	        serverConnStatus = true;
            while(true){
                process(in.readLine());
            }
        } catch (Exception ex){ serverConnStatus = false; return; }
    }

	// process() - process message sent from server
	private void process(String message) throws IOException{
		System.out.println(message);
		switch(message){
			case "VALID!":
				((CardLayout) cards.getLayout()).show(cards, "Decision panel");
				setTitle("Game Lobby");
				isValidated = true;
				break;
			case "BADPASSWORD":
				JOptionPane.showMessageDialog(null,"Invalid Username / Password");
				pass.setText("");
				if (txuser.equals("")){ txuser.requestFocus(); } 
				else { pass.requestFocus(); }
				break;
			case "NOUSER":
				JOptionPane.showMessageDialog(null,"Invalid Username / Password");
				pass.setText("");
				if (txuser.equals("")){ txuser.requestFocus(); } 
				else { pass.requestFocus(); }
				break;
			case "TOOMANY":
				JOptionPane.showMessageDialog(null,"There are too many Clients on the Server\nPlease try again later");
				break;
			case "ROOMALREADYEXISTS":
				JOptionPane.showMessageDialog(null,"'" + createGameName.getText() + "' already exists\nPlease try creating a room with another name");
				break;
			case "ROOMCREATED":
				joinRoom(createGameName.getText(), createGamePass.getText());
				break;
			case "BADGAMENAME":
				JOptionPane.showMessageDialog(null,"'" + roomName + "' does not exist\nPlease try a different Room Name");
				break;
			case "BADGAMEPASS":
				JOptionPane.showMessageDialog(null,"Invalid Password for '" + roomName + "' Room");
				break;
			case "USERALREADYINSIDE":
				JOptionPane.showMessageDialog(null,"Someone with your username is already inside '" + roomName + "' Room\nPlease let an admin know if this is not you");
				break;
			case "NOMORESPACE":
				JOptionPane.showMessageDialog(null, "'" + roomName + "' is currently full");
				break;
			case "GETALLGAMEROOMS":
				System.out.println("GOT GET ALL GAME ROOMS");
				int numGameRooms = Integer.parseInt(in.readLine());
				System.out.println(numGameRooms);
				openGamesPanel.setLayout(new GridLayout(numGameRooms, 1));
				System.out.println("before");
				for (int i = 0; i < numGameRooms; i++){
					if (Integer.parseInt(in.readLine()) == 0){
						JButton button = new JButton();
	         			button.setText(in.readLine()); //contactList.get(i).getSurname() + ", " + contactList.get(i).getGivenName());
	         			openGamesPanel.add(button);
					}
				}
				System.out.println("after");
				JScrollPane scrollPane = new JScrollPane(openGamesPanel);
				scrollPane.setBounds(0,0,100,800);
				DecisionPanel.add(scrollPane, BorderLayout.CENTER);
				break;
			case "JOINSUCCESS":
				((CardLayout) cards.getLayout()).show(cards, "GameRoom panel");
				setBuffer = new int[]{0,-1,-1,-1};
				prevSetSubmit[0] = new JLabel("");
				prevSetSubmit[1] = new JLabel("");
				prevSetSubmit[2] = new JLabel("");
				GameRoomPanel.add(prevSetSubmit[0]);
				GameRoomPanel.add(prevSetSubmit[1]);
				GameRoomPanel.add(prevSetSubmit[2]);
				isInGame = true;
				inGameRoom = roomName;
				String titleTemp = "Game Room: " + roomName;
				setTitle(titleTemp);
				break;
			case "UPDATEPLAYERINFO":
				String tempPlayerName = null;
				String tempPlayerScore = null;
				String tempPlayerThink = null;
				for (int i = 0; i < 4; i++){
					playerNames[i].setText("");
					playerScores[i].setText("");
					playerThinks[i].setText("");
				}
				int index = 0;
				for (int i = 0; i < 4; i++){
					tempPlayerName = in.readLine();
					tempPlayerScore = in.readLine();
					tempPlayerThink = in.readLine();
					if (!tempPlayerName.equals("User N/A")){
						playerNames[index].setText(tempPlayerName);
						playerScores[index].setText(tempPlayerScore);
						if (tempPlayerThink.equals("12345")){
							playerThinks[index].setText("");
							if (tempPlayerName.equals(userStr)){
								sentNoSet = false;
							}
						} else if (tempPlayerThink.equals("6789")){
							playerThinks[index].setText("X");
							if (tempPlayerName.equals(userStr)){
								sentNoSet = true;
							}
						}
						index++;
					}
				}
				break;
			case "GAMESTARTING":
				int CardWidth = 100;
				int CardHeight = 150;
				playerInfoThink.setText("No Sets");
				for(int i = 0; i < 12; i++){
					int cardnum = Integer.parseInt(in.readLine());
					String imagePath = "/img/" + cardnum + ".png";
					URL url = SetGame.class.getResource(imagePath);
					ImageIcon icon = new ImageIcon(url);
					cardButtonsArray[cardOrder[i]-1].setIcon(new ImageIcon(((icon.getImage().getScaledInstance(CardWidth, CardHeight, java.awt.Image.SCALE_SMOOTH)))));
					GameRoomPanel.add(cardButtonsArray[cardOrder[i]-1]);
					cardValuesArray[cardOrder[i]-1] = cardnum;
				}
				GameRoomPanel.remove(startGame);
				GameRoomPanel.add(submitSet);
				GameRoomPanel.add(noMoreSets);
				revalidate();
				repaint();
				sentNoSet = false;
				break;
			case "GAMESTARTEDALREADY":
				JOptionPane.showMessageDialog(null, "'" + roomName + "' has already started\nCannot join a game in progress");
				break;
			case "LEAVESUCCESSFUL":
				for (int i = 0; i < 21; i++){
					GameRoomPanel.remove(cardButtonsArray[i]);
				}
				GameRoomPanel.remove(prevSetSubmit[0]);
				GameRoomPanel.remove(prevSetSubmit[1]);
				GameRoomPanel.remove(prevSetSubmit[2]);
				GameRoomPanel.remove(prevUserSubmit);
				
				GameRoomPanel.remove(submitSet);
				GameRoomPanel.remove(noMoreSets);
				playerInfoThink.setText("Start?");
				revalidate();
				repaint();
				sentNoSet = false;
				break;
			case "NOTAVALIDSET":
				canClick = false;
				//flash invalid cards red
				final int cardindex1 = findCardIndex(setBuffer[1]);
				final int cardindex2 = findCardIndex(setBuffer[2]);
				final int cardindex3 = findCardIndex(setBuffer[3]);
				Timer blinkTimer = new Timer(150, new ActionListener(){
					private int count = 0;
					private int maxCount = 6;
					boolean isRed = false;
					public void actionPerformed(ActionEvent e){
						if (count >= maxCount){
							((Timer) e.getSource()).stop();
							canClick = true;
						} else {
							if (isRed){
								cardButtonsArray[cardindex1].setBorder(null);
								cardButtonsArray[cardindex2].setBorder(null);
								cardButtonsArray[cardindex3].setBorder(null);
								isRed = false;
							}else {
								cardButtonsArray[cardindex1].setBorder(new LineBorder(Color.RED, 5));
								cardButtonsArray[cardindex2].setBorder(new LineBorder(Color.RED, 5));
								cardButtonsArray[cardindex3].setBorder(new LineBorder(Color.RED, 5));
								isRed = true;
							}
							revalidate();
							repaint();
							count++;
						}
					}
				});
				blinkTimer.start();	

				setBuffer = new int[]{0,-1,-1,-1};
				break;
			case "SETWASSUBMITTED":
				String userSubmitted = in.readLine();
				int removeCard1 = Integer.parseInt(in.readLine());
				int removeCard2 = Integer.parseInt(in.readLine());
				int removeCard3 = Integer.parseInt(in.readLine());
				removeCards(removeCard1, removeCard2, removeCard3);

				GameRoomPanel.remove(prevSetSubmit[0]);
				GameRoomPanel.remove(prevSetSubmit[1]);
				GameRoomPanel.remove(prevSetSubmit[2]);
				String imagePath1 = "/img/" + removeCard1 + ".png";
				String imagePath2 = "/img/" + removeCard2 + ".png";
				String imagePath3 = "/img/" + removeCard3 + ".png";
				URL url1 = SetGame.class.getResource(imagePath1);
				URL url2 = SetGame.class.getResource(imagePath2);
				URL url3 = SetGame.class.getResource(imagePath3);
				ImageIcon img1 = new ImageIcon(new ImageIcon(url1).getImage().getScaledInstance(50, 75, java.awt.Image.SCALE_SMOOTH));
				ImageIcon img2 = new ImageIcon(new ImageIcon(url2).getImage().getScaledInstance(50, 75, java.awt.Image.SCALE_SMOOTH));
				ImageIcon img3 = new ImageIcon(new ImageIcon(url3).getImage().getScaledInstance(50, 75, java.awt.Image.SCALE_SMOOTH));
				prevSetSubmit[0] = new JLabel(img1);
				prevSetSubmit[1] = new JLabel(img2);
				prevSetSubmit[2] = new JLabel(img3);
				prevSetSubmit[0].setBounds(65, 300, 50, 75);
				prevSetSubmit[1].setBounds(65+60, 300, 50, 75);
				prevSetSubmit[2].setBounds(65+120, 300, 50, 75);
				GameRoomPanel.add(prevSetSubmit[0]);
				GameRoomPanel.add(prevSetSubmit[1]);
				GameRoomPanel.add(prevSetSubmit[2]);
				GameRoomPanel.remove(prevUserSubmit);
				prevUserSubmit = new JLabel("Previous Submission: " + userSubmitted);
				prevUserSubmit.setBounds(65, 280, 170, 20);
				GameRoomPanel.add(prevUserSubmit);

				//clear setBuffer and card selections
				for (int i = 0; i < 21; i++){
					cardButtonsArray[i].setBorder(null);
				}
				setBuffer = new int[]{0,-1,-1,-1};
				
				revalidate();
				repaint();
				break;
			case "ADDCARDS":
				int addCard1 = Integer.parseInt(in.readLine());
				int addCard2 = Integer.parseInt(in.readLine());
				int addCard3 = Integer.parseInt(in.readLine());
				addCards(addCard1);
				addCards(addCard2);
				addCards(addCard3);
				
				//clear setBuffer and card selections
				for (int i = 0; i < 21; i++){
					cardButtonsArray[i].setBorder(null);
				}
				setBuffer = new int[]{0,-1,-1,-1};
				
				revalidate();
				repaint();
				break;
			case "GAMEOVER":
				String status = in.readLine();
				String retMessage = "";
				if(status.equals("YOULOSE")){
					retMessage = "You Lost :(";
				} else if (status.equals("YOUWON")){
					retMessage = "You Won :D";
				}
				JOptionPane.showMessageDialog(null,retMessage);
				GameRoomPanel.remove(submitSet);
				GameRoomPanel.remove(noMoreSets);
				
				//clear setBuffer and card selections
				for (int i = 0; i < 21; i++){
					cardButtonsArray[i].setBorder(null);
				}
				setBuffer = new int[]{0,-1,-1,-1};
				setBuffer[0] = -1;
				revalidate();
				repaint();
				break;
			case "HELLOFROMSERVER":
				System.out.println("Hello!");
				break;
		}
	}// end run()
	
	public static void main(String[] args) throws Exception{
		SetGame frame = new SetGame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		System.out.println("Client Running...");
		frame.run();
	}
}// end SetGame Class
