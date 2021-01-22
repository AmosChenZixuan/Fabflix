package main.java;

public class ParserQuery {
    // Naming Convention: QueryName_param1_param2_...

    static final String getGenres = "SELECT distinct id, name " +
            "FROM genres order by id ";
    static final String getStars = "SELECT * FROM stars order by id desc ";
    static final String getMovies = "SELECT * FROM moviedb.movies order by id desc ";
    static final String insertMovie = "insert into movies value(?, ?, ?, ?) ";
    static final String insertGenre = "insert into genres(name) value(?) ";
    static final String insertGinM = "insert into genres_in_movies value(?, ?) ";
    static final String insertStar = "insert into stars value(?, ?, ?); ";
    static final String insertSinM = "insert into stars_in_movies value(?, ?) ";

    // deprecated
    public static final String batch_add_movie = "call batch_add_movie(?,?,?)";
    public static final String batch_add_star = "call batch_add_star(?, ?)";
    public static final String batch_add_and_link_genre = "call batch_add_and_link_genre(?, ?, ?, ?)";
    public static final String batch_link_star = "call batch_link_star(?, ?, ?, ?)";

    public static final String call_batch_add_genre = "{call batch_add_genre(?)}";
    public static final String call_batch_link_genre = "{call batch_link_genre(?, ?, ?, ?)}";
    public static String toCallable(String query){
        return "{" + query + "}";
    }
}
