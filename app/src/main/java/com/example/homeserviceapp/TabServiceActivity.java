package com.example.homeserviceapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TabServiceActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvHeaderTitle;
    private FirebaseFirestore db;

    // Thay đổi từ mảng cứng sang List động
    private List<String> categoryNames = new ArrayList<>();
    private TabAdapter adapter;
    private TabLayoutMediator mediator;

    private ActivityResultLauncher<Intent> filterActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_service);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvHeaderTitle = findViewById(R.id.tv_header_title);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        registerFilterResultLauncher();

        ImageButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(TabServiceActivity.this, FilterActivity.class);
            filterActivityResultLauncher.launch(intent);
        });

        // Tải dữ liệu danh mục từ Firebase
        fetchCategoriesFromFirebase();

        // Lắng nghe sự kiện đổi Tab để cập nhật Title Header
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab != null) tvHeaderTitle.setText(tab.getText());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchCategoriesFromFirebase() {
        // Truy vấn: Chỉ lấy category đang hoạt động và sắp xếp theo thứ tự hiển thị
        db.collection("categories")
                .whereEqualTo("isActive", true)
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryNames.clear();
                        categoryNames.add("Tất cả"); // Luôn có tab mặc định

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            if (name != null) categoryNames.add(name);
                        }

                        setupViewPagerAndTabs();
                    } else {
                        Exception e = task.getException();
                        Log.e("FIRESTORE_DEBUG", "Lỗi: " + e.getMessage());
                        Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupViewPagerAndTabs() {
        // Khởi tạo Adapter với List vừa lấy được
        adapter = new TabAdapter(this, categoryNames);
        viewPager.setAdapter(adapter);

        // Liên kết TabLayout với ViewPager2
        if (mediator != null) mediator.detach();
        mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(categoryNames.get(position))
        );
        mediator.attach();

        // Cài đặt tiêu đề ban đầu
        if (!categoryNames.isEmpty()) {
            tvHeaderTitle.setText(categoryNames.get(0));
        }
    }

    private void registerFilterResultLauncher() {
        filterActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        float minPrice = result.getData().getFloatExtra("MIN_PRICE", 0.0f);
                        float maxPrice = result.getData().getFloatExtra("MAX_PRICE", 300.0f);
                        String categories = result.getData().getStringExtra("SELECTED_CATEGORIES");

                        // Lấy Fragment hiện tại qua Tag (ViewPager2 sử dụng format: f + ID)
                        Fragment currentFragment = getSupportFragmentManager()
                                .findFragmentByTag("f" + viewPager.getCurrentItem());

                        if (currentFragment instanceof ServiceListFragment) {
                            ((ServiceListFragment) currentFragment).applyFilter(minPrice, maxPrice, categories);
                        }
                    }
                }
        );
    }
}