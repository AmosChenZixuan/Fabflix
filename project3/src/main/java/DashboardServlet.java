package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name="DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    private JsonObject dbMetaData = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = req.getSession();

        // check if user type is Admin
        User user = (User)session.getAttribute("user");
        if (user == null || !user.getType().equals("Admin")){
            resultData.addProperty("isAllowed", false);
            resp.getWriter().write(resultData.toString());
            return;
        }
        resultData.addProperty("isAllowed", true);
        try{
            resultData.add("metaData", getMetaData());
            Helper.getUserInfo(resultData, session);
        }
        catch (Exception e){
            System.out.println("DashBoard:"+e.getMessage());
        }
        resp.getWriter().write(resultData.toString());
        // isAllowed : bool
        // metaData : json {table:columns{name, type, default, key, extra}}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        //HttpSession session = req.getSession();

        int action = Integer.parseInt(req.getParameter("action"));
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            // 0: add movie, 1: add new star
            switch(action){
                case 0:
                    addMovie(req, connection, resultData);
                    break;
                case 1:
                    addStar(req, connection, resultData);
                    break;
                default:
                    writeMessage(resultData, -1, "Form Error: Unrecognized Action");
            }

        }
        catch (NumberFormatException e){
            System.out.println("DashBoard:"+e.getMessage());
            writeMessage(resultData, -1, "Input Error: Please double check your input and their types");
        }
        catch (SQLException e){
            System.out.println("DashBoard:"+e.getMessage());
            writeMessage(resultData, -1, "SQL Error: " + e.getMessage());
        }
        catch (Exception e){
            System.out.println("DashBoard:"+e.getMessage());
            writeMessage(resultData, -1, "Input Error: " + e.getMessage());
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        resp.getWriter().write(resultData.toString());
    }

    private void addMovie(HttpServletRequest req, Connection connection, JsonObject resultData) throws SQLException {
        String movie_title = req.getParameter("movie_title"),
                movie_year = req.getParameter("movie_year"),
                movie_director = req.getParameter("movie_director"),
                star_name = req.getParameter("star_name"),
                birth_year = req.getParameter("star_birth"),
                genre = req.getParameter("genre");
        // check required fields
        if ("".equals(movie_title) || "".equals(movie_year) || "".equals(movie_director) ||
                "".equals(star_name) || "".equals(genre)){
            writeMessage(resultData, -1, "Request Failed: All Fields except for \"Star Birth Year\" is required but some are not provided");
            return;
        }
        // call procedure
        PreparedStatement statement = connection.prepareStatement(SqlQuery.add_new_movie);
        statement.setString(1, movie_title);
        statement.setInt(2, Integer.parseInt(movie_year));
        statement.setString(3, movie_director);
        statement.setString(4, star_name);
        if ("".equals(birth_year))
            statement.setNull(5, Types.INTEGER);
        else
            statement.setInt(5, Integer.parseInt(birth_year));
        statement.setString(6, genre);
        ResultSet rs = statement.executeQuery();
        rs.next();
        int colCount = rs.getMetaData().getColumnCount();
        // retrieve id
        if (colCount > 1) {
            String movie_id = rs.getString("movieId");
            String star_id = rs.getString("starId");
            int genre_id = rs.getInt("genreId");
            writeMessage(resultData, 1, String.format("Request Success!: Movie(%s) is added to Database; " +
                    "Star(%s) and Genre(%d) are successfully linked to the Movie", movie_id, star_id, genre_id));
        }
        else
            writeMessage(resultData, -1, String.format("Request Failed: The movie you want to add already exists in the record" +
                    "and has id %s", rs.getString("movieId")));

        rs.close();
        statement.close();
    }

    private void addStar(HttpServletRequest req, Connection connection, JsonObject resultData) throws SQLException {
        String star_name = req.getParameter("star_name"),
                birth_year = req.getParameter("star_birth");
        // check required fields
        if ("".equals(star_name)){
            writeMessage(resultData, -1, "Request Failed: Field \"Star Name\" is required but not provided");
            return;
        }
        // call procedure
        PreparedStatement statement = connection.prepareStatement(SqlQuery.add_new_star);
        statement.setString(1, star_name);
        if ("".equals(birth_year))
            statement.setNull(2, Types.INTEGER);
        else
            statement.setInt(2, Integer.parseInt(birth_year));
        ResultSet rs = statement.executeQuery();
        // retrieve id
        if (rs.next()) {
            String star_id = rs.getString("starId");
            writeMessage(resultData, 1, String.format("Request Success!: Star(%s) is added to Database", star_id));
        }
        else
            writeMessage(resultData, -1, "Request Failed: Database failed to insert the record");

        rs.close();
        statement.close();
    }

    private JsonObject getMetaData() throws SQLException {
        if (dbMetaData != null){
            System.out.println("DashBoard: db metadata cached, exiting");
            return dbMetaData;
        }
        // if not cached, retrieve from db
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(SqlQuery.getDbMetaData);

        JsonObject metaData = new JsonObject();
        while (rs.next()){
            String table_name = rs.getString("TABLE_NAME");
            // get table (object)
            if (!metaData.has(table_name)){
                metaData.add(table_name, new JsonObject());
            }
            JsonObject table = metaData.get(table_name).getAsJsonObject();
            // get columns (array)
            if (!table.has("columns")){
                table.add("columns", new JsonArray());
            }
            JsonArray cols = table.get("columns").getAsJsonArray();
            // write col info as object
            JsonObject col = new JsonObject();
            col.addProperty("name", rs.getString("COLUMN_NAME"));
            col.addProperty("type", rs.getString("COLUMN_TYPE"));
            col.addProperty("key", rs.getString("COLUMN_KEY"));
            col.addProperty("extra", rs.getString("EXTRA"));
            cols.add(col);
        }
        dbMetaData = metaData;

        rs.close();
        statement.close();
        connection.close();
        return dbMetaData;
    }

    private void writeMessage(JsonObject resultData, int status, String msg){
        resultData.addProperty("status", status);
        resultData.addProperty("message", msg);
    }
}


