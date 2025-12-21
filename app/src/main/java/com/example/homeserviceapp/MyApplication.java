package com.example.homeserviceapp;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.example.homeserviceapp.helpers.NotificationHelper;
import com.example.homeserviceapp.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private ListenerRegistration notificationListener;
    private FirebaseFirestore db;
    private NotificationHelper notificationHelper;
    private String lastNotificationId = null;
    
    @Override
    public void onCreate() {
        super.onCreate();

        androidx.appcompat.app.AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        
        android.util.Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, " MyApplication.onCreate() CALLED");
        Log.d(TAG, "════════════════════════════════════════");

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

        MediaManager.init(this, config);
        Log.d(TAG, "Cloudinary initialized");
        
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firestore initialized");
        
        notificationHelper = new NotificationHelper(this);
        Log.d(TAG, "NotificationHelper created");
        
        Log.d(TAG, "Adding AuthStateListener...");
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "User logged in: " + user.getUid());
                Log.d(TAG, "   Email: " + user.getEmail());
                startNotificationListener(user.getUid());
            } else {
                Log.d(TAG, "User logged out (no current user)");
                stopNotificationListener();
            }
        });
        
        Log.d(TAG, "MyApplication initialization complete");
    }
    
    public void startNotificationListener(String userId) {
        if (notificationListener != null) {
            Log.d(TAG, "Listener already running, stopping old one");
            stopNotificationListener();
        }
        
        Log.d(TAG, "Starting notification listener for user: " + userId);

        long currentTime = System.currentTimeMillis();
        
        notificationListener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, " Listen failed: " + error.getMessage());
                    return;
                }
                
                Log.d(TAG, " Snapshot received, documents: " + (snapshots != null ? snapshots.size() : 0));
                
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Log.d(TAG, " Document change type: " + dc.getType());
                        
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Notification notification = dc.getDocument().toObject(Notification.class);
                            notification.setNotificationId(dc.getDocument().getId());

                            boolean isNewNotification = false;
                            if (notification.getCreatedAt() != null) {
                                long notifTime = notification.getCreatedAt().toDate().getTime();
                                isNewNotification = notifTime >= currentTime;
                                Log.d(TAG, " Notification time: " + notifTime + ", Current time: " + currentTime + ", Is new: " + isNewNotification);
                            } else {
                                isNewNotification = true;
                                Log.d(TAG, " No timestamp on notification, showing anyway");
                            }
                            
                            if (isNewNotification) {
                                Log.d(TAG, " Showing NEW notification: " + notification.getTitle());
                                notificationHelper.showLocalNotification(notification);
                            } else {
                                Log.d(TAG, " Skipping OLD notification: " + notification.getTitle());
                            }
                        }
                    }
                } else {
                    Log.d(TAG, " No notifications in snapshot");
                }
            });
        
        Log.d(TAG, "Notification listener started successfully");
    }
    
    public void stopNotificationListener() {
        if (notificationListener != null) {
            Log.d(TAG, "Stopping notification listener");
            notificationListener.remove();
            notificationListener = null;
        }
    }
}
