package com.example.ur_mart;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    List<Product> productList;
    MyViewHolder.OnItemClickListener listener;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public MyAdapter(Context context, List<Product> productList, MyViewHolder.OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Product item = productList.get(position);
        Glide.with(context).load(productList.get(position).getImage()).into(holder.recImage);
        holder.recProductName.setText(productList.get(position).getProductName());
        holder.recProductId.setText(productList.get(position).getproductId());
        holder.recDescription.setText(productList.get(position).getDescription());
        holder.recPrice.setText("CAD " + String.valueOf(productList.get(position).getPrice()));

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("Image", productList.get(holder.getAdapterPosition()).getImage());
                intent.putExtra("ProductName", productList.get(holder.getAdapterPosition()).getProductName());
                intent.putExtra("ProductId", productList.get(holder.getAdapterPosition()).getproductId());
                intent.putExtra("Description", productList.get(holder.getAdapterPosition()).getDescription());
                intent.putExtra("Price", productList.get(holder.getAdapterPosition()).getPrice());

                context.startActivity(intent);
            }
        });

        holder.productListAddToCartButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(item);
                addToCart(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void searchProductList(ArrayList<Product> searchList){
        productList = searchList;
        notifyDataSetChanged();
    }

    public void addToCart(Product product){
        String cartItemId = UUID.randomUUID().toString(); // Generate a unique UUID

        double amount = 1 * product.getPrice();
        ShoppingCartItem item = new ShoppingCartItem(cartItemId, product.getproductId(), product.getProductName(), product.getImage(), 1, product.getPrice(), amount);

        if(currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("shopping_cart").child(currentUser.getUid()).child(item.getProductId());

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int existingQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                        int newQuantity = existingQuantity + item.getQuantity();

                        // Update the quantity field if product already existed in the cart
                        cartRef.child("quantity").setValue(newQuantity)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, product.getProductName() + " is added to the cart.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }else {
                        // Add product if does not exist in the cart
                        cartRef.setValue(item)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Item added to cart successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error occurred while saving: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recProductName, recProductId, recPrice, recDescription;
    CardView recCard;
    ImageButton productListAddToCartButton;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recProductName = itemView.findViewById(R.id.recProductName);
        recProductId = itemView.findViewById(R.id.recProductId);
        recDescription = itemView.findViewById(R.id.recDescription);
        recPrice = itemView.findViewById(R.id.recPrice);
        recCard = itemView.findViewById(R.id.recCard);
        productListAddToCartButton = itemView.findViewById(R.id.productListAddToCartButton);
    }

    public interface OnItemClickListener {
        void onAddToCartClick(Product product);
    }
}