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
import com.example.homeserviceapp.helpers.CloudinaryHelper;

public class ChinhSuaHoSoActivity extends AppCompatActivity {

    private de.hdodenhof.circleimageview.CircleImageView imgProfile;
    private EditText etFirstName, etEmail, etPhoneNumber;
    private ImageButton btnEditPhoto;
    private Uri selectedImageUri;
    private String currentAvatarUrl;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_ho_so);

        imgProfile = findViewById(R.id.imgProfile);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnSave = findViewById(R.id.btnSave);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu...");
        progressDialog.setCancelable(false);

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgProfile.setImageURI(selectedImageUri);
                }
            }
        );

        btnBack.setOnClickListener(v -> finish());

        btnEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProfile());

        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        currentAvatarUrl = documentSnapshot.getString("avatarUrl");

                        if (fullName != null) {
                            etFirstName.setText(fullName);
                        }

                        if (email != null) {
                            etEmail.setText(email);
                        }

                        if (phone != null) {
                            etPhoneNumber.setText(phone);
                        }

                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            String optimizedUrl = currentAvatarUrl;
                            if (currentAvatarUrl.contains("cloudinary.com")) {
                                optimizedUrl = CloudinaryHelper.getThumbnailUrl(currentAvatarUrl);
                            }
                            
                            com.bumptech.glide.Glide.with(this)
                                .load(optimizedUrl)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                .skipMemoryCache(false)
                                .placeholder(R.drawable.ic_provider_avatar)
                                .error(R.drawable.ic_provider_avatar)
                                .into(imgProfile);
                        }
                    }
                });
        }
    }

    private void saveProfile() {
        String fullName = etFirstName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            if (selectedImageUri != null) {
                CloudinaryHelper.uploadUserAvatar(this, selectedImageUri, userId, new CloudinaryHelper.OnUploadListener() {
                    @Override
                    public void onProgress(int progress) {
                        progressDialog.setMessage("Đang upload ảnh... " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String imageUrl) {
                        saveUserDataToFirestore(userId, fullName, email, phoneNumber, imageUrl);
                    }

                    @Override
                    public void onError(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(ChinhSuaHoSoActivity.this, "Lỗi khi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                saveUserDataToFirestore(userId, fullName, email, phoneNumber, currentAvatarUrl);
            }
        }
    }

    private void saveUserDataToFirestore(String userId, String fullName, String email, String phoneNumber, String avatarUrl) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phoneNumber", phoneNumber);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
