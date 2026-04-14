package com.example.drinkinventoryapp.network;

import android.net.Uri;
import com.example.drinkinventoryapp.model.CustomDrink;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FirebaseDrinkProvider {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public interface DrinkCallback {
        void onCallback(List<CustomDrink> drinks);
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
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

    public void uploadImage(Uri imageUri, UploadCallback callback) {
        String fileName = "drink_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        callback.onSuccess(downloadUri.toString());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
