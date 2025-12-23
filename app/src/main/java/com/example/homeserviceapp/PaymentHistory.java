package com.example.homeserviceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeserviceapp.models.Notification;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import android.util.Log;

public class PaymentHistory extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        if (notification.getCreatedAt() != null) {
            String time = dateFormat.format(notification.getCreatedAt().toDate());
            holder.tvTime.setText(time);
        }

        if (notification.isRead()) {
            holder.itemView.setAlpha(0.6f);
            holder.ivUnreadDot.setVisibility(View.GONE);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.ivUnreadDot.setVisibility(View.VISIBLE);
        }

        try {
            if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
                holder.ivIcon.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(notification.getImageUrl())
                        .placeholder(R.drawable.ic_notification)
                        .error(R.drawable.ic_notification)
                        .into(holder.ivIcon);
            } else {
                // Use Glide for local resource too to handle vector correctly
                Glide.with(context)
                        .load(R.drawable.ic_notification)
                        .into(holder.ivIcon);
            }
        } catch (Exception e) {
            Log.e("NotificationAdapter", "Error loading icon: " + e.getMessage());
            // Fallback that is super safe
            holder.ivIcon.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public void removeItem(int position) {
        notificationList.remove(position);
        notifyItemRemoved(position);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        View ivUnreadDot;
        TextView tvTitle, tvMessage, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}
