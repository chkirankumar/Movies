package com.kiran.movies;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.kiran.movies.app.AppController;
import com.kiran.movies.model.CustomListAdapter;
import com.kiran.movies.model.Movie;
import com.potyvideo.library.AndExoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AndExoPlayerView andExoPlayerView;

    private String TEST_URL_MP4 = "https://demonuts.com/Demonuts/smallvideo.mp4";
    private static final String url = "https://videohm.000webhostapp.com/movies.json";
    private ProgressDialog pDialog;
    private List<Movie> movieList = new ArrayList<>();
    private ListView listView;
    private CustomListAdapter adapter;
    private int req_code = 129;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, movieList);
        listView.setAdapter(adapter);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");

        andExoPlayerView = findViewById(R.id.andExoPlayerView);

        if(isInternetOn()){
            loadMP4ServerSide(TEST_URL_MP4);

            JsonArrayRequest movieReq = new JsonArrayRequest(url,new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    hidePDialog();

                    for (int i = 0; i < response.length(); i++) {
                        try {

                            JSONObject obj = response.getJSONObject(i);
                            Movie movie = new Movie();
                            movie.setTitle(obj.getString("title"));
                            movie.setThumbnailUrl(obj.getString("image"));
                            movie.setRating(((Number) obj.get("rating"))
                                    .doubleValue());
                            movie.setVurl(obj.getString("videoUrl"));
                            JSONArray genreArry = obj.getJSONArray("genre");
                            ArrayList<String> genre = new ArrayList<String>();
                            for (int j = 0; j < genreArry.length(); j++) {
                                genre.add((String) genreArry.get(j));
                            }
                            movie.setGenre(genre);
                            movieList.add(movie);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    adapter.notifyDataSetChanged();
                }
            },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    hidePDialog();

                }
            });
            AppController.getInstance().addToRequestQueue(movieReq);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.releaseYear); //
                    String vURL = textView.getText().toString();
                    loadMP4ServerSide(vURL);
                }
            });
        }else{
            Toast.makeText(this,"Check Network Connection",Toast.LENGTH_SHORT).show();
        }
    }
    private void loadMP4ServerSide(String url) {
        andExoPlayerView.setSource(url);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
    private boolean isInternetOn() {
        ConnectivityManager connec =
                (ConnectivityManager)this.getSystemService(this.getApplicationContext().CONNECTIVITY_SERVICE);
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            return true;
        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            return false;
        }
        return false;
    }
}
