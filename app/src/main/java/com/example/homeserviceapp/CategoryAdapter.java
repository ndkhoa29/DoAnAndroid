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
import com.bumptech.glide.Glide;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categoryList;
    private Context context;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());

        // Nếu iconUrl là một link ảnh (https://...)
        Glide.with(context)
                .load(category.getIconUrl())
                .placeholder(R.drawable.ic_cleaning) // Ảnh mặc định khi đang tải
                .into(holder.imgIcon);

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Selected: " + category.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_category);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}