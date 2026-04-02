package com.example.drinkinventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IngredientAdapter adapter;
    private boolean isEditMode = false;
    private boolean isDeleteMode = false;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityinventorypage);

        // Init Firebase
        db = LoginFirebaseBackend.getDb();
        auth = LoginFirebaseBackend.getAuth();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Not logged in! Cannot access inventory.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_ingredients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start with empty adapter, load from DB
        adapter = new IngredientAdapter(new ArrayList<>(), item -> {
            if (item.getId() != null) {
                getInventoryCollection().document(item.getId()).delete()
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete from database", Toast.LENGTH_SHORT).show());
            }
        });
        recyclerView.setAdapter(adapter);

        loadInventoryFromDatabase();

        // Add Button
        ImageButton addButton = findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add New Ingredient");

            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(60, 40, 60, 10);

            final EditText nameInput = new EditText(this);
            nameInput.setHint("Ingredient Name");
            dialogLayout.addView(nameInput);

            final EditText volumeInput = new EditText(this);
            volumeInput.setHint("Volume (e.g. 1L)");
            dialogLayout.addView(volumeInput);

            builder.setView(dialogLayout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String volume = volumeInput.getText().toString().trim();
                if (!name.isEmpty()) {
                    Ingredient newIngredient = new Ingredient(name, 1, volume.isEmpty() ? "N/A" : volume);
                    
                    // Add to firestore
                    getInventoryCollection().add(newIngredient).addOnSuccessListener(docRef -> {
                        newIngredient.setId(docRef.getId());
                        adapter.addItem(newIngredient);
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }).addOnFailureListener(e -> Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show());
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Edit Button Toggle
        Button editButton = findViewById(R.id.btn_edit);
        ImageButton deleteButton = findViewById(R.id.btn_delete);
        
        editButton.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            if (isEditMode) {
                editButton.setText("Done Edit");
                isDeleteMode = false; 
                deleteButton.setColorFilter(Color.parseColor("#EAEAEA"));
            } else {
                editButton.setText("Edit");
                // Save edited changes
                saveAllEditsToDatabase();
            }
            adapter.setEditMode(isEditMode);
        });

        // Delete Button Toggle
        deleteButton.setOnClickListener(v -> {
            isDeleteMode = !isDeleteMode;
            if (isDeleteMode) {
                deleteButton.setColorFilter(Color.RED); 
                isEditMode = false;
                editButton.setText("Edit");
            } else {
                deleteButton.setColorFilter(Color.parseColor("#EAEAEA"));
            }
            adapter.setDeleteMode(isDeleteMode);
        });

        // Search feature
        FloatingActionButton fabSearch = findViewById(R.id.fab_search);
        EditText searchBar = findViewById(R.id.search_bar);

        fabSearch.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
            } else {
                searchBar.setVisibility(View.GONE);
                searchBar.setText(""); // Clear search
                adapter.filter("");
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private CollectionReference getInventoryCollection() {
        return db.collection("users").document(userId).collection("inventory");
    }

    private void loadInventoryFromDatabase() {
        getInventoryCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Ingredient> loadedItems = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Ingredient item = document.toObject(Ingredient.class);
                    loadedItems.add(item);
                }
                adapter.setList(loadedItems);
            } else {
                Toast.makeText(this, "Failed to load inventory data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAllEditsToDatabase() {
        // Iterate through all tracked items and push them to database if they exist
        for (Ingredient i : adapter.getAllItems()) {
            if (i.getId() != null) {
                getInventoryCollection().document(i.getId()).set(i);
            }
        }
    }
}
