package com.example.homeserviceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.BannerItem;

import java.util.List;

public class ManageBannerAdapter extends RecyclerView.Adapter<ManageBannerAdapter.ViewHolder> {
    private Context context;
    private List<BannerItem> bannerList;
    private OnBannerActionListener listener;

    public interface OnBannerActionListener {
        void onEdit(BannerItem banner);
        void onDelete(BannerItem banner);
    }

    public ManageBannerAdapter(Context context, List<BannerItem> bannerList, OnBannerActionListener listener) {
        this.context = context;
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerItem banner = bannerList.get(position);
        
        holder.tvTitle.setText(banner.getTitle());
        holder.tvDisplayOrder.setText("Thứ tự: " + banner.getDisplayOrder());

        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(banner.getImageUrl())
                    .into(holder.ivImage);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(banner));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(banner));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDisplayOrder;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivBannerImage);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvDisplayOrder = itemView.findViewById(R.id.tvDisplayOrder);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
