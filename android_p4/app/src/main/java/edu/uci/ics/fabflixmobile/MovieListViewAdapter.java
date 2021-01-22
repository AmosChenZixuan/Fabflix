package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> movies;


    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.row, movies);
        this.movies = movies;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row, parent, false);

        Movie movie = movies.get(position);

        TextView title = view.findViewById(R.id.Title);
        TextView year = view.findViewById(R.id.year);
        TextView director = view.findViewById(R.id.director);
        TextView genre = view.findViewById(R.id.genres);
        TextView star= view.findViewById(R.id.stars);
        TextView rating = view.findViewById(R.id.rating);

        title.setText(movie.getName());
        year.setText("\n"+ movie.getYear());// need to cast the year to a string to set the label
        director.setText('\n'+"Director:\n"+movie.director);
        genre.setText('\n'+ " Genres:"+movie.genres);
        star.setText(" Stars:" +movie.stars);
        rating.setText("Rating:"+movie.rating);

        return view;
    }

}