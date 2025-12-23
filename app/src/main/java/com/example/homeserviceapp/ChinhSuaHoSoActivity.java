package com.example.homeserviceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.helpers.CloudinaryHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChinhSuaHoSoActivity extends AppCompatActivity {

    private CircleImageView imgProfile;
    private EditText etFirstName, etEmail, etPhoneNumber;
    private ImageButton btnEditPhoto;
    private Button btnSave;

    private Uri selectedImageUri;
    private String currentAvatarUrl;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_ho_so);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // View
        imgProfile = findViewById(R.id.imgProfile);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        btnSave = findViewById(R.id.btnSave);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Progress
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu...");
        progressDialog.setCancelable(false);

        // Chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgProfile.setImageURI(selectedImageUri);
                    }
                }
        );

        btnEditPhoto.setOnClickListener(v -> openGallery());
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        loadCurrentUserData();
    }

    // ================= LOAD DATA =================
    private void loadCurrentUserData() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etFirstName.setText(doc.getString("fullName"));
                    etEmail.setText(doc.getString("email"));
                    etPhoneNumber.setText(doc.getString("phoneNumber"));
                    currentAvatarUrl = doc.getString("avatarUrl");

                    if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                        String imageUrl = currentAvatarUrl;

                        if (imageUrl.contains("cloudinary.com")) {
                            imageUrl = CloudinaryHelper.getThumbnailUrl(imageUrl);
                        }

                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_provider_avatar)
                                .error(R.drawable.ic_provider_avatar)
                                .into(imgProfile);
                    }
                });
    }

    // ================= SAVE PROFILE =================
    private void saveProfile() {
        String fullName = etFirstName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();

        // Validation (theo yêu cầu đề)
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng không để trống thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;

        progressDialog.show();
        String userId = auth.getCurrentUser().getUid();

        // Có chọn ảnh mới
        if (selectedImageUri != null) {
            CloudinaryHelper.uploadUserAvatar(
                    this,
                    selectedImageUri,
                    userId,
                    new CloudinaryHelper.OnUploadListener() {
                        @Override
                        public void onProgress(int progress) {
                            progressDialog.setMessage("Đang upload ảnh... " + progress + "%");
                        }

                        @Override
                        public void onSuccess(String imageUrl) {
                            saveUserDataToFirestore(userId, fullName, email, phone, imageUrl);
                        }

                        @Override
                        public void onError(String error) {
                            progressDialog.dismiss();
                            Toast.makeText(
                                    ChinhSuaHoSoActivity.this,
                                    "Upload ảnh thất bại: " + error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        } else {
            saveUserDataToFirestore(userId, fullName, email, phone, currentAvatarUrl);
        }
    }

    // ================= UPDATE FIRESTORE =================
    private void saveUserDataToFirestore(
            String userId,
            String fullName,
            String email,
            String phone,
            String avatarUrl
    ) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phoneNumber", phone);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    // ================= GALLERY =================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}
