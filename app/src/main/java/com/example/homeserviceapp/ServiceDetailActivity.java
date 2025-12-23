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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Review;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceDetailActivity extends AppCompatActivity {

    // Views
    private ImageView btnChat, btnCall, btnBack, imgService;
    private TextView tvTitle, tvPrice, tvRating, tvDescription,
            tvProviderName, tvReadMore, tvNoReviews;
    private Button btnBookNow;
    private LinearLayout providerSection;
    private RecyclerView rvReviews;

    // Data
    private FirebaseFirestore db;
    private String serviceId;
    private ServiceItem currentService;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // LẤY SERVICE ID
        serviceId = getIntent().getStringExtra("service_id");
        if (serviceId == null || serviceId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        loadServiceData();
    }

    private void initViews() {
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnBack = findViewById(R.id.btnBack);
        btnBookNow = findViewById(R.id.btnBookNow);

        imgService = findViewById(R.id.imgService);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvRating = findViewById(R.id.tvRating);
        tvDescription = findViewById(R.id.tvDescription);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvReadMore = findViewById(R.id.tvReadMore);
        tvNoReviews = findViewById(R.id.tvNoReviews);

        providerSection = findViewById(R.id.providerSection);

        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );

        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            startActivity(intent);
        });

        btnBookNow.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentService == null) {
                Toast.makeText(this, "Dữ liệu dịch vụ chưa sẵn sàng", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("service_id", serviceId);
            intent.putExtra("service_name", currentService.getTitle());
            intent.putExtra("service_price", currentService.getFormattedPrice());

            if (currentService.getImageUrls() != null
                    && !currentService.getImageUrls().isEmpty()) {
                intent.putExtra("service_image",
                        currentService.getImageUrls().get(0));
            }

            startActivity(intent);
        });
    }

    private void loadServiceData() {
        db.collection("services")
                .document(serviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    currentService = doc.toObject(ServiceItem.class);
                    if (currentService == null) {
                        Toast.makeText(this, "Không tìm thấy dữ liệu dịch vụ", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    displayServiceData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayServiceData() {
        tvTitle.setText(currentService.getTitle());
        tvPrice.setText(currentService.getFormattedPrice());
        tvDescription.setText(currentService.getDescription());

        // Read more
        tvDescription.setMaxLines(3);
        tvReadMore.setVisibility(View.GONE);

        tvDescription.post(() -> {
            if (tvDescription.getLineCount() > 3) {
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

        // Image
        if (currentService.getImageUrls() != null
                && !currentService.getImageUrls().isEmpty()) {
            Glide.with(this)
                    .load(currentService.getImageUrls().get(0))
                    .into(imgService);
        }

        tvProviderName.setText("Service Center AZ");
        providerSection.setVisibility(View.VISIBLE);

        loadReviews();
        calculateRating();
    }

    private void loadReviews() {
        db.collection("reviews")
                .whereEqualTo("serviceId", serviceId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    reviewList.clear();
                    for (var doc : snapshot.getDocuments()) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) reviewList.add(r);
                    }

                    if (reviewList.isEmpty()) {
                        rvReviews.setVisibility(View.GONE);
                        tvNoReviews.setVisibility(View.VISIBLE);
                    } else {
                        rvReviews.setVisibility(View.VISIBLE);
                        tvNoReviews.setVisibility(View.GONE);
                    }

                    reviewAdapter.notifyDataSetChanged();
                });
    }

    private void calculateRating() {
        db.collection("reviews")
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = snapshot.size();
                    if (count == 0) {
                        tvRating.setText("Chưa có đánh giá");
                        return;
                    }

                    double total = 0;
                    for (var doc : snapshot.getDocuments()) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) total += r.getRating();
                    }

                    double avg = total / count;
                    tvRating.setText(String.format(
                            Locale.getDefault(),
                            "%.1f (%d đánh giá)", avg, count
                    ));
                });
    }
}
