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
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private EditText searchBar;
    private CocktailAPI api;
    private FirebaseDrinkProvider firebaseProvider;
    private Chip chipFilterInventory;
    
    private final Set<String> userInventory = new HashSet<>();
    private List<Drink> currentRawResults = new ArrayList<>();
    private boolean isInventoryFilterActive = false;

    // Common ingredients that most people have at home
    private final List<String> commonIngredients = Arrays.asList(
            "ice", "water", "sugar", "salt", "pepper", "lemon", "lime"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityrecipespage);

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        chipFilterInventory = findViewById(R.id.chipFilterInventory);
        
        api = ApiClient.getClient().create(CocktailAPI.class);
        firebaseProvider = new FirebaseDrinkProvider();

        adapter = new RecipeAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchBar.getText().toString());
                return true;
            }
            return false;
        });

        chipFilterInventory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isInventoryFilterActive = isChecked;
            applyInventoryFilter();
        });

        setupBack();
        setupAdd();
        
        loadUserInventory();
        loadInitialRecipes();
    }

    private void loadUserInventory() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("inventory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userInventory.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) {
                            userInventory.add(name.toLowerCase().trim());
                        }
                    }
                    Log.d(TAG, "Inventory loaded: " + userInventory.size() + " items");
                    // Refresh the view if the filter is already toggled
                    if (isInventoryFilterActive) {
                        applyInventoryFilter();
                    }
                });
    }

    private void loadInitialRecipes() {
        final List<Drink> defaultResults = new ArrayList<>();
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
                existingResults.add(0, custom.toDrink());
            }
            currentRawResults = new ArrayList<>(existingResults);
            applyInventoryFilter();
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            loadInitialRecipes();
            return;
        }

        final List<Drink> combinedResults = new ArrayList<>();
        api.searchByName(query).enqueue(new Callback<DrinkResponse>() {
            @Override
            public void onResponse(Call<DrinkResponse> call, Response<DrinkResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().drinks != null) {
                    combinedResults.addAll(response.body().drinks);
                }
                searchFirebase(query, combinedResults);
            }
            @Override
            public void onFailure(Call<DrinkResponse> call, Throwable t) {
                searchFirebase(query, combinedResults);
            }
        });
    }

    private void searchFirebase(String query, List<Drink> existingResults) {
        firebaseProvider.searchCustomDrinks(query, customDrinks -> {
            for (com.example.drinkinventoryapp.model.CustomDrink custom : customDrinks) {
                existingResults.add(0, custom.toDrink());
            }
            currentRawResults = new ArrayList<>(existingResults);
            applyInventoryFilter();
        });
    }

    private void applyInventoryFilter() {
        if (!isInventoryFilterActive) {
            adapter.setDrinks(currentRawResults);
            return;
        }

        List<Drink> filtered = new ArrayList<>();
        for (Drink drink : currentRawResults) {
            if (canMakeDrink(drink)) {
                filtered.add(drink);
            }
        }
        adapter.setDrinks(filtered);
        
        if (filtered.isEmpty() && !currentRawResults.isEmpty()) {
            Toast.makeText(this, "No recipes fully match your inventory", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean canMakeDrink(Drink drink) {
        // If inventory hasn't loaded yet, we can't filter
        if (userInventory.isEmpty()) return false;

        // Check first 15 ingredients
        for (int i = 1; i <= 15; i++) {
            try {
                String ingredient = (String) drink.getClass().getField("strIngredient" + i).get(drink);
                if (ingredient != null && !ingredient.isEmpty()) {
                    String recipeIng = ingredient.toLowerCase().trim();
                    
                    // Skip common items (ice, water, etc)
                    if (isCommonIngredient(recipeIng)) continue;

                    // Fuzzy matching: check if any item in your inventory is part of the recipe ingredient name
                    // (e.g., your "Tequila" matches "Silver Tequila" or "Tequila Blanco")
                    boolean found = false;
                    for (String myItem : userInventory) {
                        if (recipeIng.contains(myItem) || myItem.contains(recipeIng)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) return false; // Missing a required ingredient
                }
            } catch (Exception e) {
                break;
            }
        }
        return true;
    }

    private boolean isCommonIngredient(String ingredient) {
        for (String common : commonIngredients) {
            if (ingredient.contains(common)) return true;
        }
        return false;
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
