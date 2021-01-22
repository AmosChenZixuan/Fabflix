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
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "MainPageServlet", urlPatterns = "/api/main-page")
public class MainPageServlet extends HttpServlet {
    private final static int genre_limit = 3, star_limit = 3;
    //private static int default_display_num =3;
    //private static int max_num = 100;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    {
        File myObj1 = new File("/home/ubuntu/query_time.txt");
        File myObj2 = new File("/home/ubuntu/server_time.txt");
        try {
            myObj1.createNewFile();
            myObj2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime_server = System.nanoTime();

        response.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = request.getSession(true);
        SearchData search_info = (SearchData) session.getAttribute("search_info");
        Integer page_num = (Integer) session.getAttribute("page_num");
        Integer display_num = (Integer) session.getAttribute("display_num");
        String sortType = (String) session.getAttribute("sortType");
        ArrayList<String> tempt = process_search_input(search_info);
        String fulltext_query = (String) session.getAttribute("fulltext_query");
        resultData.addProperty("page_num",page_num);
        int off_set_num =display_num*(page_num-1);

        //String is_browse = (String) session.getAttribute("browse");
        int mode = (int) session.getAttribute("main_page_mode"); // 0:browse, 1:search, 2:fulltext
        String genre;
        if(mode == 0)
            genre = (String) session.getAttribute("genre");
        else{genre = "";}

        try {
            Connection connection = dataSource.getConnection();

            if(mode < 2) { // browse or search
                long startTime_query = System.nanoTime();
                resultData.add("movieList", apply_search(request, connection, display_num, off_set_num, tempt, search_info, sortType, genre));
                long endTime_query = System.nanoTime();
                long elapsedTime_query = endTime_query - startTime_query;
                FileWriter myWriter = new FileWriter("/home/ubuntu/query_time.txt",true);
                myWriter.write(""+elapsedTime_query+"\n");
                myWriter.close();
            }
            else { // fulltext
                resultData.add("movieList", fulltext_search(request, connection, display_num, off_set_num, fulltext_query, sortType));
            }
            // remember and auto-refill user's search and sort
            if (mode == 1)
                resultData.add("search_info", search_info.toJsonObject());
            else if (mode == 2)
                resultData.addProperty("fulltext_query", fulltext_query);
            resultData.addProperty("sort_type", sortType);
            // display cart size
            Helper.writeCartSize(resultData, session);

            JsonObject movie_limit = new JsonObject();
            movie_limit.addProperty("movie_limit", display_num);
            resultData.add("movie_limit",movie_limit);
            resultData.add("genres",getDistinctGenres(connection));
            Helper.getUserInfo(resultData, request);
            response.setStatus(200);
            connection.close();
        } catch (Exception e) {
            resultData.addProperty("errorMessage", e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            System.out.println(resultData.toString());
        }
        PrintWriter out = response.getWriter();
        out.write(resultData.toString());
        out.close();
        long endTime_sever = System.nanoTime();
        long elapsedTime_query = endTime_sever - startTime_server;
        FileWriter myWriter = new FileWriter("/home/ubuntu/server_time.txt",true);
        myWriter.write(""+elapsedTime_query+"   0\n");// 1 is post request 0 is get request
        myWriter.close();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime_server = System.nanoTime();

        response.setContentType("application/json");
        HttpSession session = request.getSession(true);
        int page_num = (int) session.getAttribute("page_num");
        String action = request.getParameter("action");
        Integer display_num = (Integer) session.getAttribute("display_num");
        Integer query_count = (Integer) session.getAttribute("query_count");
        //System.out.println("action: "+action);
        switch(action){

            case "fulltext":
                page_num=1;
                String query = request.getParameter("query");
                session.setAttribute("main_page_mode", 2);
                session.setAttribute("fulltext_query", query);
                break;
            case "search":{
                page_num=1;
                String s = request.getParameter("star").trim();
                String d = request.getParameter("director").trim();
                String t = request.getParameter("title").trim();
                String y = request.getParameter("year").trim();

                //session.setAttribute("browse","false");
                session.setAttribute("main_page_mode", 1);
                SearchData data = new SearchData(s,d,t,y);
               // data.print();
                session.setAttribute("search_info",data);
                break;
            }
            case "next":
                int i =(page_num+1)*display_num;
                if (i<query_count+display_num)
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
            case"dis_num":{// get the display number
                int n = Integer.parseInt(request.getParameter("num"));
                session.setAttribute("display_num",n);
                page_num =1;
                break;
            }
            case"browse_genre":{
                String genre = request.getParameter("genre");
                session.setAttribute("genre",genre);

                //session.setAttribute("browse","true");
                session.setAttribute("main_page_mode", 0);
                page_num = 1;
                //System.out.println("got the browse_genre post:" +genre);
                break;
            }
        }
        session.setAttribute("page_num",page_num);
        long endTime_sever = System.nanoTime();
        long elapsedTime_query = endTime_sever - startTime_server;
        FileWriter myWriter = new FileWriter("/home/ubuntu/server_time.txt",true);
        myWriter.write(""+elapsedTime_query+"   1\n"); // 1 is post request 0 is get request
        myWriter.close();
    }


    private JsonArray fulltext_search(HttpServletRequest request, Connection connection, int limit, int offset_num, String query, String sortT) throws SQLException {
        query = query.trim();
        // Empty query
        if ("".equals(query)){
            request.getSession(true).setAttribute("query_count", 0);
            return new JsonArray();
        }
        String[] tokens = query.split(" +");
        String sort = get_sort(sortT);
        int count =0;
        ArrayList<String> queies = SqlQuery.fulltext_query_generator(tokens.length, sort);
        String search_query = queies.get(1);
        String count_query = queies.get(0);
//        System.out.println(search_query);
//        System.out.println(count_query);

        PreparedStatement statement_query = connection.prepareStatement(search_query); //statement for query
        PreparedStatement statement_count = connection.prepareStatement(count_query); //statement for count
        set_fulltext_prepare_statement(statement_query, tokens, query, limit, offset_num);
        set_fulltext_prepare_statement(statement_count, tokens, query,-1, -1);
        ResultSet rs = statement_query.executeQuery();
        ResultSet rs2 = statement_count.executeQuery();

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
        if (rs2.next())
            count = rs2.getInt("num");
        HttpSession session = request.getSession(true);
        session.setAttribute("query_count", count);
        statement_query.close();
        statement_count.close();
        return result;
    }

    private JsonArray apply_search(HttpServletRequest request, Connection connection, int limit, int offset_num, ArrayList<String> inputs,SearchData sd, String sortT,String genre)throws SQLException {
        String sort = get_sort(sortT);
        ArrayList<String> queries = SqlQuery.search_query_generator(inputs,sd,sort,genre);
        String query = queries.get(1);
        String count_query= queries.get(0);
        int count =0;
//        System.out.println(query);
//        System.out.println(count_query);
        PreparedStatement statement_query = connection.prepareStatement(query); //statement for query
        PreparedStatement statement_count = connection.prepareStatement(count_query); //statement for count
        set_prepare_statement(statement_query, sd, inputs, limit, offset_num, genre);
        set_prepare_statement(statement_count, sd, inputs, -1, -1,genre); //count statement does not have limit or offset
        ResultSet rs = statement_query.executeQuery();
        ResultSet rs2 = statement_count.executeQuery();

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
        if (rs2.next())
            count = rs2.getInt("num");
        HttpSession session = request.getSession(true);
        session.setAttribute("query_count", count);
        statement_query.close();
        statement_count.close();
        return result;
    }

    private void set_fulltext_prepare_statement(PreparedStatement statement, String[] inputs, String query, int limit, int offset_num) throws SQLException {
        int i=1;
        for(String input :inputs){
            statement.setString(i, input);
            i++;
        }
        // fuzzy search parameters
        int threshold = query.length()/4;
        statement.setString(i++, query);
        statement.setInt(i++, threshold);
        // limit offset
        if (limit != -1 || offset_num != -1) {
            statement.setInt(i, limit);
            statement.setInt(++i, offset_num);
        }
    }
    private void set_prepare_statement(PreparedStatement statement,SearchData sd, ArrayList<String> inputs,int limit, int offset_num, String genre) throws SQLException {
        //only for browsing and searching; it can't be used for fulltext search
        int i=1;
        if(genre.equals("")) {
            if (!sd.star.equals("")){
                statement.setString(i, sd.star);
                i++;
            }
            for(String s: inputs){ // m.title m.year m.director
                if( s.charAt(3) == 't')
                    statement.setString(i, sd.title);
                else if(s.charAt(3) == 'y')
                    statement.setString(i,sd.year);
                else if(s.charAt(3) == 'd')
                    statement.setString(i,sd.director);
                else --i;
                ++i;
            }
        }
        else{ //it is an browsing search
            if(genre.length() ==1 && genre.charAt(0) == '*') //it is an on letter browsing search
                // if it is *
                statement.setString(i, "^[^A-Za-z0-9]");
            // a-z 0-9 or genre name
            else statement.setString(i, genre);
            i++;
        }
        if (limit != -1 || offset_num != -1) {
            statement.setInt(i, limit);
            statement.setInt(++i, offset_num);
        }
    }
    private JsonArray getGenres(Connection connection, String id, int limit) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieGenre_id_limit);
        statement.setString(1, id);
        statement.setInt(2, limit);

        JsonArray result = new JsonArray();
        ResultSet rs = statement.executeQuery();
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

        ResultSet rs = statement.executeQuery(SqlQuery.getDistinctGenres);
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
        PreparedStatement statement = connection.prepareStatement(SqlQuery.getMovieStar_id_limit);
        statement.setString(1, id);
        statement.setInt(2, limit);

        JsonArray result = new JsonArray();
        ResultSet rs = statement.executeQuery();
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

    private ArrayList<String> process_search_input(SearchData sd){
        ArrayList<String> to_return = new ArrayList<String>();
        if (!sd.title.equals(""))
            to_return.add(" m.title LIKE CONCAT( '%',?,'%') ");
        if (!sd.year.equals(""))
            to_return.add(" m.year= ? ");
        if (!sd.director.equals(""))
            to_return.add(" m.director LIKE CONCAT( '%',?,'%') ");
        return to_return;
    }

    private String get_sort(String i){
        String sort;
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


}