package edu.uci.ics.fabflixmobile;

public class Movie {
    public String name;
    public short year;
    public String genres;
    public String stars;
    public String director;
    public String id;
    public String rating;

    public Movie(String id, String name, short year, String genres, String stars,String director,String rating) {
        this.rating = rating;
        this.genres = genres;
        this.id = id;
        this.stars = stars;
        this.director = director;
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }
    public short getYear() {
        return year;
    }
    public String toString(){return "name:" +name + " year:"+year;}
}