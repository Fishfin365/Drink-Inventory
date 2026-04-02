package com.example.drinkinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drinkinventoryapp.model.CustomDrink;
import com.example.drinkinventoryapp.network.FirebaseDrinkProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etRecipeName;
    private EditText etRecipeIngredients;
    private EditText etRecipeInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityaddrecipespage);
        
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeIngredients = findViewById(R.id.etRecipeIngredients);
        etRecipeInstructions = findViewById(R.id.etRecipeInstructions);

        setupAdd();
        setupBack();
    }

    private void goToRecipeActivity() {
        Intent intent = new Intent(this, RecipeActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupBack() {
        FloatingActionButton fab = findViewById(R.id.fabBackButton);
        fab.setOnClickListener(v -> goToRecipeActivity());
    }

    private void setupAdd() {
        FloatingActionButton fab = findViewById(R.id.fabAddRecipe);
        fab.setOnClickListener(v -> {
            String name = etRecipeName.getText().toString().trim();
            String ingredientsStr = etRecipeIngredients.getText().toString().trim();
            String instructions = etRecipeInstructions.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a recipe name", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomDrink drink = new CustomDrink();
            drink.setName(name);
            drink.setCategory("Custom");
            drink.setInstructions(instructions);

            // gotta parse the ingredients string into a map
            if (!ingredientsStr.isEmpty()) {
                String[] parts = ingredientsStr.split(",");
                for (String part : parts) {
                    String cleanPart = part.trim();
                    if (!cleanPart.isEmpty()) {
                        drink.getIngredients().put(cleanPart, "");
                    }
                }
            }

            // push the new recipe to firebase
            new FirebaseDrinkProvider().saveCustomDrink(drink);
            
            Toast.makeText(this, "Recipe Saved to Firebase!", Toast.LENGTH_SHORT).show();
            goToRecipeActivity();
        });
    }

}
