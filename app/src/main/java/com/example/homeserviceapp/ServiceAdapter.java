package com.example.homeserviceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceItem> serviceList;
    private Context context;

    public ServiceAdapter(Context context, List<ServiceItem> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem currentItem = serviceList.get(position);

        holder.tvTitle.setText(currentItem.getTitle());
        String priceString = "$" + currentItem.getPrice() + "/h";
        holder.tvPrice.setText(priceString);
        holder.tvRating.setText(String.format(Locale.US, "%.1f", currentItem.getRating()));
        holder.imgService.setImageResource(currentItem.getImageResource());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
        });

        holder.iconHeart.setOnClickListener(v -> {
            Toast.makeText(context, "Toggled Favorite", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgService, iconHeart;
        TextView tvTitle, tvPrice, tvRating;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgService = itemView.findViewById(R.id.img_service);
            iconHeart = itemView.findViewById(R.id.icon_heart);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
        }
    }
}