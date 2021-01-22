package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SingleMovieActivity extends Activity {
    private TextView title;
    private TextView year_genre;
    private TextView actor;
    private Button main_btn;
    private Button list_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_movie_layout);
        Bundle bundle = getIntent().getExtras();
        String response = bundle.getString("response");
        title = findViewById(R.id.single_title);
        year_genre = findViewById(R.id.single_genre_year);
        actor = findViewById(R.id.single_actor);
        main_btn = findViewById(R.id.single_main_btn);
        list_btn = findViewById(R.id.single_list_btn);
        load_response(response);
        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_search_page();
            }
        });
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_list_page(bundle.getString("search_results"));
            }
        });
    }

    private void back_to_search_page(){
        final Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
    private void back_to_list_page(String search_results){
        final Intent search_intent = new Intent(this, ListViewActivity.class);
        search_intent.putExtra("search_results",search_results);
        startActivity(search_intent);
    }
    private void load_response(String response){
        try {
            String y_g="";
            String star="Cast:\n";
            JSONObject jsonobject = new JSONObject(response);
            String t = jsonobject.getString("movie_title");

            if(jsonobject.has("movie_year"))
                y_g+= "Release Year:"+jsonobject.getString("movie_year")+"\n\n";
            else y_g+= "\nRelease Year:: NA";

            y_g+= "Director:"+jsonobject.getString("movie_director")+"\n\n";
            JSONArray genres = jsonobject.getJSONArray("movie_genre");
            y_g+= "Genres:\n";
            for(int j=0; j <genres.length(); j++){
                y_g+= genres.getString(j) +'\n';
            }

            if(jsonobject.has("movie_rating"))
                y_g+= "\nRating:"+jsonobject.getString("movie_rating")+"\n";
            else  y_g+= "\nRating: NA";

            if(jsonobject.has("movie_star")){
                JSONArray stars = jsonobject.getJSONArray("movie_star");
                for(int j=0; j <stars.length(); j++){
                    star+= stars.getJSONObject(j).getString("star_name")+'\n';
                }
            }

            title.setText(t);
            year_genre.setText(y_g);
            actor.setText(star);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
