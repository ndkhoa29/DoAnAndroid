package com.example.homeserviceapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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

    private final String[] tabTitles = {"Tất cả", "Dọn vệ sinh", "Sửa chữa", "Giặt ủi", "Sơn sửa", "Đồ điên tử", "điều hòa "};

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

        registerFilterResultLauncher();

        ImageButton btnFilter = findViewById(R.id.btn_filter);

        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(TabServiceActivity.this, FilterActivity.class);
            filterActivityResultLauncher.launch(intent);
        });

        TabAdapter adapter = new TabAdapter(this, tabTitles);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabTitles[position]);
                }
        ).attach();

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

        if (tabTitles.length > 0) {
            tvHeaderTitle.setText(tabTitles[0]);
        }
    }

    
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

                            // Tag được tạo tự động cho Fragment trong ViewPager2 là "f" + position
                            Fragment currentFragment = getSupportFragmentManager()
                                    .findFragmentByTag("f" + viewPager.getCurrentItem());

                            if (currentFragment instanceof ServiceListFragment) {
                                ServiceListFragment serviceFragment = (ServiceListFragment) currentFragment;

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

    /**
     * Hiển thị menu popup để sắp xếp (từ main branch - giữ lại nếu cần)
     */
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