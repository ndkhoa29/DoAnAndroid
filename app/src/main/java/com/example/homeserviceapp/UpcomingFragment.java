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

import com.example.homeserviceapp.models.Notification;
import com.example.homeserviceapp.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpcomingFragment extends Fragment {

    private RecyclerView rvUpcoming;
    private BookingAdapter adapter;
    private List<Booking> upcomingList;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        db = FirebaseFirestore.getInstance();
        rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        upcomingList = new ArrayList<>();
        adapter = new BookingAdapter(getContext(), upcomingList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onActionClick(Booking booking, String action) {
                if ("cancel".equals(action)) {
                    cancelBooking(booking);
                } else if ("pay".equals(action)) {
                    processPayment(booking);
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
        rvUpcoming.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Fetch Only Pending for "Chờ xác nhận" Tab
        Query query = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Pending")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle error
                return;
            }

            if (value != null) {
                upcomingList.clear();
                for (DocumentSnapshot doc : value) {
                    Booking booking = doc.toObject(Booking.class);
                    upcomingList.add(booking);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void cancelBooking(Booking booking) {
        // Implement cancel logic
        db.collection("bookings").document(booking.getBookingId())
                .update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    // Send notification to admins
                    sendCancelNotificationToAdmins(booking);
                });
    }
    
    private void sendCancelNotificationToAdmins(Booking booking) {
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
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String adminId = doc.getId();
                            
                            String title = "Đơn hàng bị hủy bởi " + finalUserName;
                            String message = "Đơn hàng " + bookingCode + " - " + booking.getServiceName() + " đã bị hủy";
                            
                            Notification notification = 
                                new Notification(
                                    adminId,
                                    title,
                                    message,
                                    "booking_cancelled_by_user",
                                    bookingCode
                                );
                            
                            db.collection("notifications").add(notification);
                        }
                    });
            });
    }

    private void processPayment(Booking booking) {
        // In real app, open Payment Gateway. Here, just confirm payment.
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Paid");
        // updates.put("paymentMethod", "Cash");
        
        db.collection("bookings").document(booking.getBookingId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    // Send notification to admins
                    sendPaymentNotificationToAdmins(booking);
                });
    }
    
    private void sendPaymentNotificationToAdmins(Booking booking) {
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
                            
                            String title = "Thanh toán từ " + finalUserName;
                            String message = "Đơn hàng " + bookingCode + " - " + booking.getServiceName() + " đã được thanh toán";
                            
                            com.example.homeserviceapp.models.Notification notification = 
                                new com.example.homeserviceapp.models.Notification(
                                    adminId,
                                    title,
                                    message,
                                    "booking_paid",
                                    bookingCode
                                );
                            
                            db.collection("notifications").add(notification);
                        }
                    });
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}