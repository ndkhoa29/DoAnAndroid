package com.example.homeserviceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;

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
    }
}