package com.example.kristin.rollout;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private CollectionReference rollOutDatabase = FirebaseFirestore.getInstance().collection("RollOutData");
    String userName;
    String userEmail;
    String userPhoneNumber;
    String userUsername;
    String userPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        TextView loginLink = findViewById(R.id.loginLink);

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(loginIntent);
            }
        });

    }


    public void registerUser(View v){

        EditText userNameTextView = findViewById(R.id.userName);
        EditText userEmailTextView = findViewById(R.id.userEmail);
        EditText userPhoneNumberTextView = findViewById(R.id.userPhoneNumber);
        EditText userUsernameTextView = findViewById(R.id.userUsername);
        EditText userPasswordTextView = findViewById(R.id.userPassword);

        userName = userNameTextView.getText().toString();
        userEmail = userEmailTextView.getText().toString();
        userPhoneNumber = userPhoneNumberTextView.getText().toString();
        userUsername = userUsernameTextView.getText().toString();
        userPassword = userPasswordTextView.getText().toString();

        writeToDatabase();

        Intent startMap = new Intent(RegisterActivity.this, LoginActivity.class);
        RegisterActivity.this.startActivity(startMap);
    }

    public void writeToDatabase(){

        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("name", userName);
        userInfo.put("email", userEmail);
        userInfo.put("phone_number", userPhoneNumber);
        userInfo.put("username", userUsername);
        userInfo.put("password", userPassword);


        rollOutDatabase.document(userUsername).set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });
    }
}
