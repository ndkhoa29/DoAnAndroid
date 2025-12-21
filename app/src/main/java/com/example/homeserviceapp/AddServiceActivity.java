package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserviceapp.helpers.CloudinaryHelper;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.UUID;

public class AddServiceActivity extends AppCompatActivity {

    private ImageView ivServiceImage;
    private TextInputEditText etName, etPrice, etDescription;
    private Spinner spinnerCategory, spinnerUnit;
    private Button btnSave;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private boolean isEditMode = false;
    private String editingServiceId = null;

    // Hardcode placeholder categories for MVP - REMOVED
    // private final String[] CATEGORIES = {"Dọn dẹp", "Sửa chữa", "Giặt ủi", "Nấu ăn", "Trông trẻ", "Chăm sóc người già"};
    private final String[] UNITS = {"/giờ", "/lần", "/m2", "/kg"};
    
    private java.util.List<String> categoryNames = new java.util.ArrayList<>();
    private java.util.List<String> categoryIds = new java.util.ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;


    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        android.util.Log.d("AddServiceActivity", "Selected URI: " + uri.toString());

                        android.widget.TextView tvImageHint = findViewById(R.id.tvImageHint);
                        if (tvImageHint != null) {
                            tvImageHint.setVisibility(View.GONE);
                        }
                        
                        ivServiceImage.setPadding(0, 0, 0, 0);
                        ivServiceImage.setBackground(null);

                        com.bumptech.glide.Glide.with(this)
                            .load(uri)
                            .override(800, 800) // Force downsampling to a reasonable size
                            .centerCrop()
                            .into(ivServiceImage);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupSpinners();
        loadCategories();
        setupListeners();

        if (getIntent().hasExtra("IS_EDIT_MODE") && getIntent().getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true;
            editingServiceId = getIntent().getStringExtra("SERVICE_ID");
            android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
            tvTitle.setText("Cập nhật Dịch vụ");
            btnSave.setText("Lưu Thay Đổi");
            loadServiceData(editingServiceId);
        }
    }
    
    private void loadServiceData(String serviceId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    ServiceItem service = documentSnapshot.toObject(ServiceItem.class);
                    if (service != null) {
                        etName.setText(service.getTitle());
                        etPrice.setText(String.valueOf(service.getPrice()));
                        etDescription.setText(service.getDescription());

                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUnit.getAdapter();
                        int unitPos = adapter.getPosition(service.getPriceUnit());
                        if (unitPos >= 0) spinnerUnit.setSelection(unitPos);

                        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
                            String imageUrl = service.getImageUrls().get(0);

                            android.widget.TextView tvImageHint = findViewById(R.id.tvImageHint);
                            if (tvImageHint != null) {
                                tvImageHint.setVisibility(View.GONE);
                            }
                            
                            ivServiceImage.setPadding(0, 0, 0, 0);
                            ivServiceImage.setBackground(null);
                            
                            com.bumptech.glide.Glide.with(this).load(imageUrl).into(ivServiceImage);
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initViews() {
        ivServiceImage = findViewById(R.id.ivServiceImage);
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);
    }
    
    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("categories")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                progressBar.setVisibility(View.GONE);
                categoryNames.clear();
                categoryIds.clear();
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    String id = doc.getId();
                    if (name != null) {
                        categoryNames.add(name);
                        categoryIds.add(id);
                    }
                }

                if (categoryNames.isEmpty()) {
                     categoryNames.add("Chưa có danh mục");
                     categoryIds.add("");
                }
                
                categoryAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupListeners() {
        findViewById(R.id.cardImage).setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveService());
        
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    


    private void saveService() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập tên dịch vụ");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Vui lòng nhập giá");
            return;
        }
        // In Edit Mode, image is optional if we already have one
        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (categoryIds.isEmpty()) {
             Toast.makeText(this, "Đang tải danh mục...", Toast.LENGTH_SHORT).show();
             return;
        }

        int price = Integer.parseInt(priceStr);
        String unit = spinnerUnit.getSelectedItem().toString();

        int selectedPosition = spinnerCategory.getSelectedItemPosition();
        if (selectedPosition < 0) selectedPosition = 0;
        String categoryId = categoryIds.get(selectedPosition);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Service ID
        String serviceId = isEditMode ? editingServiceId : UUID.randomUUID().toString();

        if (selectedImageUri != null) {
            // Upload new image
            CloudinaryHelper.uploadServiceImage(this, selectedImageUri, serviceId, new CloudinaryHelper.OnUploadListener() {
                @Override
                public void onProgress(int progress) {}

                @Override
                public void onSuccess(String imageUrl) {
                    saveToFirestore(serviceId, name, categoryId, price, unit, description, imageUrl);
                }

                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AddServiceActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
             saveToFirestore(serviceId, name, categoryId, price, unit, description, null);
        }
    }

    private void saveToFirestore(String serviceId, String name, String categoryId, int price, String unit, String description, String imageUrl) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("serviceId", serviceId);
        data.put("title", name);
        data.put("categoryId", categoryId);
        data.put("price", price);
        data.put("priceUnit", unit);
        data.put("description", description);
        data.put("updatedAt", Timestamp.now());
        
        if (imageUrl != null) {
            data.put("imageUrls", java.util.Collections.singletonList(imageUrl));
        }
        
        if (!isEditMode) {
            data.put("rating", 5.0);
            data.put("reviewCount", 0);
            data.put("viewCount", 0);
            data.put("bookingCount", 0);
            data.put("isActive", true);
            data.put("createdAt", Timestamp.now());
            if (auth.getCurrentUser() != null) {
                data.put("createdBy", auth.getCurrentUser().getUid());
            }
        }

        db.collection("services").document(serviceId).set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddServiceActivity.this, isEditMode ? "Đã cập nhật!" : "Đã thêm mới!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AddServiceActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
