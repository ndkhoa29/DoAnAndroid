package com.example.homeserviceapp.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.homeserviceapp.ChatActivity;
import com.example.homeserviceapp.R;
import com.example.homeserviceapp.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationHelper {
    
    private static final String CHANNEL_ID = "home_service_notifications";
    private static final String CHANNEL_NAME = "Home Service Notifications";
    private static final String CHANNEL_DESC = "Notifications for bookings, chat, and updates";
    
    private Context context;
    private FirebaseFirestore db;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    

    public void sendChatNotification(String recipientUserId, String senderName, String message) {
        android.util.Log.d("NotificationHelper", "Creating notification for user: " + recipientUserId);
        
        Notification notification = new Notification(
            recipientUserId,
            "Tin nhắn mới từ " + senderName,
            message,
            "chat_message",
            recipientUserId // chatId same as userId for this implementation
        );
        
        android.util.Log.d("NotificationHelper", "Saving notification to Firestore");
        saveNotificationToFirestore(notification);
    }
    

    public void sendBookingConfirmedNotification(String userId, String bookingCode) {
        Notification notification = new Notification(
            userId,
            "Đơn hàng đã được xác nhận",
            "Đơn hàng " + bookingCode + " đang được xử lý",
            "booking_confirmed",
            bookingCode
        );
        
        saveNotificationToFirestore(notification);
    }

    public void sendBookingCompletedNotification(String userId, String bookingCode) {
        Notification notification = new Notification(
            userId,
            "Dịch vụ đã hoàn thành",
            "Đơn hàng " + bookingCode + " đã hoàn thành. Vui lòng thanh toán",
            "booking_completed",
            bookingCode
        );
        
        saveNotificationToFirestore(notification);
    }
    

    private void saveNotificationToFirestore(Notification notification) {
        android.util.Log.d("NotificationHelper", "Attempting to save to Firestore");
        
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(documentReference -> {
                notification.setNotificationId(documentReference.getId());
                documentReference.update("notificationId", documentReference.getId());
                android.util.Log.d("NotificationHelper", "✅ Notification saved successfully: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("NotificationHelper", "❌ Failed to save notification: " + e.getMessage());
            });
    }
    

    public void showLocalNotification(Notification notification) {
        try {
            android.util.Log.d("NotificationHelper", "🔔 Showing notification: " + notification.getTitle());
            android.util.Log.d("NotificationHelper", "   Message: " + notification.getMessage());
            android.util.Log.d("NotificationHelper", "   Type: " + notification.getType());
            
            Intent intent = getIntentForNotification(notification);
            android.util.Log.d("NotificationHelper", "   Intent created for: " + intent.getComponent());
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notification.getNotificationId() != null ? notification.getNotificationId().hashCode() : 0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            android.util.Log.d("NotificationHelper", "   PendingIntent created");
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});
            
            android.util.Log.d("NotificationHelper", "   Notification builder created");
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            
            int notifId = notification.getNotificationId() != null 
                ? notification.getNotificationId().hashCode() 
                : (int) System.currentTimeMillis();
            
            android.util.Log.d("NotificationHelper", "   Notification ID: " + notifId);
            android.util.Log.d("NotificationHelper", "   Attempting to notify...");
            
            notificationManager.notify(notifId, builder.build());
            android.util.Log.d("NotificationHelper", "✅ Notification displayed successfully");
            
        } catch (SecurityException e) {
            android.util.Log.e("NotificationHelper", "❌ Permission not granted: " + e.getMessage());
        } catch (Exception e) {
            android.util.Log.e("NotificationHelper", "❌ Error showing notification: " + e.getMessage(), e);
        }
    }

    private Intent getIntentForNotification(Notification notification) {
        Intent intent;
        
        switch (notification.getType()) {
            case "chat_message":
                intent = new Intent(context, ChatActivity.class);
                if (notification.getRelatedId() != null) {
                    intent.putExtra("CONVERSATION_ID", notification.getRelatedId());
                    
                    String userName = notification.getTitle();
                    if (userName != null && userName.startsWith("Tin nhắn mới từ ")) {
                        userName = userName.substring("Tin nhắn mới từ ".length());
                        intent.putExtra("USER_NAME", userName);
                    }
                }
                break;
            case "booking_confirmed":
            case "booking_completed":
                intent = new Intent(context, ChatActivity.class);
                break;
            default:
                intent = new Intent(context, ChatActivity.class);
                break;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
    

    public void markAsRead(String notificationId) {
        db.collection("notifications")
            .document(notificationId)
            .update("read", true)
            .addOnFailureListener(e -> {
                // Log error
            });
    }

    public void getUnreadCount(String userId, OnCountListener listener) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                listener.onCount(queryDocumentSnapshots.size());
            })
            .addOnFailureListener(e -> {
                listener.onCount(0);
            });
    }
    
    public interface OnCountListener {
        void onCount(int count);
    }
}
