package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity {

    //Variables
    private Button SignIn,SignUp;
    private EditText mEmail, mPassword;

    //Data
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Assigning views
        SignUp = findViewById(R.id.login_button_register);
        SignIn = findViewById(R.id.login_button_login);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);

        //Assigning data
        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name), MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        //If there is network connection and already logged in move straight to moveactivity
        if (isNetworkAvailable()) {
            if (auth.getCurrentUser() != null) {
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
            }
        }

        //SignIn Listener
        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Getting inputs
                String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();

                //Checks
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required");
                    return;
                }
                if (!isValidEmail(email)){
                    mEmail.setError("Enter a valid email");
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required");
                    return;
                }

                if (password.length() < 8) {
                    mPassword.setError("Password must be at least 8 characters");
                    return;
                }

                //If no connection login by checking shared preferences
                if (!isNetworkAvailable()) {
                    String pass = sharedPreferences.getString(email,null);
                    if (pass.equals(password)) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(Login.this, "Invalid credentials",
                                Toast.LENGTH_SHORT).show();
                    }
                    //If there is connection to internet use Firebase
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        if (password.length() < 8) {
                                            mPassword.setError(getString(R.string.minimum_password));
                                        } else {
                                            Toast.makeText(Login.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }


            }
        });

        //Sign Up Button Listener
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToSignUpActivity();
            }


            private void moveToSignUpActivity() {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    //Set current user through sharedPreferences
    public void onPause() {

        super.onPause();
        mEmail = findViewById(R.id.login_email);
        String checkInput = mEmail.getText().toString().trim();
        Log.d("Login","Checking if user is logged in...");
        if (!checkInput.isEmpty() || checkInput != null) {
            Log.d("Login","Setting email from input " + checkInput);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

            // Sets current user
            prefsEditor.putString(getString(R.string.pref_email),checkInput);
            prefsEditor.apply();
        }
    }


    /**
     * Checks if the email isValid
     * @param target
     * @return
     */
    public static boolean isValidEmail(CharSequence target) {
        if(target == null) {
            return false;
        }else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }

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







