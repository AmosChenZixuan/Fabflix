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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String movieId = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try {
            JsonObject movieData = new JsonObject();
            Connection connection = dataSource.getConnection();

            getMovieInfo(movieData, connection, movieId);
            getGenres(movieData, connection, movieId);
            getStars(movieData, connection, movieId);

            out.write(movieData.toString());
            response.setStatus(200);

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

    private void getMovieInfo(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        String query = "select * from movies where id = '" + movieId + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            movieData.addProperty("movie_id", rs.getString("id"));
            movieData.addProperty("movie_title", rs.getString("title"));
            movieData.addProperty("movie_year", rs.getString("year"));
            movieData.addProperty("movie_director", rs.getString("director"));
            getRating(movieData, connection, movieId);
        }
        rs.close();
        statement.close();
    }

    private void getRating(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        String query = "select * from ratings where movieId = '" + movieId + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            movieData.addProperty("movie_rating", rs.getString("rating"));
            movieData.addProperty("movie_numVotes", rs.getString("numVotes"));
        }
        rs.close();
        statement.close();
    }

    private void getGenres(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        String query = "select g.name " +
                "from movies as m, genres as g, genres_in_movies as gm " +
                "where m.id = gm.movieId and g.id = gm.genreId and m.id = '" + movieId + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        JsonArray genres = new JsonArray();
        while (rs.next()) {
            genres.add(rs.getString("name"));
        }
        movieData.add("movie_genre", genres);
        rs.close();
        statement.close();
    }

    private void getStars(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        String query = "select s.* " +
                "from movies as m, stars as s, stars_in_movies as sm " +
                "where m.id = sm.movieId and s.id = sm.starId and m.id = '" + movieId + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        JsonArray stars = new JsonArray();
        while (rs.next()) {
            JsonObject star = new JsonObject();
            star.addProperty("star_id", rs.getString("id"));
            star.addProperty("star_name", rs.getString("name"));
            star.addProperty("star_birth_year", rs.getString("birthYear"));
            stars.add(star);
        }
        movieData.add("movie_star", stars);
        rs.close();
        statement.close();
    }
}
