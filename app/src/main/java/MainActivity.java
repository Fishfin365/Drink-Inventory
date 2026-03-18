//package com.example.drinkinventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drinkinventoryapp.R;
import com.example.drinkinventoryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = LoginFirebaseBackend.getAuth();
        //auth = FirebaseAuth.getInstance();

        Button inventoryButton = findViewById(R.id.inventory);
        Button recipe = findViewById(R.id.recipe_browser);
        Button rateItem = findViewById(R.id.rate_item);
        Button leaderboard = findViewById(R.id.leaderboard);
        Button logoutButton = findViewById(R.id.logoutButton);

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is logged in — go to inventory
                startActivity(new Intent(MainActivity.this, InventoryActivity.class));
            } else {
                // Not logged in — go to login screen
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) auth.removeAuthStateListener(authListener);
    }
}
