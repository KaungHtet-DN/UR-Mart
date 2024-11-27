package com.example.ur_mart;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

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

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    EditText signupUsername, signupEmail, signupPhoneNo, signupPassword, signupConfirmPassword;
    TextView loginRedirectText;
    Button signupButton;
    ProgressBar signupProgressBar;
    FirebaseAuth mAuth;
    DatabaseReference databaseRef;
    AwesomeValidation mAwesomeValidation;
    String PhoneNumber_Regex = "^(\\+1[-\\s]?|1[-\\s]?|\\()?(\\d{3})(\\)|[-\\s])?(\\d{3})[-\\s]?(\\d{4})$";

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
        mAwesomeValidation = new AwesomeValidation(BASIC);

        // Initialize controls
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPhoneNo = findViewById(R.id.signup_phoneno);
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

                // Add validation
                mAwesomeValidation.addValidation(SignUpActivity.this, R.id.signup_username, RegexTemplate.NOT_EMPTY, R.string.err_name);
                mAwesomeValidation.addValidation(SignUpActivity.this, R.id.signup_email, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email);
                mAwesomeValidation.addValidation(SignUpActivity.this, R.id.signup_phoneno, PhoneNumber_Regex, R.string.err_phoneno);
                mAwesomeValidation.addValidation(SignUpActivity.this, R.id.signup_password, "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}", R.string.err_password);
                mAwesomeValidation.addValidation(SignUpActivity.this, R.id.signup_confirmpassword, R.id.signup_password, R.string.err_password_confirmation);

                // Validate user input
                if(mAwesomeValidation.validate()) {
                    registerUser();
                }
                else {
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

    private void registerUser() {
        String name = signupUsername.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String phone = signupPhoneNo.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            // Get the registered user
            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    // Store user details in Firebase Realtime Database using UID as key
                    databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                    UserInfo userInfo = new UserInfo(uid,name, email, phone);

                    databaseRef.setValue(userInfo).addOnCompleteListener(dbTask -> {
                        signupProgressBar.setVisibility(View.GONE);

                        if (dbTask.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show(); // Change activity to login page
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Failed to save user details: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    });
                }
            } else {
                Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}