package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homeserviceapp.models.Booking;
import com.example.homeserviceapp.AdminBookingAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminBookingsFragment extends Fragment {
    private TabLayout tabLayout;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private android.widget.ProgressBar progressBar;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_bookings, container, false);

        db = FirebaseFirestore.getInstance();
        
        tabLayout = view.findViewById(R.id.tabLayoutBookings);
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        progressBar = view.findViewById(R.id.progressBar);

        setupTabs();
        setupRecyclerView();
        
        // Load default tab (Pending)
        loadBookings("pending");

        return view;
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang làm"));
        tabLayout.addTab(tabLayout.newTab().setText("Lịch sử")); // Bao gồm completed và cancelled

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: loadBookings("pending"); break;
                    case 1: loadBookings("active"); break;
                    case 2: loadBookings("history"); break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        // Note: Adapter now expects Booking and OnBookingAdminActionListener
        adapter = new AdminBookingAdapter(getContext(), bookingList, this::handleBookingAction);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadBookings(String tabType) {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        
        progressBar.setVisibility(View.VISIBLE);
        bookingList.clear();
        adapter.notifyDataSetChanged();

        Query query;
        if (tabType.equals("history")) {
            // History: Paid, Cancelled, (and maybe TaskCompleted if user is very slow?) 
            // Let's stick to Paid and Cancelled
            query = db.collection("bookings")
                    .whereIn("status", Arrays.asList("Paid", "Cancelled"))
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        } else if (tabType.equals("active")) {
            // Active: InProgress, TaskCompleted (waiting for payment)
            query = db.collection("bookings")
                    .whereIn("status", Arrays.asList("InProgress", "TaskCompleted"))
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        } else {
            // Pending: Pending
            query = db.collection("bookings")
                    .whereEqualTo("status", "Pending")
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        }

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                bookingList.clear();
                for (DocumentSnapshot doc : value) {
                    Booking item = doc.toObject(Booking.class);
                    if (item != null) {
                        bookingList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void handleBookingAction(Booking booking, String action) {
        if ("view".equals(action)) {
            // Navigate to detail activity
            android.content.Intent intent = new android.content.Intent(getContext(), AdminBookingDetailActivity.class);
            intent.putExtra("BOOKING_ID", booking.getBookingId());
            startActivity(intent);
        } else if ("confirm_start".equals(action)) {
            updateBookingStatus(booking.getBookingId(), "InProgress");
        } else if ("confirm_finish".equals(action)) {
            updateBookingStatus(booking.getBookingId(), "TaskCompleted");
        }
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        android.util.Log.d("AdminBookingsFragment", "📝 Updating booking " + bookingId + " to status: " + newStatus);
        
        // Don't set PaymentStatus here. Payment is done by User in TaskCompleted -> Paid.
        
        db.collection("bookings").document(bookingId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Send notification to user
                    android.util.Log.d("AdminBookingsFragment", "✉️ Sending notification to user...");
                    sendBookingUpdateNotificationToUser(bookingId, newStatus);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    
    private void sendBookingUpdateNotificationToUser(String bookingId, String newStatus) {
        android.util.Log.d("AdminBookingsFragment", "🔍 Fetching booking details for notification...");
        
        // First get booking details
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener(bookingDoc -> {
                if (!bookingDoc.exists()) {
                    android.util.Log.e("AdminBookingsFragment", "❌ Booking not found");
                    return;
                }
                
                Booking booking = bookingDoc.toObject(Booking.class);
                if (booking == null || booking.getUserId() == null) {
                    android.util.Log.e("AdminBookingsFragment", "❌ Invalid booking data");
                    return;
                }
                
                String userId = booking.getUserId();
                String bookingCode = booking.getCode() != null ? booking.getCode() : booking.getBookingId();
                
                android.util.Log.d("AdminBookingsFragment", "👤 Sending to user: " + userId);
                
                // Determine notification message based on status
                NotificationData notifData = getNotificationData(newStatus, bookingCode, booking);
                if (notifData == null) {
                    android.util.Log.d("AdminBookingsFragment", "⏭️ No notification for status: " + newStatus);
                    return;
                }
                
                // Get admin's name
                String currentAdminId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (currentAdminId == null) {
                    android.util.Log.d("AdminBookingsFragment", "⚠️ No admin ID, sending without admin name");
                    saveNotificationToFirestore(userId, notifData.title, notifData.message, notifData.type, bookingCode);
                    return;
                }
                
                db.collection("users").document(currentAdminId).get()
                    .addOnSuccessListener(adminDoc -> {
                        String adminName = adminDoc.getString("fullName");
                        if (adminName == null || adminName.isEmpty()) {
                            adminName = "Admin";
                        }
                        
                        android.util.Log.d("AdminBookingsFragment", "👨‍💼 Admin name: " + adminName);
                        
                        String finalMessage = notifData.message + " bởi " + adminName;
                        saveNotificationToFirestore(userId, notifData.title, finalMessage, notifData.type, bookingCode);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AdminBookingsFragment", "❌ Failed to get admin name: " + e.getMessage());
                        saveNotificationToFirestore(userId, notifData.title, notifData.message, notifData.type, bookingCode);
                    });
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminBookingsFragment", "❌ Failed to fetch booking: " + e.getMessage());
            });
    }
    
    private static class NotificationData {
        String title;
        String message;
        String type;
        
        NotificationData(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
        }
    }
    
    private NotificationData getNotificationData(String status, String bookingCode, Booking booking) {
        switch (status) {
            case "InProgress":
                return new NotificationData(
                    "Đơn hàng đã được xác nhận",
                    "Đơn hàng " + bookingCode + " - " + booking.getServiceName() + " đang được xử lý",
                    "booking_confirmed"
                );
            case "TaskCompleted":
                return new NotificationData(
                    "Dịch vụ đã hoàn thành",
                    "Đơn hàng " + bookingCode + " đã hoàn thành. Vui lòng thanh toán",
                    "booking_completed"
                );
            case "Cancelled":
                return new NotificationData(
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng " + bookingCode + " đã bị hủy bởi admin",
                    "booking_cancelled"
                );
            default:
                return null;
        }
    }
    
    private void saveNotificationToFirestore(String userId, String title, String message, String type, String relatedId) {
        android.util.Log.d("AdminBookingsFragment", "💾 Saving notification to Firestore");
        android.util.Log.d("AdminBookingsFragment", "   Title: " + title);
        android.util.Log.d("AdminBookingsFragment", "   Message: " + message);
        android.util.Log.d("AdminBookingsFragment", "   UserId: " + userId);
        
        com.example.homeserviceapp.models.Notification notification = 
            new com.example.homeserviceapp.models.Notification(
                userId,
                title,
                message,
                type,
                relatedId
            );
        
        db.collection("notifications").add(notification)
            .addOnSuccessListener(documentReference -> {
                android.util.Log.d("AdminBookingsFragment", "✅ Notification saved successfully!");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminBookingsFragment", "❌ Failed to save notification: " + e.getMessage());
            });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
