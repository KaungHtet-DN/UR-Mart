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

import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    EditText signupEmail, signupPassword, signupConfirmPassword;
    TextView loginRedirectText;
    Button signupButton;
    ProgressBar signupProgressBar;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // Change activity to listing page
            Intent intent = new Intent(SignUpActivity.this, ProductListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        // Initialize controls
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirmpassword);
        signupProgressBar = findViewById(R.id.signup_progressbar);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);

        // Signup button click event
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress bar
                signupProgressBar.setVisibility(View.VISIBLE);

                // Get user input values
                String email = signupEmail.getText().toString();
                String password = signupPassword.getText().toString();
                String confirmPassword = signupConfirmPassword.getText().toString();

                // Validate user input
                if(validateEmail() && validatePassword() && validateConfirmPassword()) {
                    // Create user with Firebase by Email and Password
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // Hide progress bar
                                    signupProgressBar.setVisibility(View.GONE);

                                    if (task.isSuccessful()) {
                                        // Show account created message
                                        Toast.makeText(SignUpActivity.this, "Your account is successfully created.", Toast.LENGTH_SHORT).show();

                                        // Change activity to login page
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    signupProgressBar.setVisibility(View.GONE);
                }
            }
        });

        // Redirect to login page click event
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public Boolean validateEmail(){
        String email = signupEmail.getText().toString();
        if(email.isEmpty()){
            signupEmail.setError("Email cannot be empty.");
            return  false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            signupEmail.setError("Invalid email.");
            return  false;
        }
        else {
            signupEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String password = signupPassword.getText().toString();
        if(password.isEmpty()){
            signupPassword.setError("Password cannot be empty.");
            return  false;
        }
        else if (password.length() < 8){
            signupPassword.setError("Minimum password length is 8.");
            return  false;
        }
        else {
            signupPassword.setError(null);
            return true;
        }
    }

    public Boolean validateConfirmPassword(){
        String password = signupPassword.getText().toString();
        String confirmPassword = signupConfirmPassword.getText().toString();
        if(password.isEmpty()){
            signupConfirmPassword.setError("Password cannot be empty.");
            return  false;
        }
        else if (!password.equals(confirmPassword)){
            signupConfirmPassword.setError("Password and Confirm Password are not matched.");
            return  false;
        }
        else {
            signupConfirmPassword.setError(null);
            return true;
        }
    }
}