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
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String starId = request.getParameter("id");
        JsonObject resultData = new JsonObject();
        try {
            Connection connection = dataSource.getConnection();

            Helper.getUserName(resultData, request);
            resultData.add("star", getStar(connection, starId));
            // display cart size
            Helper.writeCartSize(resultData, request.getSession());
            response.setStatus(200);
            connection.close();
        } catch (Exception e){
            resultData.addProperty("errorMessage", e.getMessage());
            response.setStatus(500);
            //System.out.println(jsonObject.toString());
        }
        PrintWriter out = response.getWriter();
        out.write(resultData.toString());
        out.close();
    }

    private JsonArray getStar(Connection connection, String starId) throws SQLException {
        String query = String.format("select s.name, s.birthYear, m.* " +
                "from stars as s, stars_in_movies as sim, movies as m " +
                "where m.id = sim.movieId and sim.starId = s.id and s.id = '%s'" +
                "order by year desc, title", starId);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        JsonArray starData = new JsonArray();

        while (rs.next()){
            JsonObject movie = new JsonObject();
            //jsonObject.addProperty("star_id", starId);
            movie.addProperty("star_name", rs.getString("name"));
            movie.addProperty("star_birth_year", rs.getString("birthYear"));
            movie.addProperty("movie_id", rs.getString("id"));
            movie.addProperty("movie_title", rs.getString("title"));
            movie.addProperty("movie_year", rs.getString("year"));
            movie.addProperty("movie_director", rs.getString("director"));

            starData.add(movie);
        }
        rs.close();
        statement.close();
        return starData;
    }

}
