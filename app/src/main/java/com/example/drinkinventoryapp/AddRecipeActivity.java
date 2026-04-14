package com.example.drinkinventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.drinkinventoryapp.model.CustomDrink;
import com.example.drinkinventoryapp.network.FirebaseDrinkProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecipeActivity extends AppCompatActivity {

    private static final String TAG = "AddRecipeActivity";
    private EditText etRecipeName, etRecipeIngredients, etRecipeInstructions;
    private ImageView ivRecipeImage;
    private Button btnTakePhoto;
    private ProgressBar uploadProgress;
    
    private Uri tempPhotoUri; 
    private Uri finalPhotoUri; 
    private final FirebaseDrinkProvider drinkProvider = new FirebaseDrinkProvider();

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && tempPhotoUri != null) {
                    finalPhotoUri = tempPhotoUri;
                    Log.d(TAG, "Photo captured: " + finalPhotoUri);
                    Glide.with(this)
                            .load(finalPhotoUri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(ivRecipeImage);
                } else {
                    Log.e(TAG, "Camera capture failed.");
                    Toast.makeText(this, "Camera cancelled or failed", Toast.LENGTH_SHORT).show();
                    finalPhotoUri = null;
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityaddrecipespage);
        
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeIngredients = findViewById(R.id.etRecipeIngredients);
        etRecipeInstructions = findViewById(R.id.etRecipeInstructions);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        uploadProgress = findViewById(R.id.uploadProgress);

        btnTakePhoto.setOnClickListener(v -> checkPermissionAndLaunchCamera());

        setupAdd();
        setupBack();
    }

    private void checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            tempPhotoUri = FileProvider.getUriForFile(this,
                    "com.example.drinkinventoryapp.fileprovider",
                    photoFile);
            takePictureLauncher.launch(tempPhotoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getCacheDir(), "images");
        if (!storageDir.exists()) storageDir.mkdirs();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void setupAdd() {
        FloatingActionButton fab = findViewById(R.id.fabAddRecipe);
        fab.setOnClickListener(v -> {
            String name = etRecipeName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomDrink drink = new CustomDrink();
            drink.setName(name);
            drink.setCategory("Custom");
            drink.setInstructions(etRecipeInstructions.getText().toString().trim());
            
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                drink.setCreatorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }

            String ingredientsStr = etRecipeIngredients.getText().toString().trim();
            if (!ingredientsStr.isEmpty()) {
                for (String part : ingredientsStr.split(",")) {
                    String clean = part.trim();
                    if (!clean.isEmpty()) drink.getIngredients().put(clean, "");
                }
            }

            if (finalPhotoUri != null) {
                uploadProgress.setVisibility(View.VISIBLE);
                fab.setEnabled(false);
                
                drinkProvider.uploadImage(finalPhotoUri, new FirebaseDrinkProvider.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        drink.setImageUrl(imageUrl);
                        saveToFirestore(drink);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        uploadProgress.setVisibility(View.GONE);
                        fab.setEnabled(true);
                        Toast.makeText(AddRecipeActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        saveToFirestore(drink);
                    }
                });
            } else {
                saveToFirestore(drink);
            }
        });
    }

    private void saveToFirestore(CustomDrink drink) {
        drinkProvider.saveCustomDrink(drink);
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupBack() {
        findViewById(R.id.fabBackButton).setOnClickListener(v -> finish());
    }
}
