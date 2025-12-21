package com.example.homeserviceapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.helpers.CloudinaryHelper;
import com.example.homeserviceapp.models.Category;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private ProgressBar progressBar;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddCategory;
    private ImageView btnBack;

    private AdminCategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;

    // Dialog components needing global access for Image Picker result
    private ImageView ivDialogIcon;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadCategories();
        
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                   Uri uri = result.getData().getData();
                   if (uri != null && ivDialogIcon != null) {
                       selectedImageUri = uri;
                       Glide.with(this).load(uri).into(ivDialogIcon);
                   }
                }
            }
        );

        btnBack.setOnClickListener(v -> finish());
        fabAddCategory.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        progressBar = findViewById(R.id.progressBar);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        adapter = new AdminCategoryAdapter(this, categoryList, new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Category category) {
                showAddEditDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                confirmDelete(category);
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("categories")
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải danh mục: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        categoryList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                category.setCategoryId(doc.getId()); // Ensure ID is set from Document ID
                                categoryList.add(category);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddEditDialog(Category category) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_category); // Need to create this layout or build programmatically? 
        // Let's build layout programmatically to avoid creating another file if possible, or assume simple layout.
        // Actually, creating a layout file `dialog_add_category.xml` is cleaner. I will assume it exists or I should generic it.
        // Let's create it on fly or use a simple View inflater if I don't want to make a file.
        // But for robust code, I should make a layout file. 
        // Wait, I can't look back easily. I'll create `dialog_add_category.xml` in next step.
        // For now, I will define `dialog_add_category` references here assuming I will create it.

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        // Define Views in Dialog
        // I need to create the layout file next.
        // For now I will write the code assuming the IDs.
        
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        EditText etCategoryName = dialog.findViewById(R.id.etCategoryName);
        ivDialogIcon = dialog.findViewById(R.id.ivDialogIcon);
        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        
        selectedImageUri = null; // Reset
        
        boolean isEdit = (category != null);
        
        if (isEdit) {
            tvDialogTitle.setText("Cập nhật Danh mục");
            etCategoryName.setText(category.getName());
            String iconUrl = category.getIconUrl();
            if (iconUrl == null || iconUrl.isEmpty()) iconUrl = category.getImageUrl();
            
            if (iconUrl != null && !iconUrl.isEmpty()) {
                Glide.with(this).load(iconUrl).into(ivDialogIcon);
            }
        } else {
            tvDialogTitle.setText("Thêm Danh mục mới");
        }
        
        ivDialogIcon.setOnClickListener(v -> {
             Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
             intent.addCategory(Intent.CATEGORY_OPENABLE);
             intent.setType("image/*");
             pickImageLauncher.launch(intent);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etCategoryName.setError("Nhập tên danh mục");
                return;
            }
            
            saveCategory(category, name, dialog);
        });

        dialog.show();
    }
    
    private void saveCategory(Category category, String name, Dialog dialog) {
        boolean isEdit = (category != null);
        String catId = isEdit ? category.getCategoryId() : "CAT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Disable button?
        dialog.findViewById(R.id.btnSave).setEnabled(false);
        Toast.makeText(this, "Đang lưu...", Toast.LENGTH_SHORT).show();
        
        if (selectedImageUri != null) {
            // Upload Image first
            CloudinaryHelper.uploadServiceImage(this, selectedImageUri, "icon_" + catId, new CloudinaryHelper.OnUploadListener() {
                @Override
                public void onProgress(int progress) {}

                @Override
                public void onSuccess(String imageUrl) {
                    performFirestoreSave(catId, name, imageUrl, isEdit, dialog);
                }

                @Override
                public void onError(String error) {
                     Toast.makeText(ManageCategoriesActivity.this, "Lỗi ảnh: " + error, Toast.LENGTH_SHORT).show();
                     dialog.findViewById(R.id.btnSave).setEnabled(true);
                }
            });
        } else {
             // Keep old image if edit, specific check
             String existImage = null;
             if (isEdit) {
                 existImage = category.getIconUrl();
                 if (existImage == null) existImage = category.getImageUrl();
             }
             performFirestoreSave(catId, name, existImage, isEdit, dialog);
        }
    }

    private void performFirestoreSave(String catId, String name, String iconUrl, boolean isEdit, Dialog dialog) {
         Map<String, Object> data = new HashMap<>();
         data.put("categoryId", catId);
         data.put("name", name);
         data.put("updatedAt", Timestamp.now());
         if (iconUrl != null) {
             data.put("iconUrl", iconUrl);
             data.put("imageUrl", iconUrl); // Sync both for compatibility
         }
         
         if (!isEdit) {
             data.put("createdAt", Timestamp.now());
             data.put("isActive", true);
             data.put("displayOrder", categoryList.size() + 1);
         }
         
         db.collection("categories").document(catId)
             .set(data, com.google.firebase.firestore.SetOptions.merge())
             .addOnSuccessListener(v -> {
                 Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
                 dialog.dismiss();
             })
             .addOnFailureListener(e -> {
                 Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                 dialog.findViewById(R.id.btnSave).setEnabled(true);
             });
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa '" + category.getName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteCategory(category.getCategoryId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteCategory(String catId) {
        db.collection("categories").document(catId).delete()
            .addOnSuccessListener(v -> Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
