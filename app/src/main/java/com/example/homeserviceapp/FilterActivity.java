package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup; // Import ChipGroup
import com.google.android.material.slider.RangeSlider;

import java.util.List;
import java.util.Locale;

public class FilterActivity extends AppCompatActivity {

    private RangeSlider rangeSliderPrice;
    private TextView tvMinPrice, tvMaxPrice;
    private Button btnReset, btnApply;
    private ImageButton btnBack;
    private ChipGroup chipGroupCategory; // Khai báo ChipGroup

    // Giá trị ban đầu và giá trị tối đa/tối thiểu từ XML
    // *Lưu ý: Bạn nên tạo mảng R.array.initial_slider_values trong values/arrays.xml*
    private final float INITIAL_MIN_PRICE = 20.0f;
    private final float INITIAL_MAX_PRICE = 193.0f;
    private final float SLIDER_MAX_LIMIT = 300.0f; // Giá trị valueTo="300.0"

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // 1. Ánh xạ Views
        rangeSliderPrice = findViewById(R.id.range_slider_price);
        tvMinPrice = findViewById(R.id.tv_min_price);
        tvMaxPrice = findViewById(R.id.tv_max_price);
        btnReset = findViewById(R.id.btn_reset);
        btnApply = findViewById(R.id.btn_apply);
        btnBack = findViewById(R.id.btn_back);
        chipGroupCategory = findViewById(R.id.chip_group_category);

        // Thiết lập nhãn giá ban đầu (Lấy từ RangeSlider nếu có, hoặc dùng giá trị mặc định)
        List<Float> currentValues = rangeSliderPrice.getValues();
        if (!currentValues.isEmpty()) {
            updatePriceLabels(currentValues.get(0), currentValues.get(1));
        } else {
            updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
        }

        // 2. Xử lý nút Back: Đóng Activity
        btnBack.setOnClickListener(v -> finish());

        // 3. Xử lý Range Slider: Cập nhật TextView khi kéo
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            float minVal = values.get(0);
            float maxVal = values.get(1);
            updatePriceLabels(minVal, maxVal);
        });

        // 4. Xử lý nút Reset: Đặt lại giá trị và Chip
        btnReset.setOnClickListener(v -> {
            // Đặt lại slider về giá trị ban đầu (20.0 và 193.0)
            rangeSliderPrice.setValues(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
            updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);

            // Đặt lại trạng thái của ChipGroup: Bỏ chọn tất cả
            chipGroupCategory.clearCheck();
            // Chọn chip "Dọn dẹp" (chip_cleaning) nếu nó là chip mặc định:
            chipGroupCategory.check(R.id.chip_cleaning);

            Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
        });

        // 5. Xử lý nút Apply: Gửi kết quả lọc về TabServiceActivity
        btnApply.setOnClickListener(v -> {
            // Thu thập dữ liệu giá
            float minPrice = rangeSliderPrice.getValues().get(0);
            float maxPrice = rangeSliderPrice.getValues().get(1);

            // Lấy danh sách ID của các Chip đã được chọn
            List<Integer> selectedChipIds = chipGroupCategory.getCheckedChipIds();
            String categories = getSelectedChipTexts(selectedChipIds); // Chuyển thành chuỗi

            // Tạo Intent kết quả
            Intent resultIntent = new Intent();

            // Gửi dữ liệu (sử dụng float để đảm bảo độ chính xác)
            resultIntent.putExtra("MIN_PRICE", minPrice);
            resultIntent.putExtra("MAX_PRICE", maxPrice);
            resultIntent.putExtra("SELECTED_CATEGORIES", categories);

            // Thiết lập kết quả thành công và đóng Activity
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Cập nhật TextView hiển thị giá trị Min và Max (Đã được làm tròn)
     */
    private void updatePriceLabels(float minVal, float maxVal) {
        // Định dạng thành số nguyên (%.0f) và thêm ký hiệu ₫
        tvMinPrice.setText(String.format(Locale.US, "%.0f₫", minVal));
        tvMaxPrice.setText(String.format(Locale.US, "%.0f₫", maxVal));
    }

    /**
     * Chuyển các ID Chip đã chọn thành một chuỗi (ví dụ: "Dọn dẹp, Sửa chữa")
     */
    private String getSelectedChipTexts(List<Integer> checkedIds) {
        StringBuilder selectedCategories = new StringBuilder();
        for (int id : checkedIds) {
            Chip chip = findViewById(id);
            if (chip != null) {
                selectedCategories.append(chip.getText().toString()).append(", ");
            }
        }
        // Xóa dấu phẩy và khoảng trắng cuối cùng nếu có
        if (selectedCategories.length() > 2) {
            selectedCategories.setLength(selectedCategories.length() - 2);
        }
        return selectedCategories.toString();
    }
}