package com.example.ur_mart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartActivity extends AppCompatActivity {

    CardView shoppingCartTotalAmountCard;
    RecyclerView recyclerCart;
    CartAdapter cartAdapter;
    TextView totalAmountTextView, cartNoItem;
    Button shoppingCartClearAll, shoppingCartBackToShop, shoppingCartCheckOut;
    List<ShoppingCartItem> cartItems = new ArrayList<>();
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_cart);
        mAuth = FirebaseAuth.getInstance();

        shoppingCartTotalAmountCard = findViewById(R.id.shoppingCartTotalAmountCard);
        recyclerCart = findViewById(R.id.recyclerCart);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        cartNoItem = findViewById(R.id.cartNoItem);
        shoppingCartClearAll = findViewById(R.id.shoppingCartClearAll);
        shoppingCartBackToShop = findViewById(R.id.shoppingCartBackToShop);
        shoppingCartCheckOut = findViewById(R.id.shoppingCartCheckOut);

        cartAdapter = new CartAdapter(cartItems, this, item -> {
            cartItems.remove(item); // Remove from the list
            updateTotalAmount();
            cartAdapter.notifyDataSetChanged();
        });

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        // Get current login user
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            dbRef = FirebaseDatabase.getInstance().getReference("shopping_cart").child(currentUser.getUid());
            fetchCartItems(currentUser.getUid());
        }

        shoppingCartClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllItemsFromCart();
            }
        });

        shoppingCartBackToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        shoppingCartCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, CheckoutActivity.class);
                intent.putExtra("Amount", totalAmountTextView.getText() );
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateTotalAmount() {
        double totalAmount = 0.0;
        for (ShoppingCartItem item : cartItems) {
            totalAmount += item.getQuantity() * item.getPrice();
        }
        totalAmountTextView.setText("$ " + String.format("%.2f", totalAmount));
    }

    private void fetchCartItems(String userID) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartItems.clear();
                double totalAmount = 0.0;

                // Fetch the cart items from Firebase
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    ShoppingCartItem item = itemSnapshot.getValue(ShoppingCartItem.class);
                    cartItems.add(item);
                    totalAmount += item.getPrice() * item.getQuantity();
                }

                // Update the total amount TextView
                totalAmountTextView.setText("$ " + String.format("%.2f", totalAmount));

                if(cartItems == null || cartItems.size() == 0){
                    shoppingCartClearAll.setVisibility(View.GONE);
                    shoppingCartCheckOut.setVisibility(View.GONE);
                    recyclerCart.setVisibility(View.GONE);
                    shoppingCartTotalAmountCard.setVisibility(View.GONE);
                }else {
                    cartNoItem.setVisibility(View.GONE);
                }


                // Update the RecyclerView with the latest cart items
                cartAdapter.setCartItems(cartItems);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Failed to fetch cart items from firebase. " , databaseError.getMessage());
            }
        });
    }

    private void clearAllItemsFromCart(){
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("shopping_cart")
                .child(currentUser.getUid());

        // Remove the cart by userId
        cartRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ShoppingCartActivity.this, "Successfully clear the cart.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ShoppingCartActivity.this, ProductListActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ShoppingCartActivity.this, "Error occurred while clearing the cart.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}