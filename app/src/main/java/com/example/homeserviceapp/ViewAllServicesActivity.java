package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ViewAllServicesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvHeaderTitle;
    private RecyclerView rvServices;
    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> serviceList;
    private String type; // "RATED", "POPULAR", or "CATEGORY"
    private String categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_services);

        type = getIntent().getStringExtra("TYPE");
        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        initViews();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rvServices = findViewById(R.id.rvServices);

        btnBack.setOnClickListener(v -> finish());

        if ("RATED".equals(type)) {
            tvHeaderTitle.setText("Đánh giá cao");
        } else if ("POPULAR".equals(type)) {
            tvHeaderTitle.setText("Phổ biến nhất");
        } else if ("CATEGORY".equals(type) && categoryName != null) {
            tvHeaderTitle.setText(categoryName);
        } else {
            tvHeaderTitle.setText("Dịch vụ");
        }
    }

    private void setupRecyclerView() {
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(this, serviceList);
        serviceAdapter.setGridMode(true); // Enable Grid Layout Mode (Match Parent Width)
        
        rvServices.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Add Spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        if (spacingInPixels == 0) spacingInPixels = 32; // Fallback 16dp ish if dimen not found (approx 32px for xhdpi)
        
        // Convert dp to px manually if dimen ignored/missing
        int spacingDp = 16;
        int spacingPx = (int) (spacingDp * getResources().getDisplayMetrics().density);
        
        rvServices.addItemDecoration(new com.example.homeserviceapp.helpers.GridSpacingItemDecoration(2, spacingPx, true));
        rvServices.setAdapter(serviceAdapter);
    }

    private void loadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("services");

        if ("RATED".equals(type)) {
            query = query.orderBy("rating", Query.Direction.DESCENDING);
        } else if ("POPULAR".equals(type)) {
            query = query.orderBy("bookingCount", Query.Direction.DESCENDING);
        } else if ("CATEGORY".equals(type) && categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId);
            // Toast.makeText(this, "Đang tải dịch vụ: " + categoryName, Toast.LENGTH_SHORT).show();
        }

        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                serviceList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ServiceItem service = doc.toObject(ServiceItem.class);
                    // DEBUG: Removed isActive check temporarily to debug data issues
                    if (service != null /* && service.isActive() */) { 
                        service.setServiceId(doc.getId());
                        serviceList.add(service);
                    }
                }
                
                if (serviceList.isEmpty() && "CATEGORY".equals(type)) {
                    Toast.makeText(this, "Không tìm thấy dịch vụ nào cho danh mục này", Toast.LENGTH_SHORT).show();
                } else if ("CATEGORY".equals(type)) {
                    // Toast.makeText(this, "Đã tìm thấy " + serviceList.size() + " dịch vụ", Toast.LENGTH_SHORT).show();
                }
                
                // Fallback sort if index is missing and query returns unsorted results or if explicit sort fails
                if ("RATED".equals(type)) {
                     serviceList.sort((s1, s2) -> Double.compare(s2.getRating(), s1.getRating()));
                } else if ("POPULAR".equals(type)) {
                     serviceList.sort((s1, s2) -> Integer.compare(s2.getBookingCount(), s1.getBookingCount()));
                }

                serviceAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                // If orderBy fails (e.g., missing index), fall back to client-side sorting
                // For Category query, it shouldn't fail on index usually as it's a simple equality
                db.collection("services").get().addOnSuccessListener(retrySnapshots -> {
                     serviceList.clear();
                     for (com.google.firebase.firestore.DocumentSnapshot doc : retrySnapshots) {
                         ServiceItem service = doc.toObject(ServiceItem.class);
                         if (service != null /* && service.isActive() */) {
                              boolean matches = true;
                              if ("CATEGORY".equals(type) && categoryId != null) {
                                  // Use exact matching for category ID
                                  matches = categoryId.equals(service.getCategoryId());
                              }
                              
                              if (matches) {
                                  service.setServiceId(doc.getId());
                                  serviceList.add(service);
                              }
                         }
                     }
                     if ("RATED".equals(type)) {
                          serviceList.sort((s1, s2) -> Double.compare(s2.getRating(), s1.getRating()));
                     } else if ("POPULAR".equals(type)) {
                          serviceList.sort((s1, s2) -> Integer.compare(s2.getBookingCount(), s1.getBookingCount()));
                     }
                     serviceAdapter.notifyDataSetChanged();
                });
                // Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
