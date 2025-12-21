package com.example.homeserviceapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_AVATAR = "user_avatar";
    
    private SharedPreferences prefs;
    
    public UserPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserData(String name, String email, String phone, String avatarUrl) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_AVATAR, avatarUrl)
            .apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }
    
    public String getUserAvatar() {
        return prefs.getString(KEY_USER_AVATAR, "");
    }

    public void clearUserData() {
        prefs.edit().clear().apply();
    }

    public boolean hasCachedData() {
        return !getUserName().isEmpty();
    }

    public static void isAdmin(android.content.Context context, OnAdminCheckListener listener) {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            listener.onResult(false);
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore db = 
            com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userType = documentSnapshot.getString("userType");
                    listener.onResult("admin".equalsIgnoreCase(userType));
                } else {
                    listener.onResult(false);
                }
            })
            .addOnFailureListener(e -> listener.onResult(false));
    }
    

    public boolean isAdminCached() {
        // You can cache userType in SharedPreferences when user logs in
        return prefs.getString("user_type", "customer").equalsIgnoreCase("admin");
    }

    public void saveUserType(String userType) {
        prefs.edit().putString("user_type", userType).apply();
    }
    
    public interface OnAdminCheckListener {
        void onResult(boolean isAdmin);
    }
}
