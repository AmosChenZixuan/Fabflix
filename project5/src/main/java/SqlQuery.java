package main.java;

import java.util.ArrayList;

public class SqlQuery {
    // Naming Convention: QueryName_param1_param2_...

    // login
    public static final String getCustomer = "SELECT * FROM moviedb.customers where email = ? limit 1 ";
    public static final String getAdmin ="SELECT * FROM moviedb.employees where email = ? limit 1 ";

    // main page only for substring search it will return 2 query string: the first element is the count query the second element is the search query
    public static ArrayList<String> search_query_generator(ArrayList<String> inputs,SearchData sd, String sort,String genre) {
        ArrayList<String> to_return = new ArrayList<String>();
        String query = "";
        String count_query= "";
        String input = "";

        for(int i=0; i<inputs.size()-1;i++)
            input += inputs.get(i) +" and ";

        if (inputs.size() >= 1)
            input += inputs.get(inputs.size() - 1);
        if(!genre.equals("")) {// if it is browsing by movie genre/letter
            if(genre.length() ==1){//it is an on letter browsing search
                if(genre.charAt(0) == '*'){// if it is *
                    query = " Select id, title, year, director, rating " +
                            " From ratings as r right outer join  movies as m on r.movieId = m.id " +
                            " where title REGEXP ? " + sort;
                }
                else {
                    query = " Select id, title, year, director, rating " +
                            " From ratings as r right outer join  movies as m on r.movieId = m.id " +
                            " Where title like CONCAT(?,'%')" + sort;
                }
                count_query = "select count(*)as num from (" + query + ") as t";
                query += " limit ? offset ? ";
            }
            else{// browse by genre name e.g action/comedy
                String t = " (select id from genres g where g.name = ? ) as t "; // get all the genre and assign it as t
                String t2 = " ( select distinct(m.id), m.title, m.year, m.director, r.rating ";// same as the query as the search star query below
                t2 += "  From ratings as r right outer join  movies as m on r.movieId = m.id , genres_in_movies as gm, " +t;
                t2 += " Where gm.genreId = t.id and gm.movieId = m.id ";
                count_query = " Select count(id) as num " +" From " + t2 +" ) as t2 ";
                t2 += " limit ?  offset ? ) as t2 ";
                query += " select * from " + t2 + sort;
            }
        }
        else if (sd.star.equals("")) { // if the search form does't have star only have title year director
            query = " Select id, title, year, director, rating From ratings as r right outer join  movies as m on r.movieId = m.id ";
            if (input.equals("")) {
                query += sort + " limit ? offset ? "; //  empty user input does need where clause
                count_query = " Select count(id) as num From ratings as r right outer join  movies as m on r.movieId = m.id ";
            }
            else{
                query += " Where " + input + sort + " limit ? offset ? "; // not empty user input need where clause
                count_query = " Select count(id) as num From ratings as r right outer join  movies as m on r.movieId = m.id where "+input;
            }
        }
        else { // the search form at least contain a star section
            String t = " (select id from stars s where s.name like CONCAT( '%',?,'%') ) as t "; // get all the id of stars who name contains the input star name
            String t2 = " ( select distinct(m.id), m.title, m.year, m.director, r.rating "; // main query get all the movies info that contains the stars in t
            t2 += " From ratings as r right outer join  movies as m on r.movieId = m.id , stars_in_movies as sm, " + t;
            if (input.equals("")) t2 += " Where sm.starId = t.id and sm.movieId = m.id";
            else  t2 += "Where "+ input +" and sm.starId = t.id and sm.movieId = m.id ";
            count_query = " Select count(id) as num " +" From " + t2 +" ) as t2"; // retrieve t2
            t2 += " limit ?  offset ? ";
            query = " select * from " +t2 + " ) as t2"+ sort;
        }
        to_return.add(count_query);
        to_return.add(query);
        return to_return;
    }
    public static final String base_from_clause = " From ratings as r right outer join  movies as m on r.movieId = m.id ";
    public static final String fuzzy_search = " Or edth(lower(m.title), lower(?) , ?) = 1 ";
    //generator for fulltext search same as the search_query_generator
    public static ArrayList<String> fulltext_query_generator(int size, String sort){
        ArrayList<String> to_return = new ArrayList<String>();
        String query = " Select id, title, year, director, rating " + base_from_clause;
        if (size != 0){
            query += "Where match(m.title) against( CONCAT(";
            for(int i=0; i<size; i++)
                query +=" '+',?,'*' ";
            query+= " ) IN BOOLEAN MODE)";
            query += fuzzy_search;
        }
        String count_query =" Select count(id) as num  from (" + query +") as t";
        to_return.add(count_query); // count query
        to_return.add(query+sort+" limit ? offset ? ");// search query
        return to_return;
    }

    public static String suggestion_query_generator(int size){
        String query = " Select id, title " + base_from_clause;;
        if (size != 0){
            query += "Where match(m.title) against( CONCAT(";
            for(int i=0; i<size; i++)
                query +=" '+',?,'*' ";
            query+= " ) IN BOOLEAN MODE)";
            query += fuzzy_search;
        }
        return query + " order by rating desc, title limit 10";
    }

    public static final String getMovieGenre_id = "select g.name " +
            "from genres as g, genres_in_movies as gm " +
            "where gm.movieId = ? and g.id = gm.genreId " +
            "order by name ";

    public static final String getMovieGenre_id_limit = getMovieGenre_id +
            "limit ? ";

    public static final String getMovieStar_id = "select S.* " +
            "from (select s.* " +
            "from stars as s, stars_in_movies as sm " +
            "where sm.movieId = ? and s.id = sm.starId) as S, stars_in_movies as sim " +
            "where S.id = sim.starId " +
            "group by S.id " +
            "order by count(S.id) desc, S.name ";

    public static final String getMovieStar_id_limit =  getMovieStar_id +
            "limit ? ";

    public static final String getDistinctGenres = "SELECT distinct(name) " +
            "FROM genres order by name ";

    // single page
    public static final String getMovieInfo_id = "select * from movies where id = ? ";

    public static final String getMovieRating_id = "select * from ratings where movieId = ? ";

    public static final String getStar = "select s.name, s.birthYear, m.* " +
            "from stars as s, stars_in_movies as sim, movies as m " +
            "where m.id = sim.movieId and sim.starId = s.id and s.id = ? " +
            "order by year desc, title ";

    // payment
    public static final String getMaxSaleId = "SELECT max(id) as lastId FROM sales Limit 1";

    public static final String checkCardInfo = "select count(id) " +
            "from creditcards " +
            "where id = ? and firstName = ? and lastName = ? and expiration = ? " +
            "group by id ";

    // confirmation

    // dashboard
    public static final String dbmd_cols = " TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, EXTRA ";
    public static final String getDbMetaData = "SELECT " + dbmd_cols +
            "FROM information_schema.columns " +
            "WHERE table_schema = 'moviedb' and TABLE_NAME not like '%backup' " +
            "ORDER BY TABLE_NAME, ORDINAL_POSITION ";

    public static final String add_new_star = "call add_star(?, ?)";
    public static final String add_new_movie = "call add_movie(?, ?, ?, ?, ?, ?)";
}
