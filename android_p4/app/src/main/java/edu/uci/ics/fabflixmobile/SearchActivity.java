package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends Activity {
    private String url= WebpageURL.main_page_url;
    private Button search_btn;
    private TextView search_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);
        search_btn = (Button) findViewById(R.id.search_btn);
        search_text = findViewById(R.id.search_text);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (search_text.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "empty input", Toast.LENGTH_SHORT).show();
                else{
                    Toast.makeText(getApplicationContext(), search_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    post_the_search();
                    redirect_to_movie_list();
                }

            }
        });

        search_text.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (search_text.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(), "empty input", Toast.LENGTH_SHORT).show();
                    else{
                        Toast.makeText(getApplicationContext(), search_text.getText().toString(), Toast.LENGTH_SHORT).show();
                        post_the_search();
                        redirect_to_movie_list();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void post_the_search(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("search.success", response);
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
                final Map<String, String> params = new HashMap<>();
                params.put("action", "fulltext");
                params.put("query", search_text.getText().toString());
                return params;
            }
        };
        queue.add(loginRequest);
    }
    private void redirect_to_movie_list(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final Intent search_intent = new Intent(this, ListViewActivity.class);
        final StringRequest loginRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("search.success", response);
                search_intent.putExtra("search_results", response);
                startActivity(search_intent);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                }) {
        };
        queue.add(loginRequest);
    }

}
