package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;

    private RecyclerView rvAllCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btn_back);

        recyclerView = findViewById(R.id.recycler_categories);

        // 2. Khởi tạo Firebase & List
        db = FirebaseFirestore.getInstance();
        categoryList = new ArrayList<>();

        // 3. Thiết lập RecyclerView (Grid 4 cột như mẫu của bạn)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new CategoryAdapter(categoryList, this);
        recyclerView.setAdapter(adapter);

        // 4. Sự kiện nút Back
        btnBack.setOnClickListener(v -> finish());

        // 5. Lấy dữ liệu
        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        categoryList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Category category = doc.toObject(Category.class);
                            categoryList.add(category);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        btnBack.setOnClickListener(v -> finish());
        
        rvAllCategories = findViewById(R.id.rvAllCategories);
        setupRecyclerView();
        loadAllCategories();
    }
    
    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList, category -> {
            // Khi click vào 1 category, có thể mở TabServiceActivity với filter category đó
            // Hoặc hiển thị danh sách service của category đó
            // Tạm thời hiển thị Toast hoặc navigate đến ServiceList nếu cần
            Intent intent = new Intent(CategoryActivity.this, TabServiceActivity.class);
            intent.putExtra("CATEGORY_ID", category.getCategoryId());
            intent.putExtra("CATEGORY_NAME", category.getName());
            // TabServiceActivity cần handle extra này để switch tab hoặc filter
            startActivity(intent);
        });
        
        // Show 4 columns grid
        rvAllCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvAllCategories.setAdapter(categoryAdapter);
    }
    
    private void loadAllCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("categories")
            .orderBy("displayOrder")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                categoryList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    Category category = doc.toObject(Category.class);
                    if (category != null) {
                        category.setCategoryId(doc.getId());
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

    }
}