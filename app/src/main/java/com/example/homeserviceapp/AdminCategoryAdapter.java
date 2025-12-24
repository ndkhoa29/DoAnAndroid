package com.example.homeserviceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Category;
import com.example.homeserviceapp.helpers.CloudinaryHelper;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    public AdminCategoryAdapter(Context context, List<Category> categoryList, OnCategoryActionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        String iconUrl = category.getIconUrl();
        if (iconUrl == null || iconUrl.isEmpty()) {
            iconUrl = category.getImageUrl();
        }

        if (iconUrl != null && !iconUrl.isEmpty()) {
            if (iconUrl.contains("cloudinary")) {
                iconUrl = CloudinaryHelper.getThumbnailUrl(iconUrl);
            }
            Glide.with(context)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_service_repo) // Use a generic fallback icon
                    .into(holder.ivCategoryIcon);
        } else {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_service_repo);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon, btnEdit, btnDelete;
        TextView tvCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
