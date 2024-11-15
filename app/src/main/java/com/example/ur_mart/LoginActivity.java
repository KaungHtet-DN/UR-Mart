package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signupRedirectText;
    ProgressBar loginProgressBar;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // Change activity to listing page
            Intent intent = new Intent(LoginActivity.this, ProductListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize controls
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginProgressBar = findViewById(R.id.login_progressbar);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        loginButton = findViewById(R.id.login_button);

        // Login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress bar
                loginProgressBar.setVisibility(View.VISIBLE);

                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if(validateEmail() && validatePassword()){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loginProgressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        loginEmail.setError(null);
                                        loginPassword.setError(null);

                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        Toast.makeText(LoginActivity.this, "Login successful.",
                                                Toast.LENGTH_SHORT).show();

                                        // Change activity to listing page
                                        Intent intent = new Intent(LoginActivity.this, ProductListActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    loginProgressBar.setVisibility(View.GONE);
                    loginPassword.setError("Invalid email or password.");
                    loginPassword.requestFocus();
                }
            }
        });

        // Redirect to register page click event
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateEmail(){
        String email = loginEmail.getText().toString();
        if(email.isEmpty()){
            loginEmail.setError("Email cannot be empty.");
            return  false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginEmail.setError("Invalid email.");
            return  false;
        }
        else {
            loginEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String password = loginPassword.getText().toString();
        if(password.isEmpty()){
            loginPassword.setError("Password cannot be empty.");
            return  false;
        }
        else if (password.length() < 8){
            loginPassword.setError("Minimum password length is 8.");
            return  false;
        }
        else {
            loginPassword.setError(null);
            return true;
        }
    }
}