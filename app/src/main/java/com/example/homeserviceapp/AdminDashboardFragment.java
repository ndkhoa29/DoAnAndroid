package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminDashboardFragment extends Fragment {
    private android.widget.TextView tvTotalRevenue, tvPendingCount, tvActiveCount, tvCompletedCount, tvServicesCount;
    private android.widget.ProgressBar progressBar;
    private android.widget.Button btnManageBanners;
    private androidx.viewpager2.widget.ViewPager2 bannerViewPager;
    private BannerPagerAdapter bannerAdapter;
    private java.util.List<com.example.homeserviceapp.models.BannerItem> bannerList;
    private com.google.firebase.firestore.FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        initViews(view);
        loadBanners();
        loadStatistics();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvActiveCount = view.findViewById(R.id.tvActiveCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvServicesCount = view.findViewById(R.id.tvServicesCount);
        progressBar = view.findViewById(R.id.progressBar);
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        btnManageBanners = view.findViewById(R.id.btnManageBanners);

        bannerList = new java.util.ArrayList<>();
        bannerAdapter = new BannerPagerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        btnManageBanners.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), ManageBannersActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadBanners() {
        db.collection("banners")
            .orderBy("displayOrder")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) return;
                if (querySnapshot != null) {
                    bannerList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        com.example.homeserviceapp.models.BannerItem banner = doc.toObject(com.example.homeserviceapp.models.BannerItem.class);
                        if (banner != null) {
                            bannerList.add(banner);
                        }
                    }
                    bannerAdapter.notifyDataSetChanged();
                }
            });
    }
    
    private void loadStatistics() {
        db.collection("bookings").get().addOnSuccessListener(snap -> {
            java.util.Set<String> statuses = new java.util.HashSet<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : snap) {
                String s = doc.getString("status");
                if (s != null) statuses.add(s);
            }
        });

        db.collection("bookings")
            .whereIn("status", java.util.Arrays.asList("Pending", "pending", "Chờ xác nhận")) 
            .get()
            .addOnSuccessListener(querySnapshot -> {
                tvPendingCount.setText(String.valueOf(querySnapshot.size()));
            })
            .addOnFailureListener(e -> android.util.Log.e("AdminStats", "Pending error: " + e.getMessage()));
        
        db.collection("bookings")
            .whereIn("status", java.util.Arrays.asList("Confirmed", "confirmed", "In Progress", "in_progress", "InProgress", "Đang thực hiện"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                tvActiveCount.setText(String.valueOf(querySnapshot.size()));
            })
            .addOnFailureListener(e -> android.util.Log.e("AdminStats", "Active error: " + e.getMessage()));
        
        db.collection("bookings")
            .whereIn("status", java.util.Arrays.asList("Completed", "completed", "Hoàn thành", "Paid", "paid"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int completedCount = querySnapshot.size();
                tvCompletedCount.setText(String.valueOf(completedCount));
                
                long totalRevenue = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                    Long price = doc.getLong("totalPrice");
                    if (price == null) {
                        try {
                            Double dPrice = doc.getDouble("price");
                            if (dPrice != null) price = dPrice.longValue();
                        } catch (Exception e) {}
                    }
                    
                    if (price != null) {
                        totalRevenue += price;
                    }
                }
                tvTotalRevenue.setText(String.format("%,d đ", totalRevenue));
            })
            .addOnFailureListener(e -> android.util.Log.e("AdminStats", "Completed error: " + e.getMessage()));

        db.collection("services")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                tvServicesCount.setText(String.valueOf(querySnapshot.size()));
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(getContext(), "Lỗi tải thống kê", android.widget.Toast.LENGTH_SHORT).show();
            });
    }
}
