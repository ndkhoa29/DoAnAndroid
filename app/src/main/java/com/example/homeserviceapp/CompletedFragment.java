package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CompletedFragment extends Fragment {

    private RecyclerView rvCompleted;
    private BookingAdapter adapter;
    private List<Booking> completedList;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        db = FirebaseFirestore.getInstance();
        rvCompleted = view.findViewById(R.id.rvCompleted);
        rvCompleted.setLayoutManager(new LinearLayoutManager(getContext()));

        completedList = new ArrayList<>();
        adapter = new BookingAdapter(getContext(), completedList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onActionClick(Booking booking, String action) {
                if ("rate".equals(action)) {
                    showRatingDialog(booking);
                } else if ("pay".equals(action)) {
                    // Launch Payment Activity
                    android.content.Intent intent = new android.content.Intent(getContext(), PaymentMethodActivity.class);
                    intent.putExtra("booking_id", booking.getBookingId());
                    
                    // Calculate total (price + tax)
                    double price = booking.getPrice();
                    double tax = price * 0.1;
                    double total = price + tax;
                    
                    intent.putExtra("total_amount", total);
                    intent.putExtra("payment_method", "VNPay");
                    startActivity(intent);
                }
            }

            @Override
            public void onItemClick(Booking booking) {
                // Navigate to Detail
                android.content.Intent intent = new android.content.Intent(getContext(), ChiTietDonHangActivity.class);
                intent.putExtra("BOOKING_ID", booking.getBookingId());
                startActivity(intent);
            }
        });
        rvCompleted.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void showRatingDialog(Booking booking) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        android.widget.EditText etReview = dialogView.findViewById(R.id.etReview);

        builder.setPositiveButton("Gửi đánh giá", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String reviewText = etReview.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            saveReview(booking, rating, reviewText);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveReview(Booking booking, float rating, String comment) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Create review object
        com.example.homeserviceapp.models.Review review = new com.example.homeserviceapp.models.Review(
            booking.getBookingId(),
            userId,
            booking.getUserName(),
            booking.getServiceId(),
            booking.getServiceName(),
            rating,
            comment
        );

        // Save to reviews collection
        db.collection("reviews")
            .add(review)
            .addOnSuccessListener(documentReference -> {
                // Also update booking with hasReview flag
                db.collection("bookings").document(booking.getBookingId())
                    .update("hasReview", true, "rating", rating)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                        // Send notification to admins
                        sendReviewNotificationToAdmins(booking, rating);
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendReviewNotificationToAdmins(Booking booking, float rating) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        
        // Get user's fullName
        db.collection("users").document(userId).get()
            .addOnSuccessListener(userDoc -> {
                String userName = userDoc.getString("fullName");
                if (userName == null || userName.isEmpty()) {
                    userName = "Khách hàng";
                }
                
                final String finalUserName = userName;
                String bookingCode = booking.getCode() != null ? booking.getCode() : booking.getBookingId();
                
                // Query all admins
                db.collection("users")
                    .whereEqualTo("userType", "admin")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String adminId = doc.getId();
                            
                            String title = "Đánh giá mới từ " + finalUserName;
                            String message = booking.getServiceName() + " - " + rating + " sao";
                            
                            com.example.homeserviceapp.models.Notification notification = 
                                new com.example.homeserviceapp.models.Notification(
                                    adminId,
                                    title,
                                    message,
                                    "new_review",
                                    booking.getServiceId()
                                );
                            
                            db.collection("notifications").add(notification);
                        }
                    });
            });
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Fetch Active (InProgress, TaskCompleted) for "Đang thực hiện" Tab
        Query query = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereIn("status", java.util.Arrays.asList("InProgress", "TaskCompleted"))
                .orderBy("createdAt", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) return;

            if (value != null) {
                completedList.clear();
                for (DocumentSnapshot doc : value) {
                    Booking booking = doc.toObject(Booking.class);
                    completedList.add(booking);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}