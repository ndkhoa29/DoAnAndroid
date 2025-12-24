package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.BannerItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageBannersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ManageBannerAdapter adapter;
    private List<BannerItem> bannerList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_banners);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewBanners);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bannerList = new ArrayList<>();
        adapter = new ManageBannerAdapter(this, bannerList, new ManageBannerAdapter.OnBannerActionListener() {
            @Override
            public void onEdit(BannerItem banner) {
                Intent intent = new Intent(ManageBannersActivity.this, AddBannerActivity.class);
                intent.putExtra("IS_EDIT_MODE", true);
                intent.putExtra("BANNER_ID", banner.getBannerId());
                startActivity(intent);
            }

            @Override
            public void onDelete(BannerItem banner) {
                new androidx.appcompat.app.AlertDialog.Builder(ManageBannersActivity.this)
                        .setTitle("Xóa banner")
                        .setMessage("Bạn có chắc muốn xóa banner này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteBanner(banner.getBannerId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddBanner);
        fabAdd.setOnClickListener(v -> {
            // Check banner count limit
            if (bannerList.size() >= 3) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Giới hạn banner")
                        .setMessage("Chỉ được tối đa 3 banner. Vui lòng xóa banner cũ trước khi thêm mới.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
            startActivity(new Intent(this, AddBannerActivity.class));
        });

        loadBanners();
    }

    private void loadBanners() {
        db.collection("banners")
                .orderBy("displayOrder")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        bannerList.clear();
                        for (DocumentSnapshot doc : value) {
                            BannerItem banner = doc.toObject(BannerItem.class);
                            if (banner != null) {
                                bannerList.add(banner);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void deleteBanner(String bannerId) {
        db.collection("banners").document(bannerId).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa banner", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
