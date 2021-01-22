package main.java;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/index")
public class LoginServlet extends HttpServlet{
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // if this is a logout request
        if (request.getParameter("logout")!= null){
            request.getSession().setAttribute("user", null);
            //req.getSession().invalidate();
            return;
        }
        // else, process login information
        String username = request.getParameter("username"),
                password = request.getParameter("password");
        JsonObject resultData = new JsonObject();
        if (username.equals("") || password.equals("")){
            resultData.addProperty("status", 0);
            resultData.addProperty("message", "login failed: Please enter a username and password");
            out.write(resultData.toString());
            out.close();
            return;
        }
        try{
            Connection connection = dataSource.getConnection();
            Customer user = getUser(connection, username);
            //System.out.println(user.password +" ---"+user.email);
            if (password.equals(user.password)){
                resultData.addProperty("status", 1);
                resultData.addProperty("message", "login successfully");
                request.getSession().setAttribute("user", user);
            } else {
                resultData.addProperty("status", 0);
                resultData.addProperty("message", "login failed: username or password is incorrect");
            }
            connection.close();
        }
        catch(Exception e){
            resultData.addProperty("status", -1);
            resultData.addProperty("message", e.getMessage());
            //System.out.println(e.getMessage());
        }
        out.write(resultData.toString());
        out.close();
    }

    private Customer getUser(Connection connection, String username) throws SQLException {
        Statement statement = connection.createStatement();
        String query = String.format("SELECT * FROM moviedb.customers where email = '%s'", username);
        ResultSet rs = statement.executeQuery(query);
        Customer user = new Customer();
        while (rs.next()) {
            user.setId(rs.getString("id"));
            user.setFname(rs.getString("firstName"));
            user.setLname(rs.getString("lastName"));
            user.setCcid(rs.getString("ccId"));
            user.setAddress(rs.getString("address"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
        }
        rs.close();
        statement.close();
        return user;
    }
}
