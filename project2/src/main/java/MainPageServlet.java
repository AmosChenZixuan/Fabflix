package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@WebServlet(name = "MainPageServlet", urlPatterns = "/api/main-page")
public class MainPageServlet extends HttpServlet {
    private final static int genre_limit = 3, star_limit = 3;
    private static int display_num =2;
    private static int max_num = 100;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = request.getSession(true);
        SearchData search_info = (SearchData) session.getAttribute("search_info");
        Integer page_num = (Integer) session.getAttribute("page_num");
        //init searching data and page num to 1
        if(search_info ==null){
            search_info = new SearchData();
            page_num = 1;
            //System.out.println("hi new user");
            session.setAttribute("browse","false");
            session.setAttribute("sortType","1");
            session.setAttribute("search_info",search_info);
            session.setAttribute("page_num",1);
        }
        String title = search_info.title;
        String year = search_info.year;
        String director =search_info.director;
        String star = search_info.star;
        String sortType = (String) session.getAttribute("sortType");

        String processed_input = process_search_input(title,year,director);
        resultData.addProperty("page_num",page_num);
        Integer off_set_num =display_num*(page_num-1);
       // System.out.println("title:"+title+" year: "+year+" director:"+ director+ " star:"+ star+" page num" +page_num);
        String is_browse = (String) session.getAttribute("browse");
        String genre;
        if(is_browse.equals("true")){
            //System.out.println("currentlt is browse:" +(String) session.getAttribute("genre"));
            genre = (String) session.getAttribute("genre");
        }
        else{genre = "";}
        try {
            Connection connection = dataSource.getConnection();
//            if(search_info.empty()&& sortType.equals("1")&& genre.equals("")){
//                System.out.println(123321+"");
//                resultData.add("movieList", getMovieList(connection, display_num,off_set_num));}
//            else
            resultData.add("movieList", apply_search(connection,display_num,off_set_num,processed_input, star,sortType,genre));

            // remember and auto-refill user's search and sort
            if (session.getAttribute("browse").equals("false"))
                resultData.add("search_info", search_info.toJsonObject());
            resultData.addProperty("sort_type", sortType);
            // display cart size
            Helper.writeCartSize(resultData, session);

            JsonObject movie_limit = new JsonObject();
            movie_limit.addProperty("movie_limit", display_num);
            resultData.add("movie_limit",movie_limit);
            resultData.add("genres",getDistinctGenres(connection));
            Helper.getUserName(resultData, request);
            response.setStatus(200);
            connection.close();
        } catch (Exception e) {
            resultData.addProperty("errorMessage", e.getMessage());
            response.setStatus(500);
            System.out.println(resultData.toString());
        }
        PrintWriter out = response.getWriter();
        out.write(resultData.toString());
        out.close();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(true);
        int page_num = (int) session.getAttribute("page_num");
        String action = request.getParameter("action");
        //System.out.println("action: "+action);
        switch(action){
            case "search":{
                page_num=1;
                String s = request.getParameter("star").trim();
                String d = request.getParameter("director").trim();
                String t = request.getParameter("title").trim();
                String y = request.getParameter("year").trim();
                session.setAttribute("browse","false");
                SearchData data = new SearchData(s,d,t,y);
               // data.print();
                session.setAttribute("search_info",data);
                break;
            }
            case "next":
                int i =(page_num+1)*display_num;
                if (i<max_num+display_num)
                page_num++;break;
            case "pre":{
               if (page_num >1)
                   page_num--;
                   break;}
            case"sort":{
                page_num =1;
                String sort_type = request.getParameter("sortType");
                session.setAttribute("sortType",sort_type);
                break;}
            case"dis_num":{
                String n = request.getParameter("num");
                display_num = Integer.parseInt(n);
                page_num =1;
                break;
            }
            case"browse_genre":{
                String genre = request.getParameter("genre");
                session.setAttribute("genre",genre);
                session.setAttribute("browse","true");
                page_num = 1;
                //System.out.println("got the browse_genre post:" +genre);
                break;
            }
        }
        session.setAttribute("page_num",page_num);
    }

    private JsonArray apply_search(Connection connection, int limit, int offset_num, String input, String star_name, String sortT,String genre)throws SQLException {
        Statement statement = connection.createStatement();
        String sort = get_sort(sortT);
        String count_query ="";
        int count=0;
        String query="";
        String t2= "";

        if(!genre.equals("")){// if it is browsing by movie genre/letter
            if(genre.length() ==1)//it is an on letter browsing search
            {
                if(genre.charAt(0) == '*'){// if it is *
                    query = " Select id, title, year, director, rating " +
                            " From ratings as r right outer join  movies as m on r.movieId = m.id " +
                            " where title REGEXP '^[^A-Za-z0-9]'" + sort;
                    count_query = "select count(*)as num from (" + query + ") as t";;
                    query += " limit " + limit + " offset " + offset_num;
                }
                else {
                    query = " Select id, title, year, director, rating " +
                            " From ratings as r right outer join  movies as m on r.movieId = m.id " +
                            " Where title like '" + genre + "%' " + sort;
                    count_query = "select count(*)as num from (" + query + ") as t";
                    query += " limit " + limit + " offset " + offset_num;
                }
            }
            else{// browse by genre
                String t = " (select id from genres g where g.name = '" +genre+  "') as t ";
                t2 = " ( select distinct(m.id), m.title, m.year, m.director, r.rating ";
                t2 += "  From ratings as r right outer join  movies as m on r.movieId = m.id , genres_in_movies as gm, " +t;
                t2 += " Where gm.genreId = t.id and gm.movieId = m.id ";
                count_query = " Select count(id) as num " +" From " + t2 +" ) as t2 ";
                t2 += " limit " + limit + " offset " + offset_num +") as t2 ";
                query += " select * from " + t2 + sort;
            }

        }
        else if (star_name =="") { // if the search form does't have star
            query = " Select id, title, year, director, rating " +
                    " From ratings as r right outer join  movies as m on r.movieId = m.id " +
                    input + sort+
                    " limit " + limit + " offset " + offset_num;
            count_query =" Select count(id) as num" +
                    " From ratings as r right outer join  movies as m on r.movieId = m.id " +  input;
        }
        else{// the search form at least contain a star section
            String t = " (select id from stars s where s.name like '%" +star_name+  "%') as t ";
            t2 = " ( select distinct(m.id), m.title, m.year, m.director, r.rating ";
            t2 += "From ratings as r right outer join  movies as m on r.movieId = m.id , stars_in_movies as sm, " +t;
            if (input == "") t2 += " Where sm.starId = t.id and sm.movieId = m.id";
            else t2 += input + " and sm.starId = t.id and sm.movieId = m.id ";
            count_query = " Select count(id) as num " +" From " + t2 +" ) as t2";
            t2 += " limit " + limit + " offset " + offset_num + " ";
            query = " select * from " +t2 + " ) as t2"+ sort;
        }
        //System.out.println(count_query);
        //System.out.println(query);
        ResultSet rs = statement.executeQuery(query);
        JsonArray result = new JsonArray();
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
            result.add(jsonObject);
        }
        rs.close();
        ResultSet rs2 = statement.executeQuery(count_query);
        if(rs2.next()){
            count =  rs2.getInt("num");
        }
        max_num = count;
        statement.close();
        //System.out.println("count:"+ count);
        return result;
    }

//    private JsonArray getMovieList(Connection connection, int limit, int offset_num) throws SQLException {
//        Statement statement = connection.createStatement();
//        String query = String.format("Select m.id, m.title, m.year, m.director, r.rating " +
//                "From ratings as r, movies as m " +
//                "Where r.movieId = m.id " +
//                "Order by rating desc, numVotes desc " +
//                "limit %d offset %s", limit , offset_num);
//        ResultSet rs = statement.executeQuery(query);
//        JsonArray result = new JsonArray();
//        while (rs.next()) {
//            String movie_id = rs.getString("id");
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("movie_id", movie_id);
//            jsonObject.addProperty("movie_title", rs.getString("title"));
//            jsonObject.addProperty("movie_year", rs.getString("year"));
//            jsonObject.addProperty("movie_director", rs.getString("director"));
//            jsonObject.addProperty("movie_rating", rs.getString("rating"));
//
//            JsonArray genres = getGenres(connection, movie_id, genre_limit);
//            jsonObject.add("movie_genre", genres);
//            JsonArray stars = getStars(connection, movie_id, star_limit);
//            jsonObject.add("movie_star", stars);
//
//            result.add(jsonObject);
//        }
//        rs.close();
//        statement.close();
//        return result;
//    }

    private JsonArray getGenres(Connection connection, String id, int limit) throws SQLException {
        Statement statement = connection.createStatement();
        JsonArray result = new JsonArray();
        // first three genre, alphabetical order.
        String query = String.format("select g.name " +
                "from genres as g, genres_in_movies as gm " +
                "where gm.movieId = '%s' and g.id = gm.genreId " +
                "order by name " +
                "limit %d", id, limit);
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            result.add(rs.getString("name"));
        }
        rs.close();
        statement.close();
        return result;
    }
    private JsonArray getDistinctGenres(Connection connection) throws  SQLException{
        Statement statement = connection.createStatement();
        JsonArray result = new JsonArray();
        String query = "SELECT distinct(name) FROM genres order by name";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            JsonObject star = new JsonObject();
            star.addProperty("genre_name", rs.getString("name"));
            result.add(star);
        }
        rs.close();
        statement.close();
        return result;
    }
    private JsonArray getStars(Connection connection, String id, int limit) throws SQLException {
        Statement statement = connection.createStatement();
        JsonArray result = new JsonArray();
        /*String query = String.format("select s.id, s.name " +
                "from stars as s, stars_in_movies as sm " +
                "where sm.movieId = '%s' and s.id = sm.starid " +
                "limit %d", id, limit);*/
        // first three stars, sorted by numbers of movies played, then alphabetical
//        String query = String.format("with S as (select s.id, s.name\n" +
//                "\tfrom stars as s, stars_in_movies as sm\n" +
//                "\twhere sm.movieId = '%s' and s.id = sm.starId)\n" +
//                "select S.*\n" +
//                "from S, stars_in_movies as sm\n" +
//                "where S.id = sm.starId\n" +
//                "group by S.id\n" +
//                "order by count(S.id) desc, S.name\n"+
//                "limit %d", id, limit);
        String query = String.format("select S.* " +
                "from (select s.id, s.name " +
                "from stars as s, stars_in_movies as sm " +
                "where sm.movieId = '%s' and s.id = sm.starId) " +
                "as S, stars_in_movies as sim  " +
                "where S.id = sim.starId  " +
                "group by S.id  " +
                "order by count(S.id) desc, S.name " +
                "limit %d", id, limit);
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
    private String process_search_input(String title, String year, String director){
        ArrayList<String> temp = new ArrayList<String>();
        String to_return = "";

        if (title != "")
            temp.add(" m.title LIKE '%" +title+"%'");
        if (year != "")
            temp.add(" m.year="+year+" ");
        if (director != "")
            temp.add(" m.director LIKE '%" +director+"%'");
        if (temp.size()==0)
            return to_return;

        for(int i=0; i<temp.size()-1;i++){
            to_return += temp.get(i) +" and ";
        }
        to_return+= temp.get(temp.size()-1);
        return " Where "+to_return;
    }
    private String change(String i){
        if (i.equals("1"))
            return "2";
        return "1";
    }
    private String get_sort(String i){
        String sort;
        int count=0;
        switch (i) {
            case "1":
                sort = " Order by rating desc,title desc ";break;
            case "2":
                sort = " Order by rating asc,title desc ";break;
            case "3":
                sort = " Order by rating desc,title asc ";break;
            case "4":
                sort = " Order by rating asc,title asc ";break;
            case "5":
                sort = " Order by title asc, rating asc";break;
            case "6":
                sort = " Order by title asc, rating desc ";break;
            case "7":
                sort = " Order by title desc, rating asc ";break;
            default://(i.equals("8"))
                sort = " Order by title desc, rating desc ";break;
        }

        return sort;
    }

//    private ArrayList<Object> get_query( int limit, int offset_num, String input, String star_name, String sortT,String genre){
//        // returns [(String)query, （int) count query]
//
//    }
}