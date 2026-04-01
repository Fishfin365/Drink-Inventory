package com.example.drinkinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drinkinventoryapp.model.Drink;
import com.example.drinkinventoryapp.model.DrinkResponse;
import com.example.drinkinventoryapp.network.ApiClient;
import com.example.drinkinventoryapp.network.CocktailAPI;
import com.example.drinkinventoryapp.network.FirebaseDrinkProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private EditText searchBar;
    private CocktailAPI api;
    private FirebaseDrinkProvider firebaseProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityrecipespage);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        api = ApiClient.getClient().create(CocktailAPI.class);
        firebaseProvider = new FirebaseDrinkProvider();

        // Setup RecyclerView
        adapter = new RecipeAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Setup Search
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchBar.getText().toString());
                return true;
            }
            return false;
        });

        setupBack();
        
        // Load some initial drinks
        performSearch("margarita");
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        final List<Drink> combinedResults = new ArrayList<>();

        // 1. Search in Public API
        api.searchByName(query).enqueue(new Callback<DrinkResponse>() {
            @Override
            public void onResponse(Call<DrinkResponse> call, Response<DrinkResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().drinks != null) {
                    combinedResults.addAll(response.body().drinks);
                }
                
                // 2. Search in Firebase (Custom Drinks) after API results are added
                searchFirebase(query, combinedResults);
            }

            @Override
            public void onFailure(Call<DrinkResponse> call, Throwable t) {
                // If API fails, still try to search Firebase
                searchFirebase(query, combinedResults);
            }
        });
    }

    private void searchFirebase(String query, List<Drink> existingResults) {
        firebaseProvider.searchCustomDrinks(query, customDrinks -> {
            for (com.example.drinkinventoryapp.model.CustomDrink custom : customDrinks) {
                existingResults.add(0, custom.toDrink()); // Add custom drinks to the top
            }
            
            if (existingResults.isEmpty()) {
                Toast.makeText(RecipeActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
            }
            
            adapter.setDrinks(existingResults);
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupBack() {
        FloatingActionButton fab = findViewById(R.id.fabBackButton);
        fab.setOnClickListener(v -> goToMainActivity());
    }
}
