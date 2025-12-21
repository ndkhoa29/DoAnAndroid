package com.example.homeserviceapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Category;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminServiceDetailActivity extends AppCompatActivity {

    private ImageView ivServiceImage, btnBack;
    private TextView tvCategoryName, tvServiceName, tvServicePrice, tvDescription;
    private Button btnDelete, btnEdit;
    
    private FirebaseFirestore db;
    private String serviceId;
    private ServiceItem currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_service_detail);
        
        db = FirebaseFirestore.getInstance();
        serviceId = getIntent().getStringExtra("SERVICE_ID");

        initViews();
        loadServiceData();
        
        btnBack.setOnClickListener(v -> finish());
        
        btnDelete.setOnClickListener(v -> confirmDelete());
        
        btnEdit.setOnClickListener(v -> {
            if (currentService != null) {
                Intent intent = new Intent(this, AddServiceActivity.class);
                intent.putExtra("IS_EDIT_MODE", true);
                intent.putExtra("SERVICE_ID", serviceId);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        ivServiceImage = findViewById(R.id.ivServiceImage);
        btnBack = findViewById(R.id.btnBack);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
    }

    private void loadServiceData() {
        if (serviceId == null) {
            Toast.makeText(this, "Không tìm thấy ID dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("services").document(serviceId).addSnapshotListener((doc, e) -> {
             if (e != null) return;
             
             if (doc != null && doc.exists()) {
                 currentService = doc.toObject(ServiceItem.class);
                 if (currentService != null) {
                     displayData(currentService);
                 }
             } else {
                 Toast.makeText(this, "Dịch vụ đã bị xóa", Toast.LENGTH_SHORT).show();
                 finish();
             }
        });
    }

    private void displayData(ServiceItem service) {
        tvServiceName.setText(service.getTitle());
        tvServicePrice.setText(service.getPrice() + "đ");
        tvDescription.setText(service.getDescription());
        
        if (service.getCategoryId() != null) {
             db.collection("categories").document(service.getCategoryId()).get()
                 .addOnSuccessListener(doc -> {
                     if (doc.exists()) {
                         Category cat = doc.toObject(Category.class);
                         if (cat != null) tvCategoryName.setText(cat.getName());
                     }
                 });
        }
        
        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
            Glide.with(this).load(service.getImageUrls().get(0)).into(ivServiceImage);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Xóa Dịch vụ")
            .setMessage("Bạn chắc chắn muốn xóa dịch vụ này không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> deleteService())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteService() {
        if (serviceId != null) {
            db.collection("services").document(serviceId).delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
