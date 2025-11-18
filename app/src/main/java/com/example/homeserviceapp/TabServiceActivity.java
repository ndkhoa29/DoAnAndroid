package com.example.homeserviceapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TabServiceActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvHeaderTitle;

    // Sửa lỗi: Khai báo mảng tabTitles ở cấp độ lớp
    private final String[] tabTitles = {"Tất cả", "Dọn vệ sinh", "Sửa chữa", "Giặt ủi", "Sơn sửa", "Đồ điên tử", "điều hòa "};

    // Khai báo: Launcher để mở FilterActivity
    private ActivityResultLauncher<Intent> filterActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_service);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvHeaderTitle = findViewById(R.id.tv_header_title);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 1. Đăng ký Launcher trước khi sử dụng
        registerFilterResultLauncher();

        ImageButton btnFilter = findViewById(R.id.btn_filter);

        // 2. GẮN CHỨC NĂNG: Mở FilterActivity
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(TabServiceActivity.this, FilterActivity.class);
            filterActivityResultLauncher.launch(intent);
        });

        // 3. Thiết lập Adapter cho ViewPager2
        TabAdapter adapter = new TabAdapter(this, tabTitles);
        viewPager.setAdapter(adapter);

        // 4. Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabTitles[position]);
                }
        ).attach();

        // 5. Cập nhật tiêu đề dựa trên Tab đang chọn
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab != null) {
                    tvHeaderTitle.setText(tab.getText());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 6. Cài đặt tiêu đề ban đầu là "Tất cả"
        if (tabTitles.length > 0) {
            tvHeaderTitle.setText(tabTitles[0]);
        }
    }

    /**
     * Đăng ký ActivityResultLauncher để nhận kết quả từ FilterActivity và áp dụng lên Fragment hiện tại.
     */
    private void registerFilterResultLauncher() {
        filterActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            float minPrice = data.getFloatExtra("MIN_PRICE", 0.0f);
                            float maxPrice = data.getFloatExtra("MAX_PRICE", 300.0f);
                            // Nhận chuỗi danh mục đã chọn từ FilterActivity
                            String categories = data.getStringExtra("SELECTED_CATEGORIES");

                            // 1. Lấy Fragment hiện tại
                            // Tag được tạo tự động cho Fragment trong ViewPager2 là "f" + position
                            Fragment currentFragment = getSupportFragmentManager()
                                    .findFragmentByTag("f" + viewPager.getCurrentItem());

                            // 2. Áp dụng bộ lọc nếu Fragment là ServiceListFragment
                            if (currentFragment instanceof ServiceListFragment) {
                                ServiceListFragment serviceFragment = (ServiceListFragment) currentFragment;

                                // Gọi hàm lọc đã được định nghĩa trong ServiceListFragment
                                serviceFragment.applyFilter(minPrice, maxPrice, categories);

                                Toast.makeText(this,
                                        "Đã áp dụng lọc cho Tab: " + tabTitles[viewPager.getCurrentItem()],
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );
    }
}