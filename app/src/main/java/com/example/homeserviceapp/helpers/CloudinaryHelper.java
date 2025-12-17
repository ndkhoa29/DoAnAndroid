package com.example.homeserviceapp.helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class CloudinaryHelper {
    
    private static final String TAG = "CloudinaryHelper";
    

    public static void uploadImage(Context context, Uri imageUri, String folder, OnUploadListener listener) {
        try {
            MediaManager.get()
                    .upload(imageUri)
                    .option("folder", folder)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            double progress = (double) bytes / totalBytes * 100;
                            Log.d(TAG, "Upload progress: " + progress + "%");
                            listener.onProgress((int) progress);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            Log.d(TAG, "Upload successful: " + imageUrl);
                            listener.onSuccess(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Upload error: " + error.getDescription());
                            listener.onError(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d(TAG, "Upload rescheduled");
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            Log.e(TAG, "Exception during upload", e);
            listener.onError(e.getMessage());
        }
    }

    public static void uploadUserAvatar(Context context, Uri imageUri, String userId, OnUploadListener listener) {
        String folder = "users/avatars";
        uploadImage(context, imageUri, folder, listener);
    }

    public static void uploadServiceImage(Context context, Uri imageUri, String serviceId, OnUploadListener listener) {
        String folder = "services";
        uploadImage(context, imageUri, folder, listener);
    }

    public static void uploadReviewImage(Context context, Uri imageUri, OnUploadListener listener) {
        String folder = "reviews";
        uploadImage(context, imageUri, folder, listener);
    }
    

    public static String getOptimizedImageUrl(String imageUrl, int width, int height) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }
        
        // Check if it's a Cloudinary URL
        if (!imageUrl.contains("cloudinary.com")) {
            return imageUrl;
        }
        
        // Insert transformation parameters
        String transformation = "w_" + width + ",h_" + height + ",c_fill,q_auto,f_auto";
        return imageUrl.replace("/upload/", "/upload/" + transformation + "/");
    }

    public static String getThumbnailUrl(String imageUrl) {
        return getOptimizedImageUrl(imageUrl, 200, 200);
    }
    

    public interface OnUploadListener {
        void onProgress(int progress);
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
