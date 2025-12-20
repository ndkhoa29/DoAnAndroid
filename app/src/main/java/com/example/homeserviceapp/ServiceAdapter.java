package com.example.homeserviceapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.ServiceItem;

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

        // Set service name
        holder.tvTitle.setText(currentItem.getTitle());

        // Set price with formatted price
        String priceString = currentItem.getFormattedPrice();
        holder.tvPrice.setText(priceString);

        // Set rating
        holder.tvRating.setText(String.format(Locale.US, "%.1f", currentItem.getRating()));

        // Load image with Glide
        String imageUrl = currentItem.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_service)
                    .error(R.drawable.placeholder_service)
                    .into(holder.imgService);
        } else {
            holder.imgService.setImageResource(R.drawable.placeholder_service);
        }

        // QUAN TRỌNG: Click vào item để mở ServiceDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ServiceDetailActivity.class);

            // Truyền serviceId (quan trọng nhất!)
            intent.putExtra("serviceId", currentItem.getServiceId());
            intent.putExtra("serviceName", currentItem.getTitle());
            intent.putExtra("servicePrice", currentItem.getFormattedPrice());
            // Có thể thêm thông tin khác nếu cần

            context.startActivity(intent);
        });

        // Heart icon click - toggle favorite
        holder.iconHeart.setOnClickListener(v -> {
            // TODO: Implement add to favorites logic
            Toast.makeText(context, "Đã thêm vào yêu thích: " + currentItem.getTitle(),
                    Toast.LENGTH_SHORT).show();
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