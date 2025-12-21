package com.example.homeserviceapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.ServiceItem;

import com.example.homeserviceapp.models.ServiceItem;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<ServiceItem> serviceList;
    private boolean isGrid = false;

    public ServiceAdapter(Context context, List<ServiceItem> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    public void setGridMode(boolean isGrid) {
        this.isGrid = isGrid;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service_card, parent, false);
        
        if (isGrid) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.setMarginEnd(0);
            view.setLayoutParams(layoutParams);
        }
        
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem service = serviceList.get(position);

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
        holder.tvServiceName.setText(service.getTitle());
        holder.tvPrice.setText(service.getFormattedPrice());

        holder.tvRate.setText("...");
        calculateRating(service.getServiceId(), holder.tvRate);

        String imageUrl = service.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String optimizedUrl = imageUrl;
            if (imageUrl.contains("cloudinary.com")) {
                optimizedUrl = com.example.homeserviceapp.helpers.CloudinaryHelper.getThumbnailUrl(imageUrl);
            }

            com.bumptech.glide.Glide.with(context)
                .load(optimizedUrl)
                .placeholder(R.drawable.ic_service_repo)
                .error(R.drawable.ic_service_repo)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(holder.ivServiceImage);
        } else {
            holder.ivServiceImage.setImageResource(R.drawable.ic_service_repo);
        }

        holder.itemView.setOnClickListener(v -> {
             Intent intent = new Intent(context, ServiceDetailActivity.class);
             intent.putExtra("SERVICE_ID", service.getServiceId());
             context.startActivity(intent);
        });
    }
    
    private void calculateRating(String serviceId, TextView tvRate) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                if (count == 0) {
                    tvRate.setText("0.0");
                    return;
                }
                
                double totalRating = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    com.example.homeserviceapp.models.Review review = doc.toObject(com.example.homeserviceapp.models.Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                    }
                }
                
                double averageRating = totalRating / count;
                tvRate.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
            })
            .addOnFailureListener(e -> {
                tvRate.setText("0.0");
            });
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;
        TextView tvServiceName, tvPrice, tvRate;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRate = itemView.findViewById(R.id.tvRate);
        }
    }
}