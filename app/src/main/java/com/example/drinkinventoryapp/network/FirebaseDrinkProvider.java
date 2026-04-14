package com.example.drinkinventoryapp.network;

import com.example.drinkinventoryapp.model.CustomDrink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDrinkProvider {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface DrinkCallback {
        void onCallback(List<CustomDrink> drinks);
    }

    public void getAllCustomDrinks(DrinkCallback callback) {
        db.collection("custom_drinks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CustomDrink> drinks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CustomDrink drink = document.toObject(CustomDrink.class);
                            drink.setId(document.getId());
                            drinks.add(drink);
                        }
                        callback.onCallback(drinks);
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                });
    }

    public void searchCustomDrinks(String query, DrinkCallback callback) {
        // Basic search: fetch all and filter locally for better matching (Firestore doesn't support case-insensitive contains easily)
        db.collection("custom_drinks")
                .get()
                .addOnCompleteListener(task -> {
                    List<CustomDrink> results = new ArrayList<>();
                    if (task.isSuccessful()) {
                        String lowerQuery = query.toLowerCase();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CustomDrink drink = document.toObject(CustomDrink.class);
                            drink.setId(document.getId());
                            if (drink.getName() != null && drink.getName().toLowerCase().contains(lowerQuery)) {
                                results.add(drink);
                            }
                        }
                    }
                    callback.onCallback(results);
                });
    }

    public void saveCustomDrink(CustomDrink drink) {
        db.collection("custom_drinks").add(drink);
    }
}
