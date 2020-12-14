package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.Entities.User;
import com.example.demo.Executor.AppExecutor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;



public class SignUp extends AppCompatActivity {
    //Variables
    private Button register;
    private EditText mUsername,mPassword, mEmail;
    private TextView profileUsername;
    private String username,email,password;

    //Data variables
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Declaring fields
        register = findViewById(R.id.register_button);
        mUsername = findViewById(R.id.register_username);
        mPassword = findViewById(R.id.register_password);
        mEmail = findViewById(R.id.register_email);
        profileUsername = findViewById(R.id.profile_username);
        mAuth = FirebaseAuth.getInstance();

        // Register Button onClick Listener
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Getting inputs
                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                username = mUsername.getText().toString().trim();

                //Performing checks
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required");
                    return;
                }
                if (!isValidEmail(email)){
                    mEmail.setError("Enter a valid email");
                }
                if(TextUtils.isEmpty(username)) {
                    mUsername.setError("Username is required");
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required");
                    return;
                }
                if (password.length() < 8) {
                    mPassword.setError("Password must be at least 8 characters");
                    return;
                }

                //Register user to Firebase Database
                mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (!task.isSuccessful()) {
                                        //If not successful
                                        Toast.makeText(SignUp.this, "Authentication failed." + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Authentication successful
                                        Toast.makeText(SignUp.this, "Registration completed",
                                                Toast.LENGTH_SHORT).show();

                                        //Loading sharedPreferences
                                        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name), MODE_PRIVATE);

                                        //Saving user to database
                                        saveToDatabase(email,username,password);

                                        //Moving back to Login
                                        startActivity(new Intent(SignUp.this, Login.class));
                                        finish();
                                    }
                                }
                        });

            }
        });




    }

    public void onPause() {
        super.onPause();

        // Write to sharedPreferences when pausing this activity
        //If Authentication is successful
        if (sharedPreferences != null) {

            //Log.d("Login", "Writing in sharedPreferences" + username);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            // Sets current user
            Log.d("Login","Setting email for current person: " + email);
            prefsEditor.putString(getString(R.string.pref_email),email);

            // Add user to shared preferences
            Log.d("Login","Setting pair for User" + email + " : " + username );
            prefsEditor.putString("User"+email,username);
            Log.d("Login","Setting pair for " + email + " : " + password);
            prefsEditor.putString(email,password);
            prefsEditor.apply();
        }
    }

    /**
     * Convinience method for saving user to database
     * @param email String email
     * @param username String username
     * @param password String password
     */
    public void saveToDatabase(final String email, final String username, final String password){

        AppExecutor.getInstance().diskIo().execute(new Runnable() {
            @Override
            public void run(){
                User user = new User(email,username,password);
                UserDatabase.getInstance(getApplicationContext()).getUserDao().insertUser(user);
                //Toast.makeText(SignUp.this,"User successfully inserted",Toast.LENGTH_LONG).show();
                Log.d("DatabaseUser","User successfully saved to database");
            }
        });

    }

    /**
     * Method that checks if the email is valid
     * @param target Email
     * @return true - if valid/ false - if not
     */
    public static boolean isValidEmail(CharSequence target) {
        if(target == null) {
            return false;
        }else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }

    }

    /**
     * Listener that moves back to Login screen if Already login button is clicked
     * @param view
     */
    public void alreadyHaveAccountOnClick(View view) {
        startActivity(new Intent(SignUp.this,Login.class));
    }
}
