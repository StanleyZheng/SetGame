package mypkg;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class DownloadGame extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                       throws IOException, ServletException {
            response.sendRedirect("/SetGame.jar");
    }
}
