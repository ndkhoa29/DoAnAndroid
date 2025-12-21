package com.example.homeserviceapp.helpers;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.homeserviceapp.models.*;

import java.util.ArrayList;
import java.util.List;


public class FirebaseHelper {
    
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SERVICES = "services";
    private static final String COLLECTION_BOOKINGS = "bookings";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private static final String COLLECTION_BANNERS = "banners";
    private static final String COLLECTION_FAVORITES = "favorites";
    private static final String COLLECTION_CONVERSATIONS = "conversations";
    
    private FirebaseFirestore db;
    
    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void getPopularServices(OnServicesLoadedListener listener) {
        db.collection(COLLECTION_SERVICES)
            .whereEqualTo("isActive", true)
            .whereArrayContains("tags", "popular")
            .orderBy("bookingCount", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ServiceItem> services = querySnapshot.toObjects(ServiceItem.class);

                for (int i = 0; i < services.size(); i++) {
                    services.get(i).setServiceId(querySnapshot.getDocuments().get(i).getId());
                }
                listener.onServicesLoaded(services);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    

    public void getTopRatedServices(OnServicesLoadedListener listener) {
        db.collection(COLLECTION_SERVICES)
           . whereEqualTo("isActive", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ServiceItem> services = querySnapshot.toObjects(ServiceItem.class);
                for (int i = 0; i < services.size(); i++) {
                    services.get(i).setServiceId(querySnapshot.getDocuments().get(i).getId());
                }
                listener.onServicesLoaded(services);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    

    public void getServicesByCategory(String categoryId, OnServicesLoadedListener listener) {
        db.collection(COLLECTION_SERVICES)
            .whereEqualTo("categoryId", categoryId)
            .whereEqualTo("isActive", true)
            .orderBy("bookingCount", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ServiceItem> services = querySnapshot.toObjects(ServiceItem.class);
                for (int i = 0; i < services.size(); i++) {
                    services.get(i).setServiceId(querySnapshot.getDocuments().get(i).getId());
                }
                listener.onServicesLoaded(services);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void getCategories(OnCategoriesLoadedListener listener) {
        db.collection(COLLECTION_CATEGORIES)
            .whereEqualTo("isActive", true)
            .orderBy("displayOrder")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Category> categories = querySnapshot.toObjects(Category.class);
                listener.onCategoriesLoaded(categories);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    

    public void getUserBookings(String userId, OnBookingsLoadedListener listener) {
        db.collection(COLLECTION_BOOKINGS)
            .whereEqualTo("userId", userId) // Changed from customerId to userId matching Booking model
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    listener.onError(error.getMessage());
                    return;
                }
                
                List<Booking> bookings = new ArrayList<>();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null) {
                            // booking.setBookingId(doc.getId()); // ID should allow be inside object if saved correctly
                            bookings.add(booking);
                        }
                    }
                }
                listener.onBookingsLoaded(bookings);
            });
    }

    public void getBookingsByStatus(String userId, String status, OnBookingsLoadedListener listener) {
        db.collection(COLLECTION_BOOKINGS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    listener.onError(error.getMessage());
                    return;
                }
                
                List<Booking> bookings = new ArrayList<>();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                }
                listener.onBookingsLoaded(bookings);
            });
    }
    

    public void getServiceReviews(String serviceId, OnReviewsLoadedListener listener) {
        db.collection(COLLECTION_REVIEWS)
            .whereEqualTo("serviceId", serviceId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Review> reviews = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot) {
                    Review review = doc.toObject(Review.class);
                    if (review != null) {
                        review.setReviewId(doc.getId());
                        reviews.add(review);
                    }
                }
                listener.onReviewsLoaded(reviews);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    

    public void getUserNotifications(String userId, OnNotificationsLoadedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    listener.onError(error.getMessage());
                    return;
                }
                
                List<Notification> notifications = new ArrayList<>();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notif.setNotificationId(doc.getId());
                            notifications.add(notif);
                        }
                    }
                }
                listener.onNotificationsLoaded(notifications);
            });
    }

    
    public interface OnServicesLoadedListener {
        void onServicesLoaded(List<ServiceItem> services);
        void onError(String error);
    }
    
    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);
        void onError(String error);
    }
    
    public interface OnBookingsLoadedListener {
        void onBookingsLoaded(List<Booking> bookings);
        void onError(String error);
    }
    
    public interface OnReviewsLoadedListener {
        void onReviewsLoaded(List<Review> reviews);
        void onError(String error);
    }
    
    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }
}
