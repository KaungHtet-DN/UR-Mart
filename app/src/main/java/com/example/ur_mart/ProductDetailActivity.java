package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

    TextView detailProductName, detailProductId, detailDescription, detailPrice;
    ImageView detailImage;
    Button addtocartButton, checkoutButton;
    ImageButton incrementButton, decrementButton;
    TextView quantityText, totalAmountText;
    int minValue = 0;  // Minimum allowed value
    int maxValue = 100; // Maximum allowed value
    int quantity = 0; // Initial value of quantity
    double totalAmount = 0.00; // Initial value of total amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        detailProductName = findViewById(R.id.detailProductName);
        detailProductId = findViewById(R.id.detailProductId);
        detailDescription = findViewById(R.id.detailDesc);
        detailPrice = findViewById(R.id.detailPrice);
        detailImage = findViewById(R.id.detailImage);
        incrementButton = findViewById(R.id.incrementButton);
        decrementButton = findViewById(R.id.decrementButton);
        quantityText = findViewById(R.id.qunatityText);
        totalAmountText = findViewById(R.id.detailTotalAmount);
        addtocartButton = findViewById(R.id.detailAddToCartButton);
        checkoutButton = findViewById(R.id.detailCheckoutButton);

        // set default value 0 to quantity
        quantityText.setText(String.valueOf(quantity));

        // set default value 0.00 to total amount
        totalAmountText.setText(String.valueOf(totalAmount));

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            detailProductName.setText(bundle.getString("ProductName"));
            detailProductId.setText(bundle.getString("ProductId"));
            detailDescription.setText(bundle.getString("Description"));
            detailPrice.setText(bundle.getString("Price"));

            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }

        // increase quantity button click event
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < maxValue) {
                    quantity++;
                    quantityText.setText(String.valueOf(quantity));
                    totalAmountText.setText(String.valueOf(getTotalAmount(quantity)));
                }
            }
        });

        // decrease quantity button click event
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > minValue) {
                    quantity--;
                    quantityText.setText(String.valueOf(quantity));
                    totalAmountText.setText(String.valueOf(getTotalAmount(quantity))); // Round to fix 2 decimal place
                }
            }
        });

        // Add to cart button click event
        addtocartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pending to implement
            }
        });

        // Checkout button click event
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });

    }

    public double getTotalAmount(int quantity){
        double totalAmountValue = Double.parseDouble(detailPrice.getText().toString()) * quantity;
        return Math.round(totalAmountValue * 100.0) / 100.0;
    }
}