package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.Entities.User;
import com.example.demo.Executor.AppExecutor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.concurrent.CountDownLatch;

public class Profile extends AppCompatActivity {

    //Variables
    private Button btnLogOut,changeProfileImage,deleteUserData,nukeAll;
    private TextView profileUsername, profileEmail;
    private String email,userId;
    private TextView totalGames, completedGames;
    private int totalGamesInt, completedGamesInt;
    private ImageView imageView;

    //Data
    StorageReference storageReference;
    private Uri profileImage;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //View assignment
        btnLogOut = findViewById(R.id.profile_logout);
        deleteUserData = findViewById(R.id.profile_delete_all_button);
        profileUsername = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_email);
        nukeAll = findViewById(R.id.profile_wipe_all_data);
        totalGames = findViewById(R.id.profile_total_games);
        completedGames = findViewById(R.id.profile_completed_games);
        imageView = findViewById(R.id.profile_image);
        changeProfileImage = findViewById(R.id.profile_edit_profile_image);

        //Data
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //Getting Data from sharedPreferences
        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name), MODE_PRIVATE);
        email = sharedPreferences.getString(getString(R.string.pref_email), null);
        Log.d("Login", "Profile: Getting email ... - " + email);
        final String username = sharedPreferences.getString("User" + email, null);
        Log.d("Login", "Profile: Username received from " + email + " : " + username);

        //Setting username and email
        profileEmail.setText(email);
        profileUsername.setText(username);

        //Accessing Database
        final CountDownLatch textLatch = new CountDownLatch(1);
        AppExecutor.getInstance().diskIo().execute(new Runnable() {
            @Override
            public void run() {
                String imageUriTemp = UserDatabase.getInstance(getApplicationContext()).getUserDao().findByEmail(email).getProfile_image();
                if (imageUriTemp != null) {
                    Log.d("Testing", imageUriTemp);
                    profileImage = Uri.parse(imageUriTemp);
                }
                totalGamesInt = UserDatabase.getInstance(getApplicationContext()).getUserDao().findByEmail(email).getTotalGames();
                completedGamesInt = UserDatabase.getInstance(getApplicationContext()).getUserDao().findByEmail(email).getCompletedGames();
                textLatch.countDown();
            }
        });

        //Wait until database operations finish
        try {
            textLatch.await();
        } catch (Exception e) {
        }

        //If was set up before load it
        if (profileImage != null) {
            Picasso.get().load(profileImage).into(imageView);
        }

        //Set count from database
        totalGames.setText("Total Games: " + totalGamesInt);
        completedGames.setText("Completed: " + completedGamesInt);


        //Sets listener change of state in authentication
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // launch login activity
                    startActivity(new Intent(Profile.this, Login.class));
                    finish();
                }
            }
        });

        // On click listener for the logout button
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    auth.signOut();
                } else {
                    startActivity(new Intent(Profile.this, Login.class));
                    finish();
                }
            }
        });

        //Listener for delete User data button
        deleteUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check internet connection
                if (!isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), "You have to be connected to internet to delete all your data!", Toast.LENGTH_LONG);
                } else {

                    //Access firebase to delete user
                    String password = sharedPreferences.getString(email, null);
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("DeleteButton", "Reauthentication complete, Deleting user...");
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("DeleteButton", "User Successfully Deleted, Proceding to sharedPreferences");
                                                        AppExecutor.getInstance().diskIo().execute(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                //Removes user sharedPreferences
                                                                sharedPreferences.edit().remove(email).commit();
                                                                sharedPreferences.edit().remove("User" + email).commit();
                                                                Log.d("DeleteButton", "SharedPreferences Deleted, Proceiding to Database");

                                                                //Remove user from database
                                                                UserDatabase db = UserDatabase.getInstance(getApplicationContext());
                                                                User userToDelete = db.getUserDao().findByEmail(email);
                                                                db.getUserDao().delete(userToDelete);
                                                                Log.d("DeleteButton", "User successfully deleted from Database");
                                                            }
                                                        });
                                                        Log.d("DeleteButton", "Signing Out");

                                                        //Sign Out after that
                                                        auth.signOut();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });

        //Listener for wipe all data
        nukeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Access Database and delete everything
                AppExecutor.getInstance().diskIo().execute(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor prefsEdit = sharedPreferences.edit();
                        prefsEdit.clear();
                        prefsEdit.apply();
                        UserDatabase db = UserDatabase.getInstance(getApplicationContext());
                        db.getUserDao().nukeTableUser();
                        db.getGameDao().nukeTableGame();
                        Log.d("GamesOnClick", "Wiped all data successfully");
                    }
                });
            }
        });


        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Search Selected
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(), Library.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        return true;
                }
                return false;
            }
        });

        //Listener for change Profile Image Button
        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                //Open Gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

    }

    //After user has chosen image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK) {

                //Get Uri
                Uri imageUri = data.getData();

                //Uri to string to upload to firebase
                imageView.setImageURI(imageUri);

                //save image in firebase
                uploadImageToFirebase(imageUri);
            }
        }

    }

    //
    private void uploadImageToFirebase(Uri imageUri){
        //upload image to firebase storage
        final StorageReference fileReference = storageReference.child(email + ".jpg");
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profileImage = uri;
                        final String profileImageString = profileImage.toString();
                        AppExecutor.getInstance().diskIo().execute(new Runnable() {
                            @Override
                            public void run() {
                                UserDatabase.getInstance(getApplicationContext()).getUserDao().updateProfileImage(email,profileImageString);
                            }
                        });
                    }
                });
                //Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Upload Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that checks if the user has Internet connection or no connection
     *
     * @return true - if connected to the internet /false - if not
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}


