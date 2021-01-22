package main.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class NaiveParser {
    // string the key of each movie
    // each movie have title(String) year(int) director(String) and genres(Array<String>)
    // if title="" or director ="" or year =-1 or genre="" invalid movie
    public HashMap<String, Movie> movie_list;
    // star is only invalid when star's name is undefined
    public ArrayList<Star> star_list;
    // relation is invalid when star name is empty
    public ArrayList<Relation> relations;

    public static void main(String[] args) {
        NaiveParser self = new NaiveParser();
        //self.insertToDataBase();
    }

    public void insertToDataBase(){
        try{
            // init
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            final String con_url = "jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false";
            Connection connection = DriverManager.getConnection(con_url,
                    "mytestuser", "mypassword");

            insert_all_movies(connection);
            insert_all_stars(connection);
            connection.close();
        }
        catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e){
            System.out.println("Parser: " + e.getMessage());
        }
    }
    private void insert_all_stars(Connection connection) throws SQLException{
        PreparedStatement statement;
        for (Star star: star_list){
            statement = connection.prepareStatement(ParserQuery.batch_add_star);
            statement.setString(1,star.name);
            if (star.brith == -1)
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2,star.brith);
            statement.executeUpdate();
            statement.close();
        }
        for (Relation relation:relations){
            statement = connection.prepareStatement(ParserQuery.batch_link_star);
            String movieid = relation.movie_id;
            String star_name = relation.star_name;
            Movie movie = movie_list.get(movieid);
            if (movie != null) {
                statement.setString(1, movie.title);
                statement.setInt(2, movie.year);
                statement.setString(3, movie.director);
                statement.setString(4, star_name);
                statement.executeUpdate();
            }
            statement.close();
        }

    }
    private void insert_all_movies(Connection connection) throws SQLException {
        for (String key : movie_list.keySet().toArray(new String[0])){
            Movie movie = movie_list.get(key);
            //if (!movie.is_valid())
            //   continue;
            PreparedStatement statement = connection.prepareStatement(ParserQuery.batch_add_movie);
            statement.setString(1,movie.title);
            statement.setInt(2,movie.year);
            statement.setString(3,movie.director);
            statement.executeUpdate();
            statement.close();

           // System.out.println(String.format("%s call add_movie(%s, %d, %s)", key, movie.title, movie.year, movie.director));
            for (String genre: movie.genres){
                genre = lookUpGenre(genre);

//                if ( genre_cache.get(genre) == null){
//
//                    System.out.println(String.format("%s call add_genre(%s)", key, genre));
//                    genre_cache.put(genre, genre_cache.size()+1);
//                }
//                else{
//                    System.out.println(String.format("%s genre(%s) exists", key, genre));
//                }
                statement = connection.prepareStatement(ParserQuery.batch_add_and_link_genre);
                statement.setString(1,movie.title);
                statement.setInt(2,movie.year);
                statement.setString(3,movie.director);
                statement.setString(4,genre);
                statement.executeUpdate();
                //System.out.println(String.format("%s call link_m_g(%s)", key, genre));
                statement.close();
            }
        }
    }


    public String lookUpGenre(String genre){
        if (genre != null)
            genre = genre.trim();
        String g = GenreCode.code.get(genre);
        if (g == null) {
            //System.out.println(genre);
            return "Uncategorized";
        }
        return g;
    }

    NaiveParser(){
        parse();
    }

    public void parse(){
        MovieParser mp = new MovieParser();
        mp.run();
        movie_list = mp.ml;

        StarParser sp = new StarParser();
        sp.run();
        star_list = sp.sl;

        RelationParser rp = new RelationParser();
        rp.run();
        relations = rp.relations;
        System.out.println(movie_list.size()+"-----"+ star_list.size() +"--------"+ relations.size());
    }
}
