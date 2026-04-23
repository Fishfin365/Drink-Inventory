package com.example.drinkinventoryapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.drinkinventoryapp.model.Drink;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Get the drink data from intent
        String drinkJson = getIntent().getStringExtra("drink_json");
        Drink drink = new Gson().fromJson(drinkJson, Drink.class);

        if (drink != null) {
            displayDrinkDetails(drink);
        }

        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());
    }

    private void displayDrinkDetails(Drink drink) {
        ImageView image = findViewById(R.id.detailImage);
        TextView title = findViewById(R.id.detailTitle);
        TextView category = findViewById(R.id.detailCategory);
        TextView ingredients = findViewById(R.id.detailIngredients);
        TextView instructions = findViewById(R.id.detailInstructions);

        title.setText(drink.strDrink);
        category.setText(drink.strCategory + " | " + drink.strAlcoholic);
        instructions.setText(drink.strInstructions);
        
        int ratingScore = getIntent().getIntExtra("drink_rating", -1);
        if (ratingScore != -1) {
            TextView rating = findViewById(R.id.detailRating);
            rating.setText("⭐ " + ratingScore + " / 5");
            rating.setVisibility(android.view.View.VISIBLE);
        }

        Glide.with(this).load(drink.strDrinkThumb).into(image);

        // Format ingredients and measures
        StringBuilder ingredientsBuilder = new StringBuilder();
        appendIngredient(ingredientsBuilder, drink.strIngredient1, drink.strMeasure1);
        appendIngredient(ingredientsBuilder, drink.strIngredient2, drink.strMeasure2);
        appendIngredient(ingredientsBuilder, drink.strIngredient3, drink.strMeasure3);
        appendIngredient(ingredientsBuilder, drink.strIngredient4, drink.strMeasure4);
        appendIngredient(ingredientsBuilder, drink.strIngredient5, drink.strMeasure5);
        appendIngredient(ingredientsBuilder, drink.strIngredient6, drink.strMeasure6);
        appendIngredient(ingredientsBuilder, drink.strIngredient7, drink.strMeasure7);
        appendIngredient(ingredientsBuilder, drink.strIngredient8, drink.strMeasure8);
        appendIngredient(ingredientsBuilder, drink.strIngredient9, drink.strMeasure9);
        appendIngredient(ingredientsBuilder, drink.strIngredient10, drink.strMeasure10);
        appendIngredient(ingredientsBuilder, drink.strIngredient11, drink.strMeasure11);
        appendIngredient(ingredientsBuilder, drink.strIngredient12, drink.strMeasure12);
        appendIngredient(ingredientsBuilder, drink.strIngredient13, drink.strMeasure13);
        appendIngredient(ingredientsBuilder, drink.strIngredient14, drink.strMeasure14);
        appendIngredient(ingredientsBuilder, drink.strIngredient15, drink.strMeasure15);

        ingredients.setText(ingredientsBuilder.toString().trim());
    }

    private void appendIngredient(StringBuilder builder, String ingredient, String measure) {
        if (ingredient != null && !ingredient.isEmpty()) {
            builder.append("• ");
            if (measure != null && !measure.isEmpty()) {
                builder.append(measure.trim()).append(" ");
            }
            builder.append(ingredient.trim()).append("\n");
        }
    }
}
