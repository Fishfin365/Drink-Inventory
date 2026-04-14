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

        // grab UI references
        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        api = ApiClient.getClient().create(CocktailAPI.class);
        firebaseProvider = new FirebaseDrinkProvider();

        // hook up the list view
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
        setupAdd();
        
        // load everything initially so the page isn't empty
        loadInitialRecipes();
    }

    private void loadInitialRecipes() {
        final List<Drink> defaultResults = new ArrayList<>();
        
        // fetch margarita stuff from the public api first
        api.searchByName("margarita").enqueue(new Callback<DrinkResponse>() {
            @Override
            public void onResponse(Call<DrinkResponse> call, Response<DrinkResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().drinks != null) {
                    defaultResults.addAll(response.body().drinks);
                }
                loadAllFirebase(defaultResults);
            }
            @Override
            public void onFailure(Call<DrinkResponse> call, Throwable t) {
                loadAllFirebase(defaultResults);
            }
        });
    }

    private void loadAllFirebase(List<Drink> existingResults) {
        firebaseProvider.getAllCustomDrinks(customDrinks -> {
            for (com.example.drinkinventoryapp.model.CustomDrink custom : customDrinks) {
                existingResults.add(0, custom.toDrink()); // Custom at top
            }
            adapter.setDrinks(existingResults);
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            loadInitialRecipes();
            return;
        }

        final List<Drink> combinedResults = new ArrayList<>();

                // hit the public api for the search keyword
        api.searchByName(query).enqueue(new Callback<DrinkResponse>() {
            @Override
            public void onResponse(Call<DrinkResponse> call, Response<DrinkResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().drinks != null) {
                    combinedResults.addAll(response.body().drinks);
                }
                
                // tack on any custom firebase drinks after the api results come back
                searchFirebase(query, combinedResults);
            }

            @Override
            public void onFailure(Call<DrinkResponse> call, Throwable t) {
                // if the api crashes just search the firebase stuff anyway
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

    private void goToAddRecipeActivity() {
        Intent intent1 = new Intent(this, AddRecipeActivity.class);
        startActivity(intent1);
        finish();
    }

    private void setupBack() {
        FloatingActionButton fab = findViewById(R.id.fabBackButton);
        fab.setOnClickListener(v -> goToMainActivity());
    }
    private void setupAdd() {
        FloatingActionButton fab = findViewById(R.id.fabAddRecipe);
        fab.setOnClickListener(v -> goToAddRecipeActivity());
    }
}
