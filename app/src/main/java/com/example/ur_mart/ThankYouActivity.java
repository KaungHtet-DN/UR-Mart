package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class ThankYouActivity extends AppCompatActivity {
    Button thankyouBackToShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thank_you);

        thankyouBackToShop = findViewById(R.id.thankyouBackToShop);

        thankyouBackToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThankYouActivity.this, ProductListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}