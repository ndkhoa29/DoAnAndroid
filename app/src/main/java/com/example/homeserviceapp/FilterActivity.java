package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import java.text.DecimalFormat;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private RangeSlider rangeSliderPrice;
    private TextView tvMinPrice, tvMaxPrice;
    private Button btnReset, btnApply;
    private ImageButton btnBack;
    private ChipGroup chipGroupCategory;

    // Cấu hình các hằng số lọc giá
    private final float INITIAL_MIN_PRICE = 0.0f;
    private final float INITIAL_MAX_PRICE = 1000000.0f; // Mặc định mở rộng tối đa để thấy sp 200k
    private final float SLIDER_MAX_LIMIT = 1000000.0f;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // 1. Ánh xạ View
        rangeSliderPrice = findViewById(R.id.range_slider_price);
        tvMinPrice = findViewById(R.id.tv_min_price);
        tvMaxPrice = findViewById(R.id.tv_max_price);
        btnReset = findViewById(R.id.btn_reset);
        btnApply = findViewById(R.id.btn_apply);
        btnBack = findViewById(R.id.btn_back);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        rangeSliderPrice.setValueFrom(0f);
        rangeSliderPrice.setValueTo(1000000.0f);
        rangeSliderPrice.setStepSize(10000.0f);
        // 2. Thiết lập giá trị ban đầu cho Slider và Nhãn giá
        rangeSliderPrice.setValues(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
        updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);

        // 3. Lắng nghe thay đổi trên RangeSlider
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            updatePriceLabels(values.get(0), values.get(1));
        });

        // 4. Xử lý nút Reset: Đưa mọi thứ về mặc định
        btnReset.setOnClickListener(v -> {
            rangeSliderPrice.setValues(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
            updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
            chipGroupCategory.clearCheck();
            // Mặc định chọn chip đầu tiên nếu cần
            chipGroupCategory.check(R.id.chip_cleaning);
            Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
        });

        // 5. Xử lý nút Apply: Gửi dữ liệu lọc về TabServiceActivity
        btnApply.setOnClickListener(v -> {
            List<Float> values = rangeSliderPrice.getValues();
            float min = values.get(0);
            float max = values.get(1);

            // Lấy danh sách các danh mục đã chọn từ Chips
            StringBuilder selectedCats = new StringBuilder();
            for (int id : chipGroupCategory.getCheckedChipIds()) {
                Chip chip = findViewById(id);
                if (chip != null) {
                    selectedCats.append(chip.getText().toString()).append(",");
                }
            }

            // Đóng gói dữ liệu gửi đi
            Intent resultIntent = new Intent();
            resultIntent.putExtra("MIN_PRICE", min);
            resultIntent.putExtra("MAX_PRICE", max);
            resultIntent.putExtra("SELECTED_CATEGORIES", selectedCats.toString());

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 6. Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Định dạng số tiền có dấu phân cách hàng nghìn (ví dụ: 200.000₫)
     */
    private void updatePriceLabels(float minVal, float maxVal) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvMinPrice.setText(formatter.format(minVal) + "₫");
        tvMaxPrice.setText(formatter.format(maxVal) + "₫");
    }
}