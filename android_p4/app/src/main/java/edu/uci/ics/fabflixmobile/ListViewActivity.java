package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewActivity extends Activity {
    private String url = WebpageURL.main_page_url;
    private String single_movie_url = WebpageURL.single_movie_url;
    private ArrayList<Movie> movies;
    private Button main_btn;
    private Button pre_btn;
    private Button next_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        Bundle bundle = getIntent().getExtras();
        String respond = bundle.getString("search_results");
        movies = new ArrayList<>();

        try {
            JSONObject jsonobject = new JSONObject(respond);
            JSONArray ml = jsonobject.getJSONArray("movieList");
            for(int i=0; i<ml.length();i++){
                JSONObject m = ml.getJSONObject(i);
                String title = m.getString("movie_title");
                short year = (short) Integer.parseInt(m.getString("movie_year"));
                String id = m.getString("movie_id");
                String rate = m.getString("movie_rating");
                String director = m.getString("movie_director");
                String star = "\n";
                JSONArray stars = m.getJSONArray("movie_star");
                for(int j=0; j <stars.length(); j++){
                    star+= stars.getJSONObject(j).getString("star_name")+'\n';
                }
                String genre = "\n";
                JSONArray genres = m.getJSONArray("movie_genre");
                for(int j=0; j <genres.length(); j++){
                    genre+= genres.getString(j) +'\n';
                }
                movies.add(new Movie(id,title,year, genre,star,director,rate));
            }
            // set page num
            int page_num = jsonobject.getInt("page_num");
            ((TextView)findViewById(R.id.page_num)).setText(page_num+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        main_btn = findViewById(R.id.main_btn);
        pre_btn = findViewById(R.id.pre_btn);
        next_btn = findViewById(R.id.nxt_btn);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                jump_to_single_movie(movie.id, respond);
//                String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_search_page();
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next_page_post();
                page_refresh();
            }
        });
        pre_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pre_page_post();
                page_refresh();
            }
        });

    }
    private void jump_to_single_movie(String id,String search_result){
        single_movie_post(single_movie_url+id,search_result);
    }
    private void single_movie_post(String url, String search_result){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final Intent intent = new Intent(this, SingleMovieActivity.class);
        final StringRequest loginRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               // System.out.println(response);
                intent.putExtra("response", response);
                intent.putExtra("search_results",search_result);
                startActivity(intent);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("action", "next");
                return params;
            }
        };
        queue.add(loginRequest);
    }


    private void back_to_search_page(){
        final Intent search_intent = new Intent(this, SearchActivity.class);
        startActivity(search_intent);
    }
    private void next_page_post(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("action", "next");
                return params;
            }
        };
        queue.add(loginRequest);
    }
    private void pre_page_post(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("action", "pre");
                return params;
            }
        };
        queue.add(loginRequest);
    }
    private void page_refresh(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final Intent search_intent = new Intent(this, ListViewActivity.class);
        final StringRequest loginRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                search_intent.putExtra("search_results", response);
                startActivity(search_intent);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("login.error", error.toString());
                    }
                }) {
        };
        queue.add(loginRequest);
    }
}