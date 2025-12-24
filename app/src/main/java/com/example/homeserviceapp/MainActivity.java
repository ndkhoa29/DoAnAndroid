package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navMessage, navBooking, navProfile;
    private ImageView ivHome, ivMessage, ivBooking, ivProfile;
    private TextView tvHome, tvMessage, tvBooking, tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.navHome);
        navMessage = findViewById(R.id.navMessage);
        navBooking = findViewById(R.id.navBooking);
        navProfile = findViewById(R.id.navProfile);

        ivHome = findViewById(R.id.ivHome);
        ivMessage = findViewById(R.id.ivMessage);
        ivBooking = findViewById(R.id.ivBooking);
        ivProfile = findViewById(R.id.ivProfile);

        tvHome = findViewById(R.id.tvHome);
        tvMessage = findViewById(R.id.tvMessage);
        tvBooking = findViewById(R.id.tvBooking);
        tvProfile = findViewById(R.id.tvProfile);

        loadFragment(new HomeFragment());
        setActiveTab(0);

        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            setActiveTab(0);
        });

        navMessage.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
        });

        navBooking.setOnClickListener(v -> {
            loadFragment(new BookingFragment());
            setActiveTab(2);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            setActiveTab(3);
        });

        requestNotificationPermission();
    }
    
    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS) != 
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this, 
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    1001
                );
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    public void navigateToBooking() {
        loadFragment(new BookingFragment());
        setActiveTab(2);
    }
//    public void navigateToBooking() {
//        loadFragment(new BookingFragment());
//        setActiveTab(2);
//    }
//    public void navigateToBooking() {
//        loadFragment(new BookingFragment());
//        setActiveTab(2);
//    }

    private void setActiveTab(int position) {
        int gray = ContextCompat.getColor(this, R.color.gray);
        int active = ContextCompat.getColor(this, R.color.blue);

        ivHome.setColorFilter(gray);
        ivMessage.setColorFilter(gray);
        ivBooking.setColorFilter(gray);
        ivProfile.setColorFilter(gray);

        tvHome.setTextColor(gray);
        tvMessage.setTextColor(gray);
        tvBooking.setTextColor(gray);
        tvProfile.setTextColor(gray);

        switch (position) {
            case 0:
                ivHome.setColorFilter(active);
                tvHome.setTextColor(active);
                break;
            case 1:
                ivMessage.setColorFilter(active);
                tvMessage.setTextColor(active);
                break;
            case 2:
                ivBooking.setColorFilter(active);
                tvBooking.setTextColor(active);
                break;
            case 3:
                ivProfile.setColorFilter(active);
                tvProfile.setTextColor(active);
                break;
        }
    }
}
