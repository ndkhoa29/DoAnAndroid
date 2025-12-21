package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class CancelledFragment extends Fragment {

    private RecyclerView rvCancelled;
    private BookingAdapter adapter;
    private List<Booking> cancelledList;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled, container, false);

        db = FirebaseFirestore.getInstance();
        rvCancelled = view.findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(getContext()));

        cancelledList = new ArrayList<>();
        adapter = new BookingAdapter(getContext(), cancelledList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onActionClick(Booking booking, String action) {
                if ("rate".equals(action)) {
                    showRatingDialog(booking);
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
        rvCancelled.setAdapter(adapter);

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
                android.widget.Toast.makeText(getContext(), "Vui lòng chọn số sao", android.widget.Toast.LENGTH_SHORT).show();
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
                        android.widget.Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", android.widget.Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            });
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Fetch History (Paid, Cancelled) for "Lịch sử" Tab
        Query query = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereIn("status", java.util.Arrays.asList("Paid", "Cancelled"))
                .orderBy("createdAt", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) return;

            if (value != null) {
                cancelledList.clear();
                for (DocumentSnapshot doc : value) {
                    Booking booking = doc.toObject(Booking.class);
                    cancelledList.add(booking);
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