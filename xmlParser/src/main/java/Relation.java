package main.java;

public class Relation {
    String movie_id, movie_title, star_name;
    public Relation(String id, String t, String sn){
        movie_id = id;
        movie_title = t;
        star_name = sn;
    }

    public String toString(){
        return "<--- Relation movie id:" +movie_id +" movie title:" + movie_title +" star name:" +star_name + "--->";
    }
    public boolean is_valid(){
        return star_name!=null && movie_id!=null && movie_title!=null;
    }
}
