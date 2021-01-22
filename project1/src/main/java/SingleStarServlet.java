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
        PrintWriter out = response.getWriter();

        try {
            Connection connection = dataSource.getConnection();
            String query = "select s.name, s.birthYear, m.* " +
                    "from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and s.id = '" + starId + "'";

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            JsonArray starData = new JsonArray();

            while (rs.next()){
                JsonObject jsonObject = new JsonObject();
                //jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", rs.getString("name"));
                jsonObject.addProperty("star_birth_year", rs.getString("birthYear"));
                jsonObject.addProperty("movie_id", rs.getString("id"));
                jsonObject.addProperty("movie_title", rs.getString("title"));
                jsonObject.addProperty("movie_year", rs.getString("year"));
                jsonObject.addProperty("movie_director", rs.getString("director"));

                starData.add(jsonObject);
            }
            out.write(starData.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
            connection.close();

        } catch (Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
            //System.out.println(jsonObject.toString());
        }
        out.close();
    }

}
