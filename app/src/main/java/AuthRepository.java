import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    //backend functions for making the firebase authentication with users and their entry into the database.
    private final FirebaseAuth auth = LoginFirebaseBackend.getAuth();

    public Task<AuthResult> signUp(String email, String password){
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> logIn(String email, String password){
        return auth.signInWithEmailAndPassword(email, password);
    }

    public void logOut() {
        auth.signOut();
    }

    // Returns null if not logged in
    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }
}
