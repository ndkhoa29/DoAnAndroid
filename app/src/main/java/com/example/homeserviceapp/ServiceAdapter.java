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

import com.bumptech.glide.Glide; // Import thư viện Glide
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

        // Hiển thị giá (Giả định giá lưu trên Firestore là kiểu int)
        String priceString = "$" + currentItem.getPrice() + "/h";
        holder.tvPrice.setText(priceString);

        // Định dạng rating 1 chữ số thập phân
        holder.tvRating.setText(String.format(Locale.US, "%.1f", currentItem.getRating()));

        // --- SỬ DỤNG GLIDE ĐỂ LOAD ẢNH TỪ FIREBASE URL ---
        Glide.with(context)
                .load(currentItem.getImageUrl()) // Lấy link từ Firestore
                .placeholder(R.drawable.placeholder_image) // Ảnh hiển thị khi đang tải
                .error(R.drawable.error_image) // Ảnh hiển thị nếu link bị hỏng
                .centerCrop()
                .into(holder.imgService);

        // Sự kiện Click vào item
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Sự kiện Click vào trái tim (Yêu thích)
        holder.iconHeart.setOnClickListener(v -> {
            Toast.makeText(context, "Đã thêm vào yêu thích: " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
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