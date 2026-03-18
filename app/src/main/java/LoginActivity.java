import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private FirebaseAuth mAuth;

    // Use the ActivityResultLauncher for handling the FirebaseUI sign-in result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sign_in); // Or whatever your layout is

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, launch the Sign In activity using FirebaseUI
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(), // Email/Password sign-in
                    new AuthUI.IdpConfig.GoogleBuilder().build() // Google Sign-in
            );

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    //.setLogo(R.mipmap.ic_launcher) // Optional: add your app logo
                    .setAvailableProviders(providers)
                    .build();

            signInLauncher.launch(signInIntent); // Launch the sign-in intent
        } else {
            goToMainActivity(); // User is already signed in, go to your main content
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Log.d(TAG, "Sign in successful!");
            goToMainActivity(); // Sign in was successful
        } else {
            // Sign in failed
            IdpResponse response = result.getIdpResponse();
            if (response == null) {
                Log.w(TAG, "Sign in canceled");
            } else {
                Log.w(TAG, "Sign in error", response.getError());
            }
            Toast.makeText(this, "There was an error signing in", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
