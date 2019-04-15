package com.example.kristin.rollout;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class LoginActivity extends AppCompatActivity {

    public static String userUsername;
    public static String userPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        TextView registerLink = findViewById(R.id.registerLink);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

    }

    public void loginUser(View v) {

        EditText userUsernameTextView = findViewById(R.id.userUsername);
        EditText userPasswordTextView = findViewById(R.id.userPassword);

        userUsername = userUsernameTextView.getText().toString();
        userPassword = userPasswordTextView.getText().toString();

        Intent loginIntent = new Intent(LoginActivity.this, MapsActivity.class);
        LoginActivity.this.startActivity(loginIntent);

    }




}


