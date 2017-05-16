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
        
        .successMessage{
            text-align: center;
            color: green;
        }
        
        #download{
            float: left;
            width: 33%;
        }
        
        #downloadButton{
            height: 50px;
            width: 200px;
        }
        
        #register{
            display: inline-block;
            width:33%;
        }
        
        #changepw{
            float: right;
            width: 33%;
        }
        
        .formtable{
            margin: auto;
            text-align: right;
        }
        
        .formtable tr th{
            padding-bottom: 10px;
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
        
        <form method='POST' action="downloadGame" style="text-align:center">
            <input id="downloadButton" type ="submit" value="Download">
        </form>

        <p id="downloadError" class="errorMessage"> <!-- on failure -->
        </p>
    </div>
    
    <div id="register">
        <p class="heading">
            <b>Register New Account</b>
        </p>

        <form method='POST' action="regUser" style="text-align:center" autocomplete="off">
            <table class="formtable">
                <tr>
                    <th>Username:</th>
                    <th><input type="text" name="username" value="" required></th>
                </tr>
                <tr>
                    <th>Password:</th>
                    <th><input type="password" name="password" value="" required></th>
                </tr>
                <tr>
                    <th>Confirm Password:</th>
                    <th><input type="password" name="confirmpassword" value="" required></th>
                </tr>
            </table>
            </br><input type="submit" value="Register">
        </form></br>

        <p id="registerError" class="errorMessage"> <!-- on failure -->
            <%
                if (session.getAttribute("regMessageFail") != null){
                    out.println(session.getAttribute("regMessageFail"));
                    session.removeAttribute("regMessageFail");
                }
            %>
        </p>
        <p id="registerSuccess" class="successMessage"> <!-- on success -->
            <%
                if (session.getAttribute("regMessageSuccess") != null){
                    out.println(session.getAttribute("regMessageSuccess"));
                    session.removeAttribute("regMessageSuccess");
                }
            %>
        </p>
    </div>
    
    <div id="changepw">
        <p class="heading">
            <b>Change Password</b>
        </p>
        
        <form method='POST' action="changePW" style="text-align:center" autocomplete="off">
            <table class="formtable">
                <tr>
                    <th>Username:</th>
                    <th><input type="text" name="username" value="" required></th>
                </tr>
                <tr>
                    <th>Old Password:</th>
                    <th><input type="password" name="oldpassword" value="" required></th>
                </tr>
                <tr>
                    <th>New Password:</th>
                    <th><input type="password" name="newpassword" value="" required></th>
                </tr>
                <tr>
                    <th>Confirm New Password:</th>
                    <th><input type="password" name="confirmnewpassword" value="" required></th>
                </tr>
            </table>
            </br><input type="submit" value="Change Password">
        </form></br>
        
        <p id="changepwError" class="errorMessage"> <!-- on failure -->
            <%
                if (session.getAttribute("changeMessageFail") != null){
                    out.println(session.getAttribute("changeMessageFail"));
                    session.removeAttribute("changeMessageFail");
                }
            %>
        </p>
        <p id="changepwSuccess" class="successMessage"> <!-- on success -->
            <%
                if (session.getAttribute("changeMessageSuccess") != null){
                    out.println(session.getAttribute("changeMessageSuccess"));
                    session.removeAttribute("changeMessageSuccess");
                }
            %>
        </p>
    </div>
</body>
</html>
