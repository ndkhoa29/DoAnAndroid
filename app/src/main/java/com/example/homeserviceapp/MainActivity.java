package com.example.homeserviceapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            setActiveTab(0);
        });

        navMessage.setOnClickListener(v -> {
            loadFragment(new MessageFragment());
            setActiveTab(1);
        });

        navBooking.setOnClickListener(v -> {
            loadFragment(new BookingFragment());
            setActiveTab(2);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            setActiveTab(3);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void setActiveTab(int position) {
        ivHome.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        ivMessage.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        ivBooking.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        ivProfile.setColorFilter(ContextCompat.getColor(this, R.color.gray));

        tvHome.setTextColor(ContextCompat.getColor(this, R.color.gray));
        tvMessage.setTextColor(ContextCompat.getColor(this, R.color.gray));
        tvBooking.setTextColor(ContextCompat.getColor(this, R.color.gray));
        tvProfile.setTextColor(ContextCompat.getColor(this, R.color.gray));

        int activeColor = ContextCompat.getColor(this, R.color.blue);
        switch (position) {
            case 0:
                ivHome.setColorFilter(activeColor);
                tvHome.setTextColor(activeColor);
                break;
            case 1:
                ivMessage.setColorFilter(activeColor);
                tvMessage.setTextColor(activeColor);
                break;
            case 2:
                ivBooking.setColorFilter(activeColor);
                tvBooking.setTextColor(activeColor);
                break;
            case 3:
                ivProfile.setColorFilter(activeColor);
                tvProfile.setTextColor(activeColor);
                break;
        }
    }
}