import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {

    private static final String TAG = "LogoutActivity";
    private FirebaseAuth mAuth; // Declare the FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_logout); // Make sure you have an activity_logout.xml layout

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        // Assuming you have a button in your activity_logout.xml with id 'logout_button'
        //Button logoutButton = findViewById(R.id.logout_button);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(view -> signOut());
        } else {
            Log.e(TAG, "Logout button not found in layout.");
            // You might want to automatically sign out here if there's no button,
            // or provide some UI feedback.
            signOut(); // Example: Automatically sign out if no button to trigger it
        }

        // You could also have a prompt or confirmation dialog here before calling signOut()
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this) // Sign out the current user
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sign out successful!");
                            Toast.makeText(LogoutActivity.this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
                            // Redirect to your SignInActivity after successful sign-out
                            Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Finish the current activity so the user can't navigate back
                        } else {
                            Log.e(TAG, "Sign out failed", task.getException());
                            Toast.makeText(LogoutActivity.this, "Sign out failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // If you also want an option to delete the account from this activity
    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this) // Deletes the user account
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            Toast.makeText(LogoutActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                            // Redirect to sign-in screen
                            Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Failed to delete user account", task.getException());
                            Toast.makeText(LogoutActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
