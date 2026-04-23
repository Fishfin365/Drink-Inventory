package com.example.drinkinventoryapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RatingActivity extends AppCompatActivity {

    // UI
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private RateItemAdapter adapter;
    private TextView tvTotalRated, tvAvgRating, tvTopRated;
    private EditText etSearch;
    private Spinner spinnerSort;

    // Data
    private final List<RateItem> allItems = new ArrayList<>();
    private List<RateItem> filteredItems = new ArrayList<>();

    // Filter state
    private String activeTab = "ALL";   // "ALL" | "INGREDIENT" | "RECIPE"
    private String sortMode  = "NAME";  // "NAME" | "RATING" | "RECENT"
    private String searchQuery = "";

    // Sort options shown in Spinner
    private static final String[] SORT_OPTIONS = {"Name", "Rating ↓", "Recent"};
    
    // Firebase
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityratingpage);

        setupToolbar();
        //setupTabs();
        setupSummaryBar();
        setupRecyclerView();
        setupSearch();
        setupSortSpinner();
        setupFab();
        setupBack();

        db = LoginFirebaseBackend.getDb();
        if (LoginFirebaseBackend.getAuth().getCurrentUser() != null) {
            userId = LoginFirebaseBackend.getAuth().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadData();  
        applyFilters();
    }

    // -------------------------------------------------------------------------
    // Setup helpers
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

//    private void setupTabs() {
//        tabLayout = findViewById(R.id.tabLayout);
//        tabLayout.addTab(tabLayout.newTab().setText("All"));
//        tabLayout.addTab(tabLayout.newTab().setText("Ingredients"));
//        tabLayout.addTab(tabLayout.newTab().setText("Recipes"));
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override public void onTabSelected(TabLayout.Tab tab) {
//                switch (tab.getPosition()) {
//                    case 0: activeTab = "ALL"; break;
//                    case 1: activeTab = "INGREDIENT"; break;
//                    case 2: activeTab = "RECIPE"; break;
//                }
//                applyFilters();
//            }
//            @Override public void onTabUnselected(TabLayout.Tab tab) {}
//            @Override public void onTabReselected(TabLayout.Tab tab) {}
//        });
//    }

    private void setupSummaryBar() {
        tvTotalRated = findViewById(R.id.tvTotalRated);
        tvAvgRating  = findViewById(R.id.tvAvgRating);
        tvTopRated   = findViewById(R.id.tvTopRated);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerRatings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RateItemAdapter(this, filteredItems);
        adapter.setOnItemActionListener(new RateItemAdapter.OnItemActionListener() {
            @Override
            public void onEdit(RateItem item, int position) {
                showAddEditDialog(item, position);
            }

            @Override
            public void onDelete(RateItem item, int position) {
                showDeleteConfirm(item, position);
            }

            @Override
            public void onRatingChanged(RateItem item, int newRating, int position) {
                // Persist rating change to your DB here
                item.setRating(newRating);
                item.setUpdatedAt(System.currentTimeMillis());
                db.collection("ratings").document(item.getFirebaseId()).set(item);
                updateSummaryBar();
                Toast.makeText(RatingActivity.this,
                        item.getName() + " rated " + newRating + "★",
                        Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.getDefault());
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortSpinner() {
        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view,
                                       int pos, long id) {
                switch (pos) {
                    case 0: sortMode = "NAME"; break;
                    case 1: sortMode = "RATING"; break;
                    case 2: sortMode = "RECENT"; break;
                }
                applyFilters();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fabAddRating);
        fab.setOnClickListener(v -> showAddEditDialog(null, -1));
    }

    private void setupBack() {
        FloatingActionButton fab = findViewById(R.id.fabBackButton);
        fab.setOnClickListener(v -> goToMainActivity());
    }

    // -------------------------------------------------------------------------
    // Data & filtering
    // -------------------------------------------------------------------------


    private void loadData() {
        db.collection("ratings").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allItems.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    RateItem item = document.toObject(RateItem.class);
                    allItems.add(item);
                }
                applyFilters();
            } else {
                Toast.makeText(this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        filteredItems = new ArrayList<>();

        for (RateItem item : allItems) {
            // Tab filter
            if (!activeTab.equals("ALL")) {
                if (item.getType() == null || !item.getType().name().equals(activeTab)) continue;
            }
            // Search filter
            if (!searchQuery.isEmpty()) {
                boolean matchName  = item.getName() != null && item.getName().toLowerCase(Locale.getDefault()).contains(searchQuery);
                boolean matchNotes = item.getNotes() != null &&
                        item.getNotes().toLowerCase(Locale.getDefault()).contains(searchQuery);
                boolean matchTag   = false;
                for (String tag : item.getTags()) {
                    if (tag.toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                        matchTag = true;
                        break;
                    }
                }
                if (!matchName && !matchNotes && !matchTag) continue;
            }
            filteredItems.add(item);
        }

        // Sort
        switch (sortMode) {
            case "NAME":
                Collections.sort(filteredItems,
                        Comparator.comparing(i -> i.getName() != null ? i.getName().toLowerCase(Locale.getDefault()) : ""));
                break;
            case "RATING":
                Collections.sort(filteredItems,
                        (a, b) -> Integer.compare(b.getRating(), a.getRating()));
                break;
            case "RECENT":
                Collections.sort(filteredItems,
                        (a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()));
                break;
        }

        adapter.setItems(filteredItems);
        updateSummaryBar();
    }

    private void updateSummaryBar() {
        int total = allItems.size();
        tvTotalRated.setText(String.valueOf(total));

        if (total == 0) {
            tvAvgRating.setText("—");
            tvTopRated.setText("—");
            return;
        }

        double sum = 0;
        RateItem top = allItems.get(0);
        for (RateItem item : allItems) {
            sum += item.getRating();
            if (item.getRating() > top.getRating()) top = item;
        }

        double avg = sum / total;
        tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));

        // Abbreviate long names in the summary bar
        String topName = top.getName() != null ? top.getName() : "Unknown";
        tvTopRated.setText(topName.length() > 8 ? topName.substring(0, 7) + "…" : topName);
    }

    // -------------------------------------------------------------------------
    // Dialogs
    // -------------------------------------------------------------------------

    private void showAddEditDialog(RateItem existingItem, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.addratingdialog);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        boolean isEdit = (existingItem != null);

        TextView tvTitle     = dialog.findViewById(R.id.tvDialogTitle);
        EditText etName      = dialog.findViewById(R.id.etName);
        Spinner  spinType    = dialog.findViewById(R.id.spinnerType);
        //EditText etNotes     = dialog.findViewById(R.id.etNotes);
        //EditText etTags      = dialog.findViewById(R.id.etTags);

        ImageView[] dStars = {
                dialog.findViewById(R.id.dStar1),
                dialog.findViewById(R.id.dStar2),
                dialog.findViewById(R.id.dStar3),
                dialog.findViewById(R.id.dStar4),
                dialog.findViewById(R.id.dStar5)
        };

        tvTitle.setText(isEdit ? "Edit Rating" : "Add Rating");

        // Type spinner
        String[] types = {"Ingredient", "Recipe"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinType.setAdapter(typeAdapter);

        // Track selected star rating
        final int[] selectedRating = {isEdit ? existingItem.getRating() : 0};

        // Pre-fill if editing
        if (isEdit) {
            etName.setText(existingItem.getName());
            spinType.setSelection(existingItem.getType() == RateItem.Type.RECIPE ? 1 : 0);
            //etNotes.setText(existingItem.getNotes());
            //etTags.setText(existingItem.getTagsAsString());
            updateDialogStars(dStars, existingItem.getRating());
        }

        // Star click listeners
        for (int i = 0; i < dStars.length; i++) {
            final int starVal = i + 1;
            dStars[i].setOnClickListener(v -> {
                selectedRating[0] = starVal;
                updateDialogStars(dStars, starVal);
            });
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }
            if (selectedRating[0] == 0) {
                Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show();
                return;
            }

            RateItem.Type type = spinType.getSelectedItemPosition() == 1
                    ? RateItem.Type.RECIPE : RateItem.Type.INGREDIENT;
            //String notes = etNotes.getText().toString().trim();
            //List<String> tags = RateItem.parseTags(etTags.getText().toString());

            if (isEdit) {
                existingItem.setName(name);
                existingItem.setType(type);
                existingItem.setRating(selectedRating[0]);
                existingItem.setUpdatedAt(System.currentTimeMillis());
                existingItem.setUserId(userId);
                
                db.collection("ratings")
                        .document(existingItem.getFirebaseId()).set(existingItem)
                        .addOnSuccessListener(aVoid -> {
                            applyFilters();
                            Toast.makeText(this, "Updated " + name, Toast.LENGTH_SHORT).show();
                        });
            } else {
                RateItem newItem = new RateItem(name, type, selectedRating[0]);
                newItem.setUserId(userId);
                
                db.collection("ratings")
                        .add(newItem)
                        .addOnSuccessListener(docRef -> {
                            newItem.setFirebaseId(docRef.getId());
                            allItems.add(newItem);
                            applyFilters();
                            Toast.makeText(this, "Added " + name, Toast.LENGTH_SHORT).show();
                        });
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    /** Fill/outline dialog stars up to 'rating' */
    private void updateDialogStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(
                    i < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void showDeleteConfirm(RateItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Rating")
                .setMessage("Remove the rating for \"" + item.getName() + "\"? This can't be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("ratings")
                            .document(item.getFirebaseId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                allItems.remove(item);
                                applyFilters();
                                Toast.makeText(this, "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

