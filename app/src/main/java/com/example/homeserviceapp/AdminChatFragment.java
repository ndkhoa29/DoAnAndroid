package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.Conversation;

import java.util.ArrayList;
import java.util.List;

public class AdminChatFragment extends Fragment {

    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;
    private com.google.firebase.database.DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat, container, false);

        databaseReference = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("conversations");
        rvConversations = view.findViewById(R.id.recyclerViewConversations);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));

        conversationList = new ArrayList<>();
        adapter = new ConversationAdapter(getContext(), conversationList, conversation -> {

            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
            intent.putExtra("USER_NAME", conversation.getUserName());
            intent.putExtra("USER_AVATAR", conversation.getUserAvatar());
            startActivity(intent);
        });
        rvConversations.setAdapter(adapter);

        loadConversations();

        return view;
    }

    private void loadConversations() {
        databaseReference.orderByChild("lastMessageTime").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                conversationList.clear();
                for (com.google.firebase.database.DataSnapshot doc : snapshot.getChildren()) {
                    Conversation conversation = doc.getValue(Conversation.class);
                    if (conversation != null) {
                        conversation.setConversationId(doc.getKey());
                        conversationList.add(conversation);
                    }
                }
                java.util.Collections.sort(conversationList, (c1, c2) -> Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {

            }
        });
    }
}
