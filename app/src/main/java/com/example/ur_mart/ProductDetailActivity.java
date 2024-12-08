package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProductDetailActivity extends AppCompatActivity {

    TextView detailProductName, detailProductId, detailDescription, detailPrice;
    ImageView detailImage;
    String imageURI;
    Button addtocartButton,detailBacktoshop, detailGoToCartButton;
    ImageButton incrementButton, decrementButton;
    TextView quantityText, totalAmountText;
    int minValue = 1;  // Minimum allowed value
    int maxValue = 100; // Maximum allowed value
    int quantity = 1; // Initial value of quantity
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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
        detailBacktoshop = findViewById(R.id.detailBacktoshop);
        detailGoToCartButton = findViewById(R.id.detailGoToCartButton);

        // set default value 0 to quantity
        quantityText.setText(String.valueOf(quantity));

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            detailProductName.setText(bundle.getString("ProductName"));
            detailProductId.setText(bundle.getString("ProductId"));
            detailDescription.setText(bundle.getString("Description"));
            detailPrice.setText("$ " + String.valueOf(bundle.getDouble("Price")));
            imageURI = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);

            // set default value 0.00 to total amount
            totalAmountText.setText("$ " + String.valueOf(bundle.getDouble("Price")));
        }

        // increase quantity button click event
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < maxValue) {
                    quantity++;
                    quantityText.setText(String.valueOf(quantity));
                    totalAmountText.setText("$ " + String.valueOf(getTotalAmount(quantity)));
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
                    totalAmountText.setText("$ " + String.valueOf(getTotalAmount(quantity))); // Round to fix 2 decimal place
                }
            }
        });

        // Add to cart button click event
        addtocartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputFields()){
                    saveAddtocart();
                }
            }
        });

        // Back to shop button click event
        detailBacktoshop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        // Checkout button click event
        detailGoToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputFields()) {
                    Intent intent = new Intent(ProductDetailActivity.this, ShoppingCartActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    public Boolean validateInputFields(){

        if(quantityText.getText().toString().equals("0")){
            Toast.makeText(ProductDetailActivity.this, "Quantity cannot be 0.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    public double getTotalAmount(int quantity){
        double totalAmountValue = Double.parseDouble(detailPrice.getText().toString().replace("$","").trim()) * quantity;
        return Math.round(totalAmountValue * 100.0) / 100.0;
    }

    public void saveAddtocart(){
        String cartItemId = UUID.randomUUID().toString(); // Generate a unique UUID
        String productId = detailProductId.getText().toString();
        String productName = detailProductName.getText().toString();
        String image = imageURI;
        int quantity = Integer.parseInt(quantityText.getText().toString());
        double price = Double.parseDouble(detailPrice.getText().toString().replace("$","").trim());
        double amount = Double.parseDouble(totalAmountText.getText().toString().replace("$","").trim());

        ShoppingCartItem item = new ShoppingCartItem(cartItemId, productId, productName, image, quantity, price, amount);

        if(currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("shopping_cart").child(currentUser.getUid()).child(item.getProductId());

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int existingQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                        int newQuantity = existingQuantity + item.getQuantity();

                        // Update the quantity field
                        cartRef.child("quantity").setValue(newQuantity)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProductDetailActivity.this, "Item quantity updated successfully.", Toast.LENGTH_SHORT).show();
                                        navigateToProductList();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProductDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }else {
                        // Product does not exist, insert it as a new item
                        cartRef.setValue(item)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProductDetailActivity.this, "Item added to cart successfully.", Toast.LENGTH_SHORT).show();
                                        navigateToProductList();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProductDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProductDetailActivity.this, "Error occurred while saving: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


//            cartRef.child(item.getCartItemId()).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()) {
//                        Toast.makeText(ProductDetailActivity.this, "Item added to cart successfully.", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(ProductDetailActivity.this, ProductListActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(ProductDetailActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    private void navigateToProductList() {
        Intent intent = new Intent(ProductDetailActivity.this, ProductListActivity.class);
        startActivity(intent);
        finish();
    }
}