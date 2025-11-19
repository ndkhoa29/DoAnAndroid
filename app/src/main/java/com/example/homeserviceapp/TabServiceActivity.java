package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TabServiceActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_service);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvHeaderTitle = findViewById(R.id.tv_header_title);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        ImageButton btnFilter = findViewById(R.id.btn_filter);

        btnFilter.setOnClickListener(v -> {
            showFilterMenu(v); // Gọi hàm hiển thị menu
        });

        // 1. CẬP NHẬT: Thêm "Tất cả" vào đầu mảng
        String[] tabTitles = {"Tất cả", "Dọn vệ sinh", "Sửa chữa", "Giặt ủi", "Sơn sửa", "Đồ điên tử", "điều hòa "};

        // 2. Thiết lập Adapter cho ViewPager2
        TabAdapter adapter = new TabAdapter(this, tabTitles);
        viewPager.setAdapter(adapter);

        // 3. Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabTitles[position]);
                }
        ).attach();

        // 4. Cập nhật tiêu đề dựa trên Tab đang chọn
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

        // Cài đặt tiêu đề ban đầu là "Tất cả"
        if (tabTitles.length > 0) {
            tvHeaderTitle.setText(tabTitles[0]);
        }
    }


    private void showFilterMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.filter_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentByTag("f" + viewPager.getCurrentItem());

            if (currentFragment instanceof ServiceListFragment) {
                ServiceListFragment serviceFragment = (ServiceListFragment) currentFragment;

                int itemId = item.getItemId();

                if (itemId == R.id.filter_price_low_high) {
                    serviceFragment.sortData("PRICE_ASC");
                    return true;
                } else if (itemId == R.id.filter_price_high_low) {
                    serviceFragment.sortData("PRICE_DESC");
                    return true;
                } else if (itemId == R.id.filter_rating_high) {
                    serviceFragment.sortData("RATING_DESC");
                    return true;
                } else if (itemId == R.id.filter_most_reviews) {
                    serviceFragment.sortData("REVIEWS_DESC");
                    return true;
                }
            }
            return false;
        });

        popupMenu.show();
    }
}