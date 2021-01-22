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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet{
    private static int genre_limit = 3, star_limit = 3;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            String query = "select m.id, m.title, m.year, m.director, r.rating " +
                    "from ratings as r, movies as m " +
                    "where r.movieId = m.id " +
                    "order by rating desc, numVotes desc " +
                    "limit 20";
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsArray = new JsonArray();

            while (rs.next()) {
                String movie_id = rs.getString("id");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);

                jsonObject.addProperty("movie_title", rs.getString("title"));

                jsonObject.addProperty("movie_year", rs.getString("year"));
                jsonObject.addProperty("movie_director", rs.getString("director"));
                jsonObject.addProperty("movie_rating", rs.getString("rating"));

                JsonArray genres = getGenres(connection, movie_id, genre_limit);
                jsonObject.add("movie_genre", genres);
                JsonArray stars = getStars(connection, movie_id, star_limit);
                jsonObject.add("movie_star", stars);

                jsArray.add(jsonObject);
            }

            out.write(jsArray.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
            //System.out.println(jsonObject.toString());
        }
        out.close();
    }

    private JsonArray getGenres(Connection connection, String id, int limit) throws SQLException {
        Statement statement = connection.createStatement();
        JsonArray result = new JsonArray();
        String query = "select g.name " +
                "from genres as g, genres_in_movies as gm " +
                "where gm.movieId = \'" + id + "\' and g.id = gm.genreId " +
                "limit " + limit + "";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            result.add(rs.getString("name"));
        }
        rs.close();
        statement.close();
        return result;
    }

    private JsonArray getStars(Connection connection, String id, int limit) throws SQLException {
        Statement statement = connection.createStatement();
        JsonArray result = new JsonArray();
        String query = "select s.id, s.name " +
                "from stars as s, stars_in_movies as sm " +
                "where sm.movieId = \'" + id +"\' and s.id = sm.starid " +
                "limit " + limit + "";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            JsonObject star = new JsonObject();
            star.addProperty("star_id", rs.getString("id"));
            star.addProperty("star_name", rs.getString("name"));
            result.add(star);
        }
        rs.close();
        statement.close();
        return result;
    }
}
