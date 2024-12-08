package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class ThankYouActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    Button thankyouBackToShop, thankyouLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thank_you);
        mAuth = FirebaseAuth.getInstance();

        thankyouBackToShop = findViewById(R.id.thankyouBackToShop);
        thankyouLogOut = findViewById(R.id.thankyouLogOut);

        thankyouBackToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThankYouActivity.this, ProductListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        thankyouLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}