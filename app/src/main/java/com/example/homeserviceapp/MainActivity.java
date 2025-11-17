package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private AuthenticationPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // Khởi tạo Adapter
        pagerAdapter = new AuthenticationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Kết nối TabLayout với ViewPager2
        // TabLayoutMediator sẽ xử lý việc đồng bộ tab và trang
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Log in");
                    } else {
                        tab.setText("Sign up");
                    }
                }
        ).attach();
    }
}