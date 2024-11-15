package com.example.ur_mart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<Product> productList;

    public MyAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(productList.get(position).getImage()).into(holder.recImage);
        holder.recProductName.setText(productList.get(position).getProductName());
        holder.recProductId.setText(productList.get(position).getproductId());
        holder.recPrice.setText("$ " + productList.get(position).getPrice());

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
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recProductName, recProductId, recPrice;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recProductName = itemView.findViewById(R.id.recProductName);
        recProductId = itemView.findViewById(R.id.recProductId);
        recPrice = itemView.findViewById(R.id.recPrice);
        recCard = itemView.findViewById(R.id.recCard);
    }
}