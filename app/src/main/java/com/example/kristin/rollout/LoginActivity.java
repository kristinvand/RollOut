package com.example.kristin.rollout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         final EditText etPassword = (EditText) findViewById(R.id.etPassword);
         final EditText etEmail = (EditText) findViewById(R.id.etEmail);

         final Button bLogin = (Button) findViewById(R.id.bLogin);
         final TextView registerLink = (TextView) findViewById(R.id.tvRegisterHere);

         registerLink.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                 LoginActivity.this.startActivity(registerIntent);


             }
         });

         bLogin.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent loginIntent = new Intent(LoginActivity.this, MapsActivity.class);
                 LoginActivity.this.startActivity(loginIntent);


             }
         });




     }


}

