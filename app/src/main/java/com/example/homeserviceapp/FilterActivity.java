package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.homeserviceapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.RangeSlider;

import java.util.List;
import java.util.Locale;

public class FilterActivity extends AppCompatActivity {

    private RangeSlider rangeSliderPrice;
    private TextView tvMinPrice, tvMaxPrice;
    private Button btnReset, btnApply;
    private ImageButton btnBack;

    // Định nghĩa giá trị mặc định
    private final float DEFAULT_MIN_PRICE = 20.0f;
    private final float DEFAULT_MAX_PRICE = 190.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter); // Thay đổi tên layout nếu cần

        // Ánh xạ View
        rangeSliderPrice = findViewById(R.id.range_slider_price);
        tvMinPrice = findViewById(R.id.tv_min_price);
        tvMaxPrice = findViewById(R.id.tv_max_price);
        btnReset = findViewById(R.id.btn_reset);
        btnApply = findViewById(R.id.btn_apply);
        btnBack = findViewById(R.id.btn_back);

        // Thiết lập giá trị ban đầu cho Range Slider (Đã được set trong XML nhưng cần cập nhật TextView)
        updatePriceLabels(DEFAULT_MIN_PRICE, DEFAULT_MAX_PRICE);

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish()); // Đóng Activity hiện tại

        // Xử lý Range Slider
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            float minVal = values.get(0);
            float maxVal = values.get(1);
            updatePriceLabels(minVal, maxVal);
        });

        // Xử lý nút Reset
        btnReset.setOnClickListener(v -> {
            // Đặt lại slider về giá trị mặc định
            rangeSliderPrice.setValues(DEFAULT_MIN_PRICE, DEFAULT_MAX_PRICE);
            updatePriceLabels(DEFAULT_MIN_PRICE, DEFAULT_MAX_PRICE);

            // TODO: Đặt lại trạng thái của tất cả các Chip (Chưa làm trong code này)
            Toast.makeText(this, "Filters Reset", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Apply
        btnApply.setOnClickListener(v -> {
            List<Float> values = rangeSliderPrice.getValues();
            String minPrice = String.format(Locale.US, "%.1f", values.get(0));
            String maxPrice = String.format(Locale.US, "%.1f", values.get(1));

            // TODO: Thu thập Category được chọn từ ChipGroup

            Toast.makeText(this, "Cài đặt mức tiền: Giá từ $" + minPrice + " đến $" + maxPrice, Toast.LENGTH_LONG).show();
            // Thêm logic để gửi kết quả lọc về Activity trước đó hoặc thực hiện tìm kiếm
            finish();
        });
    }

    /**
     * Cập nhật TextView hiển thị giá trị Min và Max
     */
    private void updatePriceLabels(float minVal, float maxVal) {
        // Định dạng thành 1 chữ số thập phân, ví dụ: $20.0
        tvMinPrice.setText(String.format(Locale.US, "$%.1f", minVal));
        tvMaxPrice.setText(String.format(Locale.US, "$%.1f", maxVal));
    }

    // TODO: Thêm hàm để đọc và thiết lập trạng thái của ChipGroup
}