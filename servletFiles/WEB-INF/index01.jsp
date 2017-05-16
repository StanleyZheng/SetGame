<!DOCTYPE html>
<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
    <title>Home Page</title>
    
    <style>
        body{
            background-color: skyblue;
        }
    
        #banner{
            text-align: center;
            font: 48px arial;
        }
        
        .heading{
            text-align: center;
            font: 36px arial;
        }
        
        .errorMessage{
            text-align: center;
            color: red;
        }
        
        #download{
            float: left;
            width: 50%;
        }
        
        #downloadButton{
            height: 50px;
            width: 200px;
        }
        
        #register{
            float: right;
            width:50%;
        }
        
        #registerSuccess{
            text-align: center;
            color: green;
        }
    </style>
</head>
<body>
    <p id="banner">
        <b>Play Set Now!</br></br>
    </p>
    
    <div id="download">
        <p class="heading">
            <b>Download Game</b>
        </p>
        
        <form method='POST' action="processForm.jsp" style="text-align:center">
            <input id="downloadButton" type ="submit" value="Download">
        </form>

        <p id="downloadError" class="errorMessage"> <!-- on failure -->
        </p>
    </div>
    
    <div id="register">
        <p class="heading">
            <b>Register New Account</b>
        </p>

        <form method='POST' action="authlogin" style="text-align:center" autocomplete="off">
            Username: <input type="text" name="username" value="" required></br></br>
            Password: <input type="password" name="password" value="" required></br></br>
            <input type="submit" value="Register">
        </form>

        <p id="registerError" class="errorMessage"> <!-- on failure -->
            <%
                if (session.getAttribute("messageFail") != null){
                    out.println(session.getAttribute("messageFail"));
                    session.removeAttribute("messageFail");
                }
            %>
        </p>
        <p id="registerSuccess"> <!-- on success -->
            <%
                if (session.getAttribute("messageSuccess") != null){
                    out.println(session.getAttribute("messageSuccess"));
                    session.removeAttribute("messageSuccess");
                }
            %>
        </p>
    </div>
</body>
</html>
