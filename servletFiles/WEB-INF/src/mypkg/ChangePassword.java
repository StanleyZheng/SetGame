package mypkg;

import java.io.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class ChangePassword extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                       throws IOException, ServletException {

        String message = "";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        int isFail = -1;

        try {
            if (request.getParameter("newpassword").equals(request.getParameter("confirmnewpassword"))){
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/SetGameDB", "root", "qweqwe");

                stmt = conn.createStatement();
                String sqlStr = "SELECT * FROM Users WHERE username = '" + request.getParameter("username") + "'";
                rset = stmt.executeQuery(sqlStr);

                // if username is already in the database
                if (rset.next()){
                    if (BCrypt.checkpw(request.getParameter("oldpassword"), rset.getString("password"))){
                        String hashedPassword = BCrypt.hashpw(request.getParameter("newpassword"), BCrypt.gensalt(12));
                        sqlStr = "UPDATE Users SET password='" + hashedPassword + "'WHERE username='" + request.getParameter("username") + "';";
                        stmt.executeUpdate(sqlStr);
                        message = "Successfully changed password!";
                        isFail = 0;
                    }
                }
                else {
                    message = "User does not exist!";
                    isFail = 1;
                }
            }
            else {
                isFail = 0;
                message = "Passwords do not match!";
            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }


            HttpSession session = request.getSession(false);
            //send message depending on what happened, 0:user successfully added, 1:user already exists
            if (isFail == 1){
                session.setAttribute("changeMessageFail", message);
            }
            else if (isFail == 0){
                session.setAttribute("changeMessageSuccess", message);
            }
            response.setContentType("type/html");
            response.sendRedirect("/");
        }
    }
}
