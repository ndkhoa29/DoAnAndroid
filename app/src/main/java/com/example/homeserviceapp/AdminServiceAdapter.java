package com.example.homeserviceapp;

import android.content.Context;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    private Context context;
    private List<ServiceItem> serviceList;
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditClick(com.example.homeserviceapp.models.ServiceItem service);
        void onDeleteClick(com.example.homeserviceapp.models.ServiceItem service);
        void onItemClick(com.example.homeserviceapp.models.ServiceItem service);
    }

    public AdminServiceAdapter(Context context, List<com.example.homeserviceapp.models.ServiceItem> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.example.homeserviceapp.models.ServiceItem service = serviceList.get(position);
        
        holder.tvName.setText(service.getTitle());
        holder.tvPrice.setText(service.getPrice() + "đ" + service.getPriceUnit());
        holder.tvDescription.setText(service.getDescription());

        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(service.getImageUrls().get(0))
                    .into(holder.ivImage);
        } else {
             holder.ivImage.setImageResource(R.mipmap.ic_launcher); // Changed from ivThumb to ivImage
        }

        // Click on item to view details
        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(service));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<ServiceItem> newList) {
        this.serviceList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvDescription;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivServiceImage);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
