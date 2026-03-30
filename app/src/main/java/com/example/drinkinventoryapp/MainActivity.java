package com.example.drinkinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityhomepage);

        auth = LoginFirebaseBackend.getAuth();

        MaterialButton inventoryButton = findViewById(R.id.inventory);
        MaterialButton recipeButton = findViewById(R.id.recipe_browser);
        MaterialButton rateItemButton = findViewById(R.id.rate_item);
        MaterialButton leaderboardButton = findViewById(R.id.leaderboard);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);

        // Navigation for buttons
        if (inventoryButton != null) {
            inventoryButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                startActivity(intent);
            });
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LogoutActivity.class);
                startActivity(intent);
            });
        }

        if (rateItemButton != null) {
            rateItemButton.setOnClickListener(v -> {
                Intent intent1 = new Intent(MainActivity.this, RatingActivity.class);
                startActivity(intent1);
            });
        }

        if (recipeButton != null) {
            recipeButton.setOnClickListener(v -> {
                Intent intent2 = new Intent(MainActivity.this, RecipeActivity.class);
                startActivity(intent2);
            });
        }

        // Auth listener to handle session state
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // Not logged in — go to login screen
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
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
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
