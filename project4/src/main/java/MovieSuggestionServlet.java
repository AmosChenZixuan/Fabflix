package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name= "MovieSuggestionServlet", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {
    @Resource(name="jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonArray suggestions = new JsonArray();

        String query = req.getParameter("query");

        // return the empty json array if query is null or empty
        if (query == null || query.trim().isEmpty()) {
            resp.getWriter().write(suggestions.toString());
            return;
        }

        try{
            Connection connection = dataSource.getConnection();

            getSuggestions(connection, suggestions, query);

            connection.close();
        }
        catch (Exception e){
            System.out.println("Suggestion: " + e.getMessage());
            resp.sendError(500, e.getMessage());
        }

        resp.getWriter().write(suggestions.toString());
    }

    private void getSuggestions(Connection connection, JsonArray suggestions, String query) throws SQLException {
        //System.out.println("suggestion got query: " + query);
        String[] tokens = query.trim().split(" +");
        PreparedStatement statement = connection.prepareStatement(SqlQuery.suggestion_query_generator(tokens.length));
        int i = 1;
        for (; i <= tokens.length; i++ ){
            statement.setString(i, tokens[i-1]);
        }
        // fuzzy search parameters
        int threshold = query.length()/4;
        statement.setString(i++, query);
        statement.setInt(i, threshold);

        ResultSet rs = statement.executeQuery();
        while (rs.next()){
            JsonObject sg = new JsonObject();
            sg.addProperty("value", rs.getString("title"));
            JsonObject data = new JsonObject();
            data.addProperty("movieId", rs.getString("id"));
            sg.add("data", data);
            suggestions.add(sg);
        }
        rs.close();
        statement.close();
    }
}
