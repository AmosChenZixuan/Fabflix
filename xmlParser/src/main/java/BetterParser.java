package main.java;

import java.sql.*;
import java.util.HashMap;

public class BetterParser extends NaiveParser {
    // genre mappings
    public HashMap<String, Integer> genre_cache;

    public static void main(String[] args) {
        BetterParser self = new BetterParser();
        self.insertToDataBase();
        //self.printGenres();
    }

    public void insertToDataBase(){
        try{
            // init
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            final String con_url = "jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false";
            Connection connection = DriverManager.getConnection(con_url,
                    "mytestuser", "mypassword");
            connection.setAutoCommit(false);

            //cache_genre(connection);
            insert_all_movies(connection);
            insert_all_stars(connection);
            insert_all_relations(connection);

            connection.close();
        }
        catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void insert_all_movies(Connection connection) throws SQLException {
        CallableStatement stm_add_movie = connection.prepareCall(ParserQuery.toCallable(ParserQuery.batch_add_movie));
        CallableStatement stm_add_link_genre = connection.prepareCall(ParserQuery.toCallable(ParserQuery.batch_add_and_link_genre));
        for (String key : movie_list.keySet().toArray(new String[0])){
            Movie movie = movie_list.get(key);
            stm_add_movie.setString(1,movie.title);
            stm_add_movie.setInt(2,movie.year);
            stm_add_movie.setString(3,movie.director);
            stm_add_movie.addBatch();
            // add and link genres
            for (String genre: movie.genres){
                genre = lookUpGenre(genre);
                stm_add_link_genre.setString(1,movie.title);
                stm_add_link_genre.setInt(2,movie.year);
                stm_add_link_genre.setString(3,movie.director);
                stm_add_link_genre.setString(4,genre);
                stm_add_link_genre.addBatch();
            }
        }
        System.out.println("executing batch");
        int[] count1 = stm_add_movie.executeBatch();
        int[] count2 = stm_add_link_genre.executeBatch();
        // finish
        System.out.println(String.format("1:%d, 2:%d; commiting", count1.length, count2.length));
        connection.commit();
        stm_add_movie.close();
        stm_add_link_genre.close();
        System.out.println("Finish Add Movies");
    }

    private void insert_all_stars(Connection connection) throws SQLException{
        CallableStatement statement = connection.prepareCall(ParserQuery.toCallable(ParserQuery.batch_add_star));
        for (Star star: star_list){
            statement.setString(1, star.name);
            if (star.brith == -1)
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2,star.brith);
            statement.addBatch();
        }
        System.out.println("executing batch");
        int[] count = statement.executeBatch();
        System.out.println(String.format("Number of updates:%d; commiting", count.length));

        connection.commit();
        statement.close();
        System.out.println("Finish Add Stars");
    }

    private void insert_all_relations(Connection connection) throws SQLException{
        CallableStatement statement = connection.prepareCall(ParserQuery.toCallable(ParserQuery.batch_link_star));
        for (Relation relation:relations){
            String movieid = relation.movie_id,
                    star_name = relation.star_name;
            Movie movie = movie_list.get(movieid);
            if (movie != null) {
                statement.setString(1, movie.title);
                statement.setInt(2, movie.year);
                statement.setString(3, movie.director);
                statement.setString(4, star_name);
                statement.addBatch();
            }
        }
        System.out.println("executing batch");
        int[] count = statement.executeBatch();
        System.out.println(String.format("Number of updates:%d; commiting", count.length));

        connection.commit();
        statement.close();
        System.out.println("Finish Add Relations");
    }


    BetterParser(){
        super();
        genre_cache = new HashMap<String, Integer>();
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
