package main.java;

import java.util.ArrayList;

public class Movie {
    public String title, director;
    public int year;
    ArrayList<String> genres;

    public Movie(String t, String d, int y){
        title = t;
        director =d;
        year = y;
        genres = new ArrayList<>();
    }
    void set_genre(ArrayList<String> gs){
        genres = gs;
    }
    public String toString(){
        String gen =" genres:";
        for(String g: genres)
            gen+=g + "-";
        return "<--- Movie title:" + title+ " director:" +director+" year:" +year + gen + "--->";
    }

    public boolean is_valid(){ // if title=null or director =null or year =-1 or genre size=0 invalid movie
        return title!=null && director !=null && year != -1 && !genres.isEmpty();
    }

}
