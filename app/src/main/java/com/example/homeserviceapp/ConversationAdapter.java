package com.example.homeserviceapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Conversation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversationList;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(Context context, List<Conversation> conversationList, OnConversationClickListener listener) {
        this.context = context;
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.tvUserName.setText(conversation.getUserName());
        holder.tvLastMessage.setText(conversation.getLastMessage());

        if (conversation.getLastMessageTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new java.util.Date(conversation.getLastMessageTime())));
        } else {
            holder.tvTime.setText("");
        }

        if (conversation.getUserAvatar() != null && !conversation.getUserAvatar().isEmpty()) {
            Glide.with(context).load(conversation.getUserAvatar()).placeholder(R.drawable.img_profile_placeholder).into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.img_profile_placeholder);
        }

        // Unread Badge Logic
//        if (conversation.getUnreadCount() > 0) {
//            holder.tvUnreadCount.setVisibility(View.VISIBLE);
//            holder.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
//            holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
//            holder.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.black));
//        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
            holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
//        }

        holder.itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView ivUserAvatar;
        TextView tvUserName, tvLastMessage, tvTime, tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
