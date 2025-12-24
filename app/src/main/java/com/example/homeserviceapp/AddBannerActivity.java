package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.homeserviceapp.helpers.CloudinaryHelper;
import com.example.homeserviceapp.models.BannerItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.UUID;

public class AddBannerActivity extends AppCompatActivity {

    private ImageView ivBannerPreview;
    private TextInputEditText edtTitle, edtDisplayOrder;
    private Button btnSelectImage, btnSave;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private boolean isEditMode = false;
    private String editingBannerId = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        com.bumptech.glide.Glide.with(this)
                                .load(uri)
                                .override(800, 800)
                                .centerCrop()
                                .into(ivBannerPreview);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_banner);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        setupListeners();

        // Check for edit mode
        if (getIntent().hasExtra("IS_EDIT_MODE") && getIntent().getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true;
            editingBannerId = getIntent().getStringExtra("BANNER_ID");
            toolbar.setTitle("Cập nhật Banner");
            btnSave.setText("Lưu Thay Đổi");
            loadBannerData(editingBannerId);
        }
    }

    private void initViews() {
        ivBannerPreview = findViewById(R.id.ivBannerPreview);
        edtTitle = findViewById(R.id.edtTitle);
        edtDisplayOrder = findViewById(R.id.edtDisplayOrder);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveBanner());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImageLauncher.launch(intent);
    }

    private void loadBannerData(String bannerId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("banners").document(bannerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    BannerItem banner = documentSnapshot.toObject(BannerItem.class);
                    if (banner != null) {
                        edtTitle.setText(banner.getTitle());
                        edtDisplayOrder.setText(String.valueOf(banner.getDisplayOrder()));

                        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
                            com.bumptech.glide.Glide.with(this)
                                    .load(banner.getImageUrl())
                                    .into(ivBannerPreview);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveBanner() {
        String title = edtTitle.getText().toString().trim();
        String displayOrderStr = edtDisplayOrder.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        if (TextUtils.isEmpty(displayOrderStr)) {
            edtDisplayOrder.setError("Vui lòng nhập thứ tự");
            return;
        }

        int displayOrder = Integer.parseInt(displayOrderStr);

        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh banner", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSave(title, displayOrder);
        } else {
            updateBannerData(title, displayOrder, null);
        }
    }

    private void uploadImageAndSave(String title, int displayOrder) {
        CloudinaryHelper.uploadImage(this, selectedImageUri, "banners", new CloudinaryHelper.OnUploadListener() {
            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onSuccess(String imageUrl) {
                updateBannerData(title, displayOrder, imageUrl);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AddBannerActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateBannerData(String title, int displayOrder, String newImageUrl) {
        String bannerId = isEditMode ? editingBannerId : ("banner_" + UUID.randomUUID().toString().substring(0, 8));

        BannerItem banner = new BannerItem();
        banner.setBannerId(bannerId);
        banner.setTitle(title);
        banner.setDisplayOrder(displayOrder);
        banner.setLinkType("none");

        if (isEditMode) {
            // Load existing data first to keep imageUrl if not changing
            db.collection("banners").document(bannerId).get()
                    .addOnSuccessListener(doc -> {
                        BannerItem existing = doc.toObject(BannerItem.class);
                        if (newImageUrl != null) {
                            banner.setImageUrl(newImageUrl);
                        } else if (existing != null) {
                            banner.setImageUrl(existing.getImageUrl());
                        }
                        banner.setUpdatedAt(Timestamp.now());
                        banner.setCreatedAt(existing != null ? existing.getCreatedAt() : Timestamp.now());

                        saveBannerToFirestore(banner);
                    });
        } else {
            banner.setImageUrl(newImageUrl);
            banner.setCreatedAt(Timestamp.now());
            banner.setUpdatedAt(Timestamp.now());
            saveBannerToFirestore(banner);
        }
    }

    private void saveBannerToFirestore(BannerItem banner) {
        db.collection("banners").document(banner.getBannerId())
                .set(banner)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, isEditMode ? "Đã cập nhật banner" : "Đã thêm banner", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
