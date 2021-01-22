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
        JsonObject resultData = new JsonObject();
        try {
            Connection connection = dataSource.getConnection();

            Helper.getUserInfo(resultData, request);
            getMovieInfo(resultData, connection, movieId);
            getGenres(resultData, connection, movieId);
            getStars(resultData, connection, movieId);
            // display cart size
            Helper.writeCartSize(resultData, request.getSession());

            response.setStatus(200);
            connection.close();
        } catch (Exception e) {
            resultData.addProperty("errorMessage", e.getMessage());
            response.setStatus(500);
            //System.out.println(jsonObject.toString());
        }
        PrintWriter out = response.getWriter();
        out.write(resultData.toString());
        out.close();
    }


    private void getMovieInfo(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieInfo_id);
        statement.setString(1, movieId);

        ResultSet rs = statement.executeQuery();
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
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieRating_id);
        statement.setString(1, movieId);

        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            movieData.addProperty("movie_rating", rs.getString("rating"));
            movieData.addProperty("movie_numVotes", rs.getString("numVotes"));
        }
        rs.close();
        statement.close();
    }

    private void getGenres(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieGenre_id);
        statement.setString(1, movieId);
        ResultSet rs = statement.executeQuery();
        JsonArray genres = new JsonArray();
        while (rs.next()) {
            genres.add(rs.getString("name"));
        }
        movieData.add("movie_genre", genres);
        rs.close();
        statement.close();
    }

    private void getStars(JsonObject movieData, Connection connection, String movieId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieStar_id);
        statement.setString(1, movieId);

        ResultSet rs = statement.executeQuery();
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
