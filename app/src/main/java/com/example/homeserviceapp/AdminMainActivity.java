package com.example.homeserviceapp;

import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminMainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new AdminDashboardFragment();
                toolbar.setTitle("Tổng quan");
            } else if (itemId == R.id.nav_bookings) {
                selectedFragment = new AdminBookingsFragment();
                toolbar.setTitle("Quản lý Đơn hàng");
            } else if (itemId == R.id.nav_services) {
                selectedFragment = new AdminServicesFragment();
                toolbar.setTitle("Quản lý Dịch vụ");
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = new AdminChatFragment();
                toolbar.setTitle("Tin nhắn");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminDashboardFragment())
                    .commit();
            toolbar.setTitle("Tổng quan");
        }
        
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.admin_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performLogout() {
        if (auth.getCurrentUser() != null) {
            db.collection("users").document(auth.getCurrentUser().getUid())
                    .update("isOnline", false)
                    .addOnCompleteListener(task -> {
                        auth.signOut();
                        startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
                        finish();
                    });
        } else {
             auth.signOut();
             startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
             finish();
        }
    }
}
