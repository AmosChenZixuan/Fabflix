package main.java;

import java.sql.*;
import java.util.HashMap;

public class OptimizedParser extends NaiveParser {
    // data structures that allow to do O(1) local duplication check, thus speed up insertions
    // cache maps {key : id}
    HashMap<String, String> movie_cache; // key: title-year-director
    HashMap<String, String> star_cache; // key: name
    HashMap<String, Integer> genre_cache; // key: name
    // map xml ID to moviedb ID
    HashMap<String, String> saved_movie_id;
    // ID holder
    String max_movie_id;
    String max_star_id;

    public static void main(String[] args) {
        OptimizedParser self = new OptimizedParser();
        self.insertToDataBase();
        //self.printGenres();
        System.out.println("xmlParser: All tasks complete");
    }

    public void insertToDataBase(){
        try{
            // init
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            final String con_url = "jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false";
            Connection connection = DriverManager.getConnection(con_url,
                    "mytestuser", "mypassword");

            cache_movie(connection);
            cache_star(connection);
            cache_genre(connection);

            connection.setAutoCommit(false);
            System.out.println("Begin Add Movies");
            insert_all_movies(connection);
            System.out.println("Finish Add Movies");
            System.out.println("Begin Add Stars");
            insert_all_stars(connection);
            System.out.println("Finish Add Stars");
            System.out.println("Begin Add Relations");
            insert_all_relations(connection);
            System.out.println("Finish Add Relations");

            connection.close();
        }
        catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void insert_all_movies(Connection connection) throws SQLException {
        System.err.println("\n==========Inserting Movies==========");
        PreparedStatement stm_add_movie = connection.prepareStatement(ParserQuery.insertMovie);
        PreparedStatement stm_add_genre = connection.prepareStatement(ParserQuery.insertGenre);
        PreparedStatement stm_link_genre = connection.prepareStatement(ParserQuery.insertGinM);
        for (String key : movie_list.keySet().toArray(new String[0])){
            Movie movie = movie_list.get(key);
            String cache_key = make_movie_key(movie.title, movie.year, movie.director);
            String movie_id = movie_cache.get(cache_key);
            if (movie_id == null){
                movie_id = create_new_id(max_movie_id);
                max_movie_id = movie_id;
                stm_add_movie.setString(1, movie_id);
                stm_add_movie.setString(2, movie.title);
                stm_add_movie.setInt(3, movie.year);
                stm_add_movie.setString(4, movie.director);
                stm_add_movie.addBatch();
                movie_cache.put(cache_key, movie_id);
                // add & link genres
                for (String genre: movie.genres){
                    genre = lookUpGenre(genre);
                    if (genre_cache.get(genre) == null){
                        stm_add_genre.setString(1, genre);
                        stm_add_genre.addBatch();
                        genre_cache.put(genre, genre_cache.size()+1);
                    }
                    stm_link_genre.setInt(1, genre_cache.get(genre));
                    stm_link_genre.setString(2, movie_id);
                    stm_link_genre.addBatch();
                }
            }
            else{
                System.err.println(String.format("Movie '%s' exists in database", cache_key));
            }
            saved_movie_id.put(key, movie_id);
        }
        System.out.println("executing batch");
        stm_add_movie.executeBatch();
        stm_add_genre.executeBatch();
        stm_link_genre.executeBatch();
        // finish
        connection.commit();
        stm_add_movie.close();
        stm_add_genre.close();
        stm_link_genre.close();
    }

    private void insert_all_stars(Connection connection) throws SQLException{
        System.err.println("\n==========Inserting Stars==========");
        PreparedStatement statement = connection.prepareStatement(ParserQuery.insertStar);
        for (Star star: star_list){
            String star_id = star_cache.get(star.name);
            if (star_id != null){
                System.err.println(String.format("Star '%s' exists in database", star.name));
                continue;
            }
            star_id = create_new_id(max_star_id);
            max_star_id = star_id;
            statement.setString(1, star_id);
            statement.setString(2, star.name);
            if (star.brith == -1)
                statement.setNull(3, Types.INTEGER);
            else
                statement.setInt(3,star.brith);
            statement.addBatch();
            star_cache.put(star.name, star_id);
        }
        System.out.println("executing batch");
        statement.executeBatch();

        connection.commit();
        statement.close();
    }

    private void insert_all_relations(Connection connection) throws SQLException{
        System.err.println("\n==========Inserting Relations==========");
        PreparedStatement statement = connection.prepareStatement(ParserQuery.insertSinM);
        for (Relation relation:relations){
            String xml_id = relation.movie_id,
                    star_name = relation.star_name;
            String movie_id = saved_movie_id.get(xml_id),
                    star_id = star_cache.get(star_name);

            if (movie_id == null){
                System.err.println(String.format("Movie '%s' not found", relation.movie_title));
            }
            else if (star_id == null){
                System.err.println(String.format("Star '%s' not found", star_name));
            }
            else{
                statement.setString(1, star_id);
                statement.setString(2, movie_id);
                statement.addBatch();
            }
        }
        System.out.println("executing batch");
        statement.executeBatch();

        connection.commit();
        statement.close();
    }

    OptimizedParser(){
        super();
        movie_cache = new HashMap<String, String>();
        star_cache = new HashMap<String, String>();
        genre_cache = new HashMap<String, Integer>();
        saved_movie_id = new HashMap<String, String>();
        max_movie_id = "";
        max_star_id = "";
    }

    private String create_new_id(String oldId){
        String numPart = oldId.substring(2);
        numPart = (Integer.parseInt(numPart) + 1) + "";
        // refill leading 0s
        while (numPart.length() < oldId.length()-2)
            numPart = "0".concat(numPart);
        return oldId.substring(0, 2).concat(numPart);
    }

    private void cache_movie(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(ParserQuery.getMovies);
        int get_max_flag = 1;
        while(rs.next()){
            String id = rs.getString("id"),
                    title = rs.getString("title"),
                    director = rs.getString("director");
            int year = rs.getInt("year");
            if (get_max_flag-- > 0) // make sure only get the first id, which is the largest
                max_movie_id = id;
            movie_cache.put(make_movie_key(title, year, director), id);
        }
        rs.close();
        statement.close();
    }

    private String make_movie_key(String t, int y, String d){
        return String.format("%s-%d-%s", t, y, d);
    }

    private void cache_star(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(ParserQuery.getStars);
        int get_max_flag = 1;
        while(rs.next()){
            String id = rs.getString("id");
            if (get_max_flag-- > 0) // make sure only get the first id, which is the largest
                max_star_id = id;
            star_cache.put(rs.getString("name"), id);
        }
        rs.close();
        statement.close();
    }

    private void cache_genre(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(ParserQuery.getGenres);
        while(rs.next()){
            genre_cache.put(rs.getString("name"), rs.getInt("id"));
        }
        rs.close();
        statement.close();
    }

    private void printGenres(){
        for (String g:genre_cache.keySet().toArray(new String[0])){
            System.out.println(g + " - " + genre_cache.get(g));
        }
    }

}
