package com.example.drinkinventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IngredientAdapter adapter;
    private List<Ingredient> ingredientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityinventorypage);

        // Setup Back Button
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_ingredients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Mock Data
        ingredientList = new ArrayList<>();
        ingredientList.add(new Ingredient("Vodka", 2, "750ml"));
        ingredientList.add(new Ingredient("Gin", 1, "1L"));
        ingredientList.add(new Ingredient("Tonic Water", 5, "330ml"));
        ingredientList.add(new Ingredient("Lime Juice", 3, "250ml"));
        ingredientList.add(new Ingredient("Simple Syrup", 1, "500ml"));

        // Setup Adapter
        adapter = new IngredientAdapter(ingredientList);
        recyclerView.setAdapter(adapter);
    }
}
