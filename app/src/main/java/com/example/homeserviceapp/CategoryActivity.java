package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.homeserviceapp.R;

public class CategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        setCategoryClickListener(findViewById(R.id.category_cleaning), "Cleaning");
        setCategoryClickListener(findViewById(R.id.category_repairing), "Repairing");
        setCategoryClickListener(findViewById(R.id.category_laundry), "Laundry");
        setCategoryClickListener(findViewById(R.id.category_painting), "Painting");
        setCategoryClickListener(findViewById(R.id.category_electric), "Electric");
        setCategoryClickListener(findViewById(R.id.category_air), "Air");
        setCategoryClickListener(findViewById(R.id.category_wifi), "Wifi");
        setCategoryClickListener(findViewById(R.id.category_wifi_2), "Wifi (2)"); // Nếu bạn muốn phân biệt
        setCategoryClickListener(findViewById(R.id.category_plumber), "Plumber");
        setCategoryClickListener(findViewById(R.id.category_kitchen), "Kitchen");
    }

    private void setCategoryClickListener(LinearLayout categoryLayout, String categoryName) {
        categoryLayout.setOnClickListener(v -> {
            Toast.makeText(CategoryActivity.this, "Selected: " + categoryName, Toast.LENGTH_SHORT).show();
        });
    }
}