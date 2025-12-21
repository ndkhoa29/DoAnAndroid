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


    private final float INITIAL_MIN_PRICE = 0.0f;
    private final float INITIAL_MAX_PRICE = 500000.0f; // 500k
    private final float SLIDER_MAX_LIMIT = 2000000.0f; // 2 triệu

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

        // Setup RangeSlider
        rangeSliderPrice.setValueFrom(0.0f);
        rangeSliderPrice.setValueTo(SLIDER_MAX_LIMIT);
        rangeSliderPrice.setStepSize(10000.0f); // Bước nhảy 10k

        // Thiết lập giá trị mặc định
        rangeSliderPrice.setValues(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
        updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);

        // Load Categories
        loadCategories();

        // 2. Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());

        // 3. Xử lý Range Slider
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            updatePriceLabels(values.get(0), values.get(1));
        });

        // 4. Xử lý nút Reset
        btnReset.setOnClickListener(v -> {
            rangeSliderPrice.setValues(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
            updatePriceLabels(INITIAL_MIN_PRICE, INITIAL_MAX_PRICE);
            chipGroupCategory.clearCheck();
            Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
        });

        // 5. Xử lý nút Apply
        btnApply.setOnClickListener(v -> {
            float minPrice = rangeSliderPrice.getValues().get(0);
            float maxPrice = rangeSliderPrice.getValues().get(1);

            // Collect selected Chip Texts (Category Names)
            // Note: We use Category Name for filtering as ServiceItem usually stores CategoryId, 
            // but HomeFragment filter might check Name if ID not available. 
            // Let's return IDs if we tag them, or Names. Let's return Names for match with existing search logic,
            // or better: IDs. ServiceItem has categoryId. 
            // We will attach ID to Chip tag.
            
            StringBuilder selectedCategoryIds = new StringBuilder();
            List<Integer> checkedChipIds = chipGroupCategory.getCheckedChipIds();
            for (Integer id : checkedChipIds) {
                Chip chip = chipGroupCategory.findViewById(id);
                if (chip != null && chip.getTag() != null) {
                    selectedCategoryIds.append(chip.getTag().toString()).append(",");
                }
            }
            if (selectedCategoryIds.length() > 0) {
                selectedCategoryIds.setLength(selectedCategoryIds.length() - 1); // remove last comma
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("MIN_PRICE", minPrice);
            resultIntent.putExtra("MAX_PRICE", maxPrice);
            resultIntent.putExtra("SELECTED_CATEGORY_IDS", selectedCategoryIds.toString());

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadCategories() {
        chipGroupCategory.removeAllViews();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("categories")
            .orderBy("displayOrder")
            .get()
            .addOnSuccessListener(snapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                    com.example.homeserviceapp.models.Category cat = doc.toObject(com.example.homeserviceapp.models.Category.class);
                    if (cat != null) {
                        addChip(doc.getId(), cat.getName());
                    }
                }
            });
    }

    private void addChip(String id, String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setTag(id);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.chip_background_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, getTheme()));
        chipGroupCategory.addView(chip);
    }

    private void updatePriceLabels(float minVal, float maxVal) {
        tvMinPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", minVal));
        tvMaxPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", maxVal));
    }
}