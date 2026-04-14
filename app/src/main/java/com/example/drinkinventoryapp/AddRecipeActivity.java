package com.example.drinkinventoryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddRecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityaddrecipespage);
        setupAdd();
        setupBack();
    }

    private void goToRecipeActivity() {
        Intent intent1 = new Intent(this, RecipeActivity.class);
        startActivity(intent1);
        finish();
    }

    private void setupBack() {
        FloatingActionButton fab = findViewById(R.id.fabBackButton);
        fab.setOnClickListener(v -> goToRecipeActivity());
    }

    private void setupAdd() {
        FloatingActionButton fab = findViewById(R.id.fabAddRecipe);
        fab.setOnClickListener(v -> goToRecipeActivity());
    }



}
