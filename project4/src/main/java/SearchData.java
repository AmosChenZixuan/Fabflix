package main.java;

import com.google.gson.JsonObject;

public class SearchData{
    public String star, director, title, year;

    SearchData(String s, String d, String t, String y){
        star =s;
        director=d;
        year=y;
        title=t;
    }
    SearchData(){
        star ="";
        director="";
        year= "";
        title="";
    }

    public JsonObject toJsonObject(){
        JsonObject self = new JsonObject();
        self.addProperty("star", star);
        self.addProperty("director", director);
        self.addProperty("title", title);
        self.addProperty("year", year);
        return self;
    }

    void print(){
        System.out.println("---title:"+title+" year:"+year+" star:"+ star+" director:"+ director);
    }
    boolean empty(){
        return star.equals("") && director.equals("") && year.equals("") && title.equals("");
    }

}

