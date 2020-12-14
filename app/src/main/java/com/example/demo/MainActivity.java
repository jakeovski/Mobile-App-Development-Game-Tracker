package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.demo.Entities.Game;
import com.example.demo.Entities.User;
import com.example.demo.Executor.AppExecutor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;




public class MainActivity extends AppCompatActivity {

    //Variables
    SearchView searchBar;
    RecyclerView recyclerView;
    String [] names;
    Integer[] coversId2;
    String coversId2String;
    String[] imageUrls;
    String[] imageUrlsFormatted;
    boolean success = false;
    boolean copyOfTheGame;

    //Data
    private SharedPreferences sharedPreferences;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name),MODE_PRIVATE);

        //-----------------------------API WORK-------------------------------

        //Setting listener for search bar
        searchBar = findViewById(R.id.search_input);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {

                //If query is empty
                if (query.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"You didn't input anything",Toast.LENGTH_LONG);
                }
                else {
                    //Log.d("API TAG",query);

                    //Make a post request to API
                    AndroidNetworking.post("https://api.igdb.com/v4/games")
                            .addHeaders("Client-ID","c6hmrv9uoe670pghf3rko1lp98f19w")
                            .addHeaders("Authorization","Bearer jpghi83ussw32kn35u5lv3rre9fmfi")
                            .addHeaders("Content-Type","application/json")
                            .addStringBody("search \"" + query + "\";fields name,cover;")
                            .setPriority(Priority.HIGH)
                            .build().getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            success = true;
                            //Log.d("API WORK","WORKING " + response);

                            names = new String[response.length()];

                            //If no result notify user
                            if(names.length == 0) {
                                Toast.makeText(getApplicationContext(),"We couldn't find that game:(",Toast.LENGTH_LONG);
                            }else{

                                //Format response
                                coversId2 = new Integer[response.length()];
                                imageUrls = new String[response.length()];
                                coversId2String = "(";
                                imageUrlsFormatted = new String[imageUrls.length];

                                //Separating JSON
                                for(int i = 0;i < response.length();i++) {
                                    try {
                                        //Log.d("API Response", response.getJSONObject(i).get("name").toString());
                                        names[i] = response.getJSONObject(i).get("name").toString();
                                        coversId2[i] = Integer.parseInt(response.getJSONObject(i).get("cover").toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Log.d("API NAMES ARRAY",Arrays.toString(names));
                                Log.d("API NAMES ARRAY",Arrays.toString(coversId2));

                                //If no cover for the game provided use a default one
                                for(int f = 0;f<coversId2.length;f++) {
                                    if(coversId2[f] == null) {
                                        coversId2[f] = 22404;
                                    }
                                }

                                //Move all covers together in String array for further reques
                                for(int t = 0;t< coversId2.length;t++) {
                                    if(coversId2[t] == null) {
                                        coversId2String += "22404,";
                                    }else if (t == coversId2.length -1) {
                                        coversId2String += coversId2[t] + ");";
                                    }else {
                                        coversId2String += coversId2[t] + ",";
                                    }

                                }
                                Log.d("API COVERS STRING",coversId2String);

                                //Another POST request for covers
                                AndroidNetworking.post("https://api.igdb.com/v4/covers")
                                        .addHeaders("Client-ID","c6hmrv9uoe670pghf3rko1lp98f19w")
                                        .addHeaders("Authorization","Bearer f07zmi7efevkjyica6raq6h456sr3z")
                                        .addHeaders("Content-Type","application/json")
                                        .addStringBody("fields url;where id = " + coversId2String)
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .getAsJSONArray(new JSONArrayRequestListener() {
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                Log.d("API SECOND","Success");
                                                Log.d("API SECOND",Arrays.toString(coversId2));

                                                //Getting covers Urls
                                                for (int f = 0;f<response.length();f++) {
                                                    for(int h = 0;h<response.length();h++) {
                                                        try {
                                                            if(response.getJSONObject(f).get("id").toString().equals(Integer.toString(coversId2[h]))){
                                                                imageUrls[h] = response.getJSONObject(f).get("url").toString();
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                Log.d("API SECOND IMAGE URLS",Arrays.toString(imageUrls));

                                                //If no url use a sample one
                                                for(int i = 0; i< imageUrls.length;i++) {
                                                    if(imageUrls[i] == null) {
                                                        imageUrls[i] = "//images.igdb.com/igdb/image/upload/t_thumb/nm6sw8wl93rb8wsfa3nx.jpg";
                                                    }
                                                }

                                                //Replace thumb in url to cover_big for more bigger high res image
                                                for(int l = 0; l <imageUrls.length;l++) {
                                                    String formatted = imageUrls[l].replace("thumb","cover_big");
                                                    formatted = "https:" + formatted;
                                                    imageUrlsFormatted[l] = formatted;
                                                }
                                                Log.d("API FORMATTED",Arrays.toString(imageUrlsFormatted));

                                                //Assign RecyclerView
                                                recyclerView = findViewById(R.id.recyclerView);
                                                MyAdapter myAdapter = new MyAdapter(getApplicationContext(), names, imageUrlsFormatted, new MyAdapter.MyAdapterListener() {

                                                    @Override
                                                    public void addButtonOnClick(final View v, final int position, String[] names, String[] imageUrls) {
                                                        Log.d("GamesOnClick","Button was clicked at position " + position);
                                                        Log.d("GamesOnClick","Game title clicked is " + names[position]);
                                                        Log.d("GamesOnClick","Game Image Link is " + imageUrls[position]);

                                                        //Getting current user email
                                                        final String email = sharedPreferences.getString(getString(R.string.pref_email),null);

                                                        //Creating new game
                                                        final Game game = new Game(names[position],imageUrls[position],email);

                                                        //Access Database
                                                        final CountDownLatch latch = new CountDownLatch(1);
                                                        AppExecutor.getInstance().diskIo().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Log.d("GamesOnClick","Current User Email " + email);
                                                                UserDatabase db = UserDatabase.getInstance(getApplicationContext());

                                                                //Checks if the game is already in the database
                                                                Game gameToCompare = UserDatabase.getInstance(getApplicationContext()).getGameDao().getGame(email,game.getTitle());

                                                                //If no such game in the current database then add
                                                                if (gameToCompare == null) {
                                                                    copyOfTheGame = false;
                                                                    db.getGameDao().insertGame(game);
                                                                    User user = db.getUserDao().findByEmail(email);
                                                                    int count = user.getTotalGames() + 1;
                                                                    UserDatabase.getInstance(getApplicationContext()).getUserDao().updateTotalGames(email,count);
                                                                    Log.d("GamesOnClick","User successfully saved a game to database");
                                                                } else {
                                                                    copyOfTheGame = true;
                                                                }
                                                                latch.countDown();

                                                            }
                                                        });

                                                        //Wait until database finishes its operations
                                                        try {
                                                            latch.await();
                                                        }catch (InterruptedException e) {}

                                                        //If already in the database notify user
                                                        if(copyOfTheGame){
                                                            Toast.makeText(getApplicationContext(),"You already have this game in library",Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            //Change button to visually notify that game was added
                                                            v.setEnabled(false);
                                                            v.setBackgroundColor(Color.rgb(127,255,0));
                                                        }

                                                    }

                                                });

                                                //Setting adapter
                                                recyclerView.setAdapter(myAdapter);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                            }

                                            @Override
                                            public void onError(ANError anError) {
                                                Log.d("API SECOND","Unsuccessful");
                                            }
                                        });
                            }
                            }


                        @Override
                        public void onError(ANError anError) {
                            Log.d("API WORK","NOT WORKING " + anError );
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        /////--------------------------------------API WORKS-----------------------------------------------------


        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Search Selected
        bottomNavigationView.setSelectedItemId(R.id.search);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(),Library.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.search:
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

}