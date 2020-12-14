package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.Entities.Game;
import com.example.demo.Entities.User;
import com.example.demo.Executor.AppExecutor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Library extends AppCompatActivity {

    //Variables
    RecyclerView recyclerView;
    List<Game> games;
    String textFromInput;

    //Data
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        //Getting email from sharedPreferences
        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name), MODE_PRIVATE);
        final String email = sharedPreferences.getString(getString(R.string.pref_email), null);

        //Gets all the games of specific user
        final CountDownLatch latch = new CountDownLatch(1);
        AppExecutor.getInstance().diskIo().execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase db = UserDatabase.getInstance(getApplicationContext());
                games = db.getGameDao().getGameList(email);
                //Log.d("GamesOnClick", "For user : " + email + " Games : " + games.get(0).toString());
                latch.countDown();
            }
        });

        //Await database reading finish
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        //Declaring recyclerView
        recyclerView = findViewById(R.id.current_play_list);

        //If there are games in the list
        if (games != null) {

            //New Adapter
            MyAdapter2 myAdapter = new MyAdapter2(this, games, new MyAdapter2.MyAdapterListener() {

                /**
                 * RemoveButton Listener
                 * @param v View
                 * @param position Row position
                 * @param gameList List<Game>
                 */
                @Override
                public void addRemoveButtonOnClick(View v, int position, List<Game> gameList) {

                    Log.d("ButtonClicked", "Remove Button has beenClicked");
                    final Game game = gameList.get(position);

                    //Accessing database and deleting selected Game
                    final CountDownLatch latch = new CountDownLatch(1);
                    AppExecutor.getInstance().diskIo().execute(new Runnable() {
                        @Override
                        public void run() {
                            User user = UserDatabase.getInstance(getApplicationContext()).getUserDao().findByEmail(email);
                            int count = user.getTotalGames() - 1;
                            UserDatabase.getInstance(getApplicationContext()).getUserDao().updateTotalGames(email,count);
                            UserDatabase.getInstance(getApplicationContext()).getGameDao().deleteGame(game);

                            latch.countDown();
                        }
                    });
                    //Wait for database modifications
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                    }

                    //Reload Library when deleted from database
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }

                /**
                 * EditButton Listener
                 * @param v View
                 * @param position Row position
                 * @param gameList List<Game>
                 * @param hoursPlayed TextView
                 */
                @Override
                public void addEditButtonOnClick(View v, int position, List<Game> gameList, final TextView hoursPlayed) {

                    //Making alertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(Library.this);
                    builder.setTitle("Enter value of type 11:30");

                    //Getting title of the game
                    final String title = gameList.get(position).getTitle();

                    //Setting up the input
                    final EditText input = new EditText(Library.this);

                    //Specifying the type of input expected
                    input.setInputType(InputType.TYPE_CLASS_DATETIME);

                    //Center input text
                    input.setGravity(Gravity.CENTER);

                    //Setting up the buttons
                    //Empty OnClick because we need the dialog to stay for check when submit is clicked
                    builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    //Dismiss if negative is clicked
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    //Creating and showing dialog
                    builder.setView(input);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    //Setting Listener for submit button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //Get input for check
                            String forCheck = input.getText().toString();

                            //Start multiple checks of foram
                            try {
                                if (forCheck == null) {
                                    dialog.setTitle("Input is Empty!");
                                } else if (forCheck.isEmpty()) {
                                    dialog.setTitle("Input is Empty!");
                                } else if (!forCheck.contains(":")) {
                                    dialog.setTitle("Format should be hours:mm");
                                } else {
                                    int index = forCheck.indexOf(':');
                                    String minutes = forCheck.substring(index + 1);
                                    if (Integer.parseInt(minutes) > 59) {
                                        dialog.setTitle("Incorrect minutes");
                                    } else {

                                        //If everything is okay we can proceed and add the value to the database
                                        textFromInput = input.getText().toString();
                                        final CountDownLatch latch = new CountDownLatch(1);
                                        AppExecutor.getInstance().diskIo().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                UserDatabase.getInstance(getApplicationContext()).getGameDao().updateHoursPlayed(email, title, textFromInput);
                                                latch.countDown();
                                            }
                                        });

                                        //Wait for database modifications
                                        try {
                                            latch.await();
                                        } catch (InterruptedException e) {
                                        }

                                        //Setting TextView and closing dialog
                                        Log.d("DialogInput", textFromInput);
                                        hoursPlayed.setText(textFromInput);
                                        dialog.dismiss();
                                    }
                                }
                            }catch (Exception e){
                                dialog.setTitle("Incorrect Input!");
                            }

                        }
                    });
                }

                /**
                 * Listener for Add Hour Button
                 * @param v View
                 * @param position String position
                 * @param gameList List<Game>
                 * @param hoursPlayed TextView
                 */
                @Override
                public void addHourButtonOnClick(View v, int position, List<Game> gameList,TextView hoursPlayed) {

                    //Getting game title
                    final String title = gameList.get(position).getTitle();

                    //Getting hours from TextView
                    String hoursFromTextView = hoursPlayed.getText().toString();

                    //Getting position of splitter
                    //Getting hours and minutes separately
                    int index = hoursFromTextView.indexOf(':');
                    String hoursSubstring = hoursFromTextView.substring(0,index);
                    String minutesSubstring = hoursFromTextView.substring(index + 1);
                    Log.d("Buttons",minutesSubstring);

                    //Incrementing hours
                    int hours = Integer.parseInt(hoursSubstring) + 1;

                    //Additional String for formatting
                    String hoursFormatted;
                    String finalFormatted;

                    //If hours had 0 like 03 we add 0 back
                    if (hours < 10) {
                        hoursFormatted = "0" + hours;
                    }else {
                        hoursFormatted = hours + "";
                    }

                    //Adding them back
                    finalFormatted = hoursFormatted + ":" + minutesSubstring;
                    hoursPlayed.setText(finalFormatted);
                    Log.d("Buttons",finalFormatted);

                    //Doing the same operations as above but to the database
                    AppExecutor.getInstance().diskIo().execute(new Runnable() {
                        @Override
                        public void run() {
                            String hours = UserDatabase.getInstance(getApplicationContext()).getGameDao().getGame(email,title).getHoursPlayed();
                            int index = hours.indexOf(":");
                            String hoursSubstring = hours.substring(0,index);
                            String minuteSubstring = hours.substring(index + 1);
                            int hoursInt = Integer.parseInt(hoursSubstring) + 1;
                            String hoursFormatted;
                            String finalFormatted;
                            if(hoursInt < 10) {
                                hoursFormatted = "0" + hoursInt;
                                finalFormatted = hoursFormatted + ":" + minuteSubstring;
                                UserDatabase.getInstance(getApplicationContext()).getGameDao().updateHoursPlayed(email,title,finalFormatted);
                            } else {
                                hoursFormatted = hoursInt + "";
                                finalFormatted = hoursFormatted + ":" + minuteSubstring;
                                UserDatabase.getInstance(getApplicationContext()).getGameDao().updateHoursPlayed(email,title,finalFormatted);
                            }
                        }
                    });
                }

                @Override
                public void addRemoveHourButtonOnClick(View v, int position, List<Game> gameList,TextView hoursPlayed) {

                    //Getting title of the game
                    final String title = gameList.get(position).getTitle();

                    //Getting hours from TextView
                    String hoursFromTextView = hoursPlayed.getText().toString();

                    //Splitting between hours and minutes
                    int index = hoursFromTextView.indexOf(':');
                    String hoursSubstring = hoursFromTextView.substring(0,index);
                    String minutesSubstring = hoursFromTextView.substring(index + 1);
                    Log.d("Buttons",minutesSubstring);


                    //Checking hours to not be 0 so we don't go negative
                    if (Integer.parseInt(hoursSubstring) != 0) {

                        //Decrement hour
                        int hours = Integer.parseInt(hoursSubstring) - 1;

                        //Preparing for formatting
                        String hoursFormatted;
                        String finalFormatted;

                        //Adding possible missing 0 during conversion
                        if (hours < 10) {
                            hoursFormatted = "0" + hours;
                        } else {
                            hoursFormatted = hours + "";
                        }

                        //Putting back together and setting to TextView
                        finalFormatted = hoursFormatted + ":" + minutesSubstring;
                        hoursPlayed.setText(finalFormatted);
                        Log.d("Buttons", finalFormatted);

                        //Doing the same operation to the database value
                        AppExecutor.getInstance().diskIo().execute(new Runnable() {
                            @Override
                            public void run() {
                                String hours = UserDatabase.getInstance(getApplicationContext()).getGameDao().getGame(email, title).getHoursPlayed();
                                int index = hours.indexOf(":");
                                String hoursSubstring = hours.substring(0, index);
                                String minuteSubstring = hours.substring(index + 1);
                                int hoursInt = Integer.parseInt(hoursSubstring) - 1;
                                String hoursFormatted;
                                String finalFormatted;
                                if (hoursInt < 10) {
                                    hoursFormatted = "0" + hoursInt;
                                    finalFormatted = hoursFormatted + ":" + minuteSubstring;
                                    UserDatabase.getInstance(getApplicationContext()).getGameDao().updateHoursPlayed(email, title, finalFormatted);
                                } else {
                                    hoursFormatted = hoursInt + "";
                                    finalFormatted = hoursFormatted + ":" + minuteSubstring;
                                    UserDatabase.getInstance(getApplicationContext()).getGameDao().updateHoursPlayed(email, title, finalFormatted);
                                }
                            }
                        });
                    }

                }

                /**
                 * Completed Button Listener
                 * @param v View
                 * @param position int position
                 * @param gameList List<Game>
                 */
                @Override
                public void addCompletedButtonOnClick(View v, int position, List<Game> gameList) {

                    //Getting game title
                    final String title = gameList.get(position).getTitle();

                    //Accessing database
                    AppExecutor.getInstance().diskIo().execute(new Runnable() {
                        @Override
                        public void run() {

                            //Getting current completed status
                            String completed = UserDatabase.getInstance(getApplicationContext()).getGameDao().getGame(email, title).getCompleted();

                            //Getting current user
                            User user = UserDatabase.getInstance(getApplicationContext()).getUserDao().findByEmail(email);

                            //Assigning count for games completed
                            int count = user.getCompletedGames();

                            //Set completed and increment count
                            if (completed.equals("N")) {
                                count++;
                                UserDatabase.getInstance(getApplicationContext()).getUserDao().updateCompletedGames(email,count);
                                UserDatabase.getInstance(getApplicationContext()).getGameDao().updateCompletedStatus(email, title, "Y");
                            } else {

                                //Set uncompleted decrement count
                                count--;
                                UserDatabase.getInstance(getApplicationContext()).getUserDao().updateCompletedGames(email,count);
                                UserDatabase.getInstance(getApplicationContext()).getGameDao().updateCompletedStatus(email, title, "N");
                            }
                        }
                    });
                }
            });

            //Setting recyclerView
            recyclerView.setAdapter(myAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            //If no games in the librarry
            Toast.makeText(this, "This is a game library where you can track your progress!", Toast.LENGTH_SHORT).show();
        }



        //-----------------------------------Bottom Navigation--------------------------------------
        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Search Selected
        bottomNavigationView.setSelectedItemId(R.id.library);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.library:
                        return true;

                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        //-----------------------------------Bottom Navigation--------------------------------------
    }
}