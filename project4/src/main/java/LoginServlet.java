package main.java;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.sql.*;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/index")
public class LoginServlet extends HttpServlet{
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    public static final String reCaptcha_SECRET_KEY = "6Leru-8UAAAAAGLn3mDnIUtpUruDnFcLgilBQp5k";

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
        String  login_type = request.getParameter("type"),
                username = request.getParameter("username"),
                password = request.getParameter("password"),
                reCapResp = request.getParameter("g-recaptcha-response"); // p3-reCaptcha
        //System.out.println("gRecaptchaResponse=" + reCapResp);
//        if (username.equals("") || password.equals("")){
//            resultData.addProperty("status", 0);
//            resultData.addProperty("message", "login failed: Please enter a username and password");
//            out.write(resultData.toString());
//            out.close();
//            return;
//        }
        //System.out.println(username+"--"+password);
        JsonObject resultData = new JsonObject();
        try{
            String cell_phone = request.getParameter("cell_phone");
//            if(cell_phone == null)
//                reCapVerify(reCapResp);

            // verification passed. Otherwise an exception is thrown.
            Connection connection = dataSource.getConnection();

            User user = null;
            switch (login_type){
                default:
                case "customer":
                    user = getCustomer(connection, username);break;
                case "admin":
                    user = getEmployee(connection, username);break;
            }

            // System.out.println(user.password +" ---"+user.email + "---" + password);
            boolean checkResult = new StrongPasswordEncryptor().checkPassword(password, user.getPassword());
            if (checkResult){
                resultData.addProperty("status", 1);
                resultData.addProperty("message", "login successfully");
                request.getSession().setAttribute("user", user);
                //init the info when the user just login
                init_user_searchinfo(request);
            } else {
                resultData.addProperty("status", 0);
                resultData.addProperty("message", "login failed: username or password is incorrect");
            }
            Helper.getUserInfo(resultData, request);
            connection.close();
        }
        catch(Exception e){
//            e.printStackTrace();
            resultData.addProperty("status", -1);
            resultData.addProperty("message", e.getMessage());
            //System.out.println(e.getMessage());
        }
        out.write(resultData.toString());
        out.close();
    }

    private Admin getEmployee(Connection connection, String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getAdmin);
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();
        Admin user = null;
        if (rs.next()) {
            String email = rs.getString("email"),
                    password = rs.getString("password"),
                    fullname = rs.getString("fullname");
            user = new Admin(email, password, fullname);
        }
        else{
            user = new Admin(null, null, null);
        }
        rs.close();
        statement.close();
        return user;
    }

    private Customer getCustomer(Connection connection, String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getCustomer);
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();
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

    private void init_user_searchinfo(HttpServletRequest request){
        HttpSession session = request.getSession(true);
        SearchData search_info = new SearchData();

        //session.setAttribute("browse","false");
        session.setAttribute("main_page_mode",1); // default to search
        session.setAttribute("sortType","1");
        session.setAttribute("search_info",search_info);
        session.setAttribute("page_num",1);

        if(request.getParameter("cell_phone") == null)// it is not mobile phone
            session.setAttribute("display_num", 50);
        else session.setAttribute("display_num", 50);

        session.setAttribute("query_count",100);
        session.setAttribute("fulltext_query","");
    }

    private void reCapVerify(String resp) throws Exception{
        if (resp == null || resp.length() == 0)
            throw new Exception("recaptcha verification failed: Please check the box before sign in");

        URL verifyUrl = new URL(SITE_VERIFY_URL);
        // Open Connection to URL
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();
        // Add Request Header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        // Data will be sent to the server.
        String postParams = String.format("secret=%s&response=%s", reCaptcha_SECRET_KEY, resp);
        // Send Request
        conn.setDoOutput(true);
        // Get the output stream of Connection
        OutputStream outStream = conn.getOutputStream();
        // Write data in this stream, which means to send data to Server.
        outStream.write(postParams.getBytes());
        outStream.flush();
        outStream.close();

//        // Response code return from server.
//        int responseCode = conn.getResponseCode();
//        System.out.println("responseCode=" + responseCode);
        // Get the InputStream from Connection to read data sent from the server.
        InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
        JsonObject result = new Gson().fromJson(inputStreamReader, JsonObject.class);
        inputStreamReader.close();

        //System.out.println("Response: " + result.toString());
        if (!result.get("success").getAsBoolean()) {
            throw new Exception("recaptcha verification failed: response is " + result.toString());
        }
    }
}
