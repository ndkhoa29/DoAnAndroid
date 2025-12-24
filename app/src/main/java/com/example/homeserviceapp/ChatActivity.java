package com.example.homeserviceapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.Conversation;
import com.example.homeserviceapp.models.Message;
import com.example.homeserviceapp.helpers.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnBack;
    
    private MessageAdapter adapter;
    private List<Message> messageList;
    
    private DatabaseReference databaseReference;
    private String currentUserId;
    private String conversationId; // Often same as userId for 1:1 support chat
    private String otherUserAvatar; // To store the avatar of the other person

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getIntent().hasExtra("CONVERSATION_ID")) {
            conversationId = getIntent().getStringExtra("CONVERSATION_ID");
            if (getIntent().hasExtra("USER_AVATAR")) {
                otherUserAvatar = getIntent().getStringExtra("USER_AVATAR");
            }
        } else {
            conversationId = currentUserId;
        }

        initViews();
        
        if (getIntent().hasExtra("USER_NAME")) {
            TextView tvChatTitle = findViewById(R.id.tvChatTitle);
            tvChatTitle.setText(getIntent().getStringExtra("USER_NAME"));
        }

        setupRecyclerView();
        setupListeners();
        listenForMessages();

        String currentEmail = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null 
            ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail() 
            : "No user";
        
        UserPreferences.isAdmin(this, isAdmin -> {
            android.util.Log.e("ChatActivity", "🔍 Current user email: " + currentEmail + ", isAdmin: " + isAdmin + ", conversationId: " + conversationId);
        });

        monitorConversationMetadata();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, currentUserId);
        if (otherUserAvatar != null) {
            adapter.setOtherUserAvatar(otherUserAvatar);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true); 
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);


        rvMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                rvMessages.postDelayed(() -> {
                    if (messageList.size() > 0) {
                        rvMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 100);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        android.util.Log.e("ChatActivity", "📤 sendMessage called, text: " + text);
        
        if (text.isEmpty()) return;

        etMessage.setText("");


        Message message = new Message(currentUserId, text);
        

        String messageId = databaseReference.child("messages").child(conversationId).push().getKey();
        if (messageId != null) {
            message.setMessageId(messageId);
            databaseReference.child("messages").child(conversationId).child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {

                        sendChatNotification(text);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin: " + e.getMessage(), Toast.LENGTH_LONG).show());

            updateConversationMetadata(text);
        }
    }

    private void updateConversationMetadata(String lastMessage) {
        DatabaseReference convRef = databaseReference.child("conversations").child(conversationId);
        

        if (currentUserId.equals(conversationId)) {
             FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                 .addOnSuccessListener(documentSnapshot -> {
                     String userName = documentSnapshot.getString("fullName");
                     String avatarUrl = documentSnapshot.getString("avatarUrl");
                     
                     Map<String, Object> updates = new HashMap<>();
                     updates.put("lastMessage", lastMessage);
                     updates.put("lastMessageTime", System.currentTimeMillis());
                     if (userName != null) updates.put("userName", userName);
                     if (avatarUrl != null) updates.put("userAvatar", avatarUrl);
                     updates.put("conversationId", conversationId);
                     
                     convRef.updateChildren(updates);
                 })
                 .addOnFailureListener(e -> {
                     Map<String, Object> updates = new HashMap<>();
                     updates.put("lastMessage", lastMessage);
                     updates.put("lastMessageTime", System.currentTimeMillis());
                     convRef.updateChildren(updates);
                 });
        } else {
             Map<String, Object> updates = new HashMap<>();
             updates.put("lastMessage", lastMessage);
             updates.put("lastMessageTime", System.currentTimeMillis());
             convRef.updateChildren(updates);
        }
    }

    private void monitorConversationMetadata() {
        databaseReference.child("conversations").child(conversationId).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 Conversation conversation = snapshot.getValue(Conversation.class);
                 if (conversation != null) {
                      if (!currentUserId.equals(conversationId)) {
                          otherUserAvatar = conversation.getUserAvatar();
                          adapter.setOtherUserAvatar(otherUserAvatar);
                      }

                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForMessages() {
        databaseReference.child("messages").child(conversationId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    message.setMessageId(snapshot.getKey());
                    messageList.add(message);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    
    private void sendChatNotification(String messageText) {
        android.util.Log.d("ChatNotif", "Attempting to send notification for message: " + messageText);
        
        // Check if current user is admin using callback
        UserPreferences.isAdmin(this, isAdmin -> {
            if (isAdmin) {
                // Admin sending to user - get admin's fullName
                String recipientId = conversationId;
                String currentAdminId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (currentAdminId == null) return;
                
                android.util.Log.d("ChatNotif", "Admin sending to user: " + recipientId);
                
                // Fetch admin's fullName from Firestore
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentAdminId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String adminName = documentSnapshot.getString("fullName");
                        if (adminName == null || adminName.isEmpty()) {
                            adminName = "Admin";
                        }
                        
                        android.util.Log.d("ChatNotif", "Admin name: " + adminName);
                        
                        com.example.homeserviceapp.helpers.NotificationHelper notificationHelper = 
                            new com.example.homeserviceapp.helpers.NotificationHelper(this);
                        notificationHelper.sendChatNotification(recipientId, adminName, messageText);
                        
                        android.util.Log.d("ChatNotif", "Notification sent via helper");
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ChatNotif", "Failed to get admin name: " + e.getMessage());
                        // Fallback to "Admin" if fetch fails
                        com.example.homeserviceapp.helpers.NotificationHelper notificationHelper = 
                            new com.example.homeserviceapp.helpers.NotificationHelper(this);
                        notificationHelper.sendChatNotification(recipientId, "Admin", messageText);
                    });
            } else {
                // User sending to admin - send notification to ALL admins
                android.util.Log.d("ChatNotif", "User message - sending notification to admins");
                sendNotificationToAdmins(messageText);
            }
        });
    }
    
    private void sendNotificationToAdmins(String messageText) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;
        
        com.google.firebase.firestore.FirebaseFirestore db = 
            com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // First, get the sender's (current user's) full name
        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener(userDoc -> {
                String senderName = userDoc.getString("fullName");
                if (senderName == null || senderName.isEmpty()) {
                    senderName = "Khách hàng";
                }
                
                final String userName = senderName;
                android.util.Log.d("ChatNotif", "Sender name: " + userName);
                
                // Then query all admin users and send notification to each
                db.collection("users")
                    .whereEqualTo("userType", "admin")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        com.example.homeserviceapp.helpers.NotificationHelper notificationHelper = 
                            new com.example.homeserviceapp.helpers.NotificationHelper(this);
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String adminId = doc.getId();
                            
                            android.util.Log.d("ChatNotif", "Sending notification to admin: " + adminId + " from user: " + userName);
                            notificationHelper.sendChatNotification(adminId, userName, messageText);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ChatNotif", "Failed to query admins: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("ChatNotif", "Failed to get user info: " + e.getMessage());
            });
    }
}
