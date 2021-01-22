package main.java;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MovieParser {
    public HashMap<String, Movie> ml;
    Document dom;

    MovieParser(){
        ml = new HashMap<>();
    }

    public void run(){
        parseXmlFile();
        parseDocument();
    }

    private void parseXmlFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse("mains243.xml");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private void parseDocument() {// if title="" or director ="" or year =-1 or genre="" invalid movie
        Element docEle = dom.getDocumentElement();
        NodeList directorfilms = docEle.getElementsByTagName("directorfilms");
        int c = 0;
        for(int i=0; i< directorfilms.getLength(); i++){
            Element director = (Element) directorfilms.item(i);

            String director_name =get_movie_director_name(director);
            NodeList movies = director.getElementsByTagName("film");
            int movie_num = movies.getLength(); // get the total number of movies that this director has

            for(int j=0; j<movie_num; j++){
                Element movie = (Element) movies.item(j); // one movie from this director
                String title = get_movie_title(movie);
                int year = get_movie_year(movie);
                Movie temp_movie = new Movie(title,director_name, year);
                temp_movie.set_genre(get_movie_genres(movie));
                if(!temp_movie.is_valid()){
                    System.err.println("Inconsistent Movie: " + temp_movie.toString());
                    c++;
                }
                else
                    ml.put(get_movie_id(movie), temp_movie);
//                if(get_movie_id(movie).equals("GgL3"))
                    //System.out.println(temp_movie.toString());
            }
        }
        System.err.println("Inconsistent records in main.xml: " + c);
    }
    private String get_movie_id(Element el){
        String id;
        try{
            NodeList movieid = el.getElementsByTagName("fid");
            Element d = (Element) movieid.item(0);
            id =  d.getFirstChild().getNodeValue();
        }
        catch (NullPointerException e){
            NodeList movieid = el.getElementsByTagName("filmed");
            Element d = (Element) movieid.item(0);
            id =  d.getFirstChild().getNodeValue();
            return id;
        }
        return id;

    }

    private String get_movie_title(Element el){
        NodeList title = el.getElementsByTagName("t");
        Element t = (Element) title.item(0);
        try{
            String d =  t.getFirstChild().getNodeValue();
            return d;
        }
        catch (NullPointerException e){
            return null; //the movie does not have a title
        }

    }
    private int get_movie_year(Element el){
        NodeList year = el.getElementsByTagName("year");
        Element y = (Element) year.item(0);
        try{
            int n = Integer.parseInt(y.getFirstChild().getNodeValue());
            return n;
        }
        catch(NumberFormatException e){
            return -1;
        }
    }
    private String get_movie_director_name(Element el){
        NodeList director = el.getElementsByTagName("dirname");
        if(director.getLength() == 1){
            Element n = (Element) director.item(0);
            return n.getFirstChild().getNodeValue();
        }
        else{
            director = el.getElementsByTagName("dirn");
            Element n = (Element) director.item(0);
            return n.getFirstChild().getNodeValue();
        }
//        return null;
    }
    private ArrayList<String> get_movie_genres(Element movie){

        ArrayList<String> to_return = new ArrayList<>();
        int num = movie.getElementsByTagName("cat").getLength();
        NodeList name = movie.getElementsByTagName("cat");

        for(int i=0; i<num; ++i) {
            Element n = (Element) name.item(i);
            try{
                to_return.add(n.getFirstChild().getNodeValue());
            }
            catch (NullPointerException e){
            }
        }
        if(to_return.size() == 0){
            to_return.add("uncategorized");
        }
        return to_return;
    }
    public static void main(String[] args) {
        MovieParser p = new MovieParser();
        p.run();
    }
}
