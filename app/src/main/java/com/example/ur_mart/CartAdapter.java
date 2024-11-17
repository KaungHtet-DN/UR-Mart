package com.example.ur_mart;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<ShoppingCartItem> cartItems;
    private Context context;
    private OnItemClickListener listener;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public CartAdapter(List<ShoppingCartItem> cartItems, Context context, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        ShoppingCartItem item = cartItems.get(position);
        holder.cartItemId.setText(item.getCartItemId());
        holder.productId.setText(item.getProductId());
        holder.productName.setText(item.getProductName());
        holder.price.setText(String.valueOf(item.getPrice()));
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.amount.setText(String.valueOf(item.getAmount()));

        holder.cartRemoveButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(item);
                removeCartItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setCartItems(List<ShoppingCartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public void removeCartItem(ShoppingCartItem cartItem){
        DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                .getReference("shopping_cart")
                .child(currentUser.getUid()) // Navigate to the user's cart
                .child(cartItem.cartItemId); // Navigate to the specific cart item

        // Remove the item
        cartItemRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, cartItem.getProductName() + " is removed from the cart.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Failed to delete the cart item", "CartItemID: " + cartItem.cartItemId, task.getException());
                    }
                });
    }

public static class CartViewHolder extends RecyclerView.ViewHolder {
   TextView cartItemId, productId, productName, price, quantity, amount ;
   ImageButton cartRemoveButton;

   public CartViewHolder(View itemView) {
       super(itemView);
       cartItemId = itemView.findViewById(R.id.cartItemId);
       productId = itemView.findViewById(R.id.cartProductId);
       productName = itemView.findViewById(R.id.cartProductName);
       price = itemView.findViewById(R.id.cartPrice);
       quantity = itemView.findViewById(R.id.cartQuantity);
       amount = itemView.findViewById(R.id.cartAmount);
       cartRemoveButton = itemView.findViewById(R.id.cartRemoveButton);
      }
   }

   public interface OnItemClickListener {
       void onRemoveClick(ShoppingCartItem item);
   }
}
