package com.example.homeserviceapp;


import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeserviceapp.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);
        
        ImageView btnQuayLai = findViewById(R.id.btnQuayLai);
        rvNotifications = findViewById(R.id.rvNotifications);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        setupRecyclerView();
        loadNotifications();

        btnQuayLai.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList, (notification, position) -> {
            markAsRead(notification, position);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvNotifications.setLayoutManager(layoutManager);
        rvNotifications.setAdapter(notificationAdapter);
    }
    
    private void loadNotifications() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            
            android.util.Log.d("ThongBaoActivity", "📱 Loading notifications for user: " + userId);
            
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("ThongBaoActivity", "✅ Query successful, documents: " + queryDocumentSnapshots.size());
                    
                    notificationList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            if ("chat_message".equals(notification.getType())) {
                                continue;
                            }
                            
                            notification.setNotificationId(doc.getId());
                            notificationList.add(notification);
                            android.util.Log.d("ThongBaoActivity", "  📬 " + notification.getType() + ": " + notification.getTitle());
                        }
                    }
                    
                    java.util.Collections.sort(notificationList, (n1, n2) -> {
                        if (n1.getCreatedAt() == null || n2.getCreatedAt() == null) return 0;
                        return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                    });
                    
                    notificationAdapter.notifyDataSetChanged();
                    
                    android.util.Log.d("ThongBaoActivity", "📊 Total notifications loaded: " + notificationList.size());
                    
                    if (notificationList.isEmpty()) {
                        android.widget.Toast.makeText(this, "Không có thông báo nào", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ThongBaoActivity", "❌ Error loading notifications: " + e.getMessage(), e);
                    android.widget.Toast.makeText(this, "Lỗi: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
        } else {
            android.util.Log.e("ThongBaoActivity", "❌ User not logged in");
            android.widget.Toast.makeText(this, "Chưa đăng nhập", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void markAsRead(Notification notification, int position) {
        if (!notification.isRead()) {
            db.collection("notifications")
                .document(notification.getNotificationId())
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    notification.setRead(true);
                    notificationAdapter.notifyItemChanged(position);
                });
        }
    }
}

