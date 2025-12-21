package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Map;

public class ServiceDetailActivity extends AppCompatActivity {

    ImageView btnChat, btnCall, btnBack, imgService;
    TextView tvTitle, tvPrice, tvRating, tvDescription, tvProviderName, tvReadMore, tvNoReviews;
    Button btnBookNow;
    LinearLayout providerSection;
    androidx.recyclerview.widget.RecyclerView rvReviews;
    ReviewAdapter reviewAdapter;
    java.util.List<com.example.homeserviceapp.models.Review> reviewList;
    
    private FirebaseFirestore db;
    private String serviceId;
    private ServiceItem currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        db = FirebaseFirestore.getInstance();
        serviceId = getIntent().getStringExtra("SERVICE_ID");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết dịch vụ");
        }

        initViews();
        setupListeners();
        
        if (serviceId != null) {
            loadServiceData();
        } else {
            Toast.makeText(this, "Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnBack = findViewById(R.id.btnBack);
        imgService = findViewById(R.id.imgService);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvRating = findViewById(R.id.tvRating);
        tvDescription = findViewById(R.id.tvDescription);
        tvProviderName = findViewById(R.id.tvProviderName);
        providerSection = findViewById(R.id.providerSection);
        
        tvReadMore = findViewById(R.id.tvReadMore);
        
        btnChat.setVisibility(View.VISIBLE);
        btnCall.setVisibility(View.VISIBLE);
        btnBookNow.setVisibility(View.VISIBLE);
        
        rvReviews = findViewById(R.id.rvReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        rvReviews.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceDetailActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+1800123456"));
            startActivity(intent);
        });

        btnBookNow.setOnClickListener(v -> {
            try {
                Log.d("ServiceDetail", "Bắt đầu chuyển sang CalendarActivity");
                Intent intent = new Intent(ServiceDetailActivity.this, CalendarActivity.class);
                if (currentService != null) {
                    intent.putExtra("service_id", serviceId);
                    intent.putExtra("service_name", currentService.getTitle());
                    intent.putExtra("service_price", String.valueOf(currentService.getPrice()));
                    
                    if (currentService.getImageUrls() != null && !currentService.getImageUrls().isEmpty()) {
                        intent.putExtra("service_image", currentService.getImageUrls().get(0));
                    }
                }
                startActivity(intent);
                Log.d("ServiceDetail", "Đã gọi startActivity");
            } catch (Exception e) {
                Log.e("ServiceDetail", "Lỗi: " + e.getMessage());
                Toast.makeText(ServiceDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void loadServiceData() {
        db.collection("services").document(serviceId).get()
            .addOnSuccessListener(documentSnapshot -> {
                currentService = documentSnapshot.toObject(ServiceItem.class);
                if (currentService != null) {
                    displayServiceData();
                } else {
                    Toast.makeText(this, "Không tìm thấy dữ liệu dịch vụ", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void displayServiceData() {
        tvTitle.setText(currentService.getTitle());
        tvPrice.setText(currentService.getFormattedPrice());
        
        String desc = currentService.getDescription();
        tvDescription.setText(desc);
        
        tvReadMore.setVisibility(View.GONE);
        tvDescription.setMaxLines(3);
        
        tvDescription.post(() -> {
            if (tvDescription.getLineCount() > 3 || (desc != null && desc.length() > 100)) {
                tvReadMore.setVisibility(View.VISIBLE);
                tvReadMore.setOnClickListener(v -> {
                    if (tvReadMore.getText().toString().equals("Xem thêm")) {
                        tvDescription.setMaxLines(Integer.MAX_VALUE);
                        tvReadMore.setText("Thu gọn");
                    } else {
                        tvDescription.setMaxLines(3);
                        tvReadMore.setText("Xem thêm");
                    }
                });
            }
        });
        
        tvRating.setText("Đang tải...");
        
        if (currentService.getImageUrls() != null && !currentService.getImageUrls().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(currentService.getImageUrls().get(0))
                .into(imgService);
        }
        
        providerSection.setVisibility(View.VISIBLE);
        
        String providerName = "Service Center AZ"; 
        
        tvProviderName.setText(providerName);
        
        btnChat.setVisibility(View.VISIBLE);
        btnCall.setVisibility(View.VISIBLE);
        
        loadReviews();
        
        calculateAndDisplayRating();
    }
    
    private void loadReviews() {
        reviewList = new java.util.ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setAdapter(reviewAdapter);
        
        Log.d("ServiceDetail", "Loading reviews for serviceId: " + serviceId);
        
        db.collection("reviews")
            .whereEqualTo("serviceId", serviceId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d("ServiceDetail", "Query success, found " + queryDocumentSnapshots.size() + " reviews");
                reviewList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    com.example.homeserviceapp.models.Review review = doc.toObject(com.example.homeserviceapp.models.Review.class);
                    if (review != null) {
                        reviewList.add(review);
                        Log.d("ServiceDetail", "Added review: " + review.getComment());
                    }
                }
                

                
                if (reviewList.isEmpty()) {
                    Log.d("ServiceDetail", "No reviews found, showing empty state");
                    rvReviews.setVisibility(android.view.View.GONE);
                    tvNoReviews.setVisibility(android.view.View.VISIBLE);
                } else {
                    Log.d("ServiceDetail", "Showing " + reviewList.size() + " reviews");
                    rvReviews.setVisibility(android.view.View.VISIBLE);
                    tvNoReviews.setVisibility(android.view.View.GONE);
                }
                
                reviewAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e("ServiceDetail", "Error loading reviews: " + e.getMessage());
                tvNoReviews.setVisibility(android.view.View.VISIBLE);
                rvReviews.setVisibility(android.view.View.GONE);
                Toast.makeText(this, "Lỗi tải đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void calculateAndDisplayRating() {
        db.collection("reviews")
            .whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                if (count == 0) {
                    tvRating.setText("Chưa có đánh giá");
                    return;
                }
                
                double totalRating = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    com.example.homeserviceapp.models.Review review = doc.toObject(com.example.homeserviceapp.models.Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                    }
                }
                
                double averageRating = totalRating / count;
                tvRating.setText(String.format(Locale.getDefault(), "%.1f (%d Đánh giá)", averageRating, count));
            })
            .addOnFailureListener(e -> {
                tvRating.setText("5.0 (0 Đánh giá)");
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
