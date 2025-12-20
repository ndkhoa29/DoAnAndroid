package com.example.homeserviceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private ProgressBar progressBar;

    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<ServiceItem> originalServiceList = new ArrayList<>();

    private String categoryName;
    private FirebaseFirestore db;

    public ServiceListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            categoryName = getArguments().getString("CATEGORY_NAME");
        } else {
            categoryName = "Tất cả";
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_services_fragment);
        progressBar = view.findViewById(R.id.progress_bar_service);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);

        loadServiceDataFromFirestore(categoryName);
    }

    private void loadServiceDataFromFirestore(String category) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("services")
                .get()
                .addOnCompleteListener(task -> {
                    // Kiểm tra Fragment còn tồn tại không
                    if (!isAdded() || getContext() == null) return;

                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        serviceList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ServiceItem item = document.toObject(ServiceItem.class);

                            // SỬA LỖI: Kiểm tra null cho categoryType và so sánh không phân biệt hoa thường
                            String itemCat = item.getCategoryType();
                            if ("Tất cả".equalsIgnoreCase(category) ||
                                    (itemCat != null && itemCat.equalsIgnoreCase(category))) {
                                serviceList.add(item);
                            }
                        }

                        originalServiceList = new ArrayList<>(serviceList);
                        sortData("RATING_DESC");
                        serviceAdapter.notifyDataSetChanged();

                        // Thêm thông báo nếu danh sách trống
                        if (serviceList.isEmpty()) {
                            Log.d("DEBUG", "Không có dữ liệu cho danh mục: " + category);
                        }
                    } else {
                        Log.e("FIRESTORE_ERROR", "Lỗi tải dữ liệu", task.getException());
                        Toast.makeText(getContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void applyFilter(float minPrice, float maxPrice, String selectedCategories) {
        if (originalServiceList == null || originalServiceList.isEmpty()) return;

        String[] selectedCats = selectedCategories.isEmpty() ? new String[0] : selectedCategories.split(", ");
        List<ServiceItem> filteredList = new ArrayList<>();

        for (ServiceItem item : originalServiceList) {
            boolean matchesPrice = item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
            boolean matchesCategory = true;

            // Lọc category phụ khi ở Tab "Tất cả"
            if ("Tất cả".equalsIgnoreCase(categoryName) && selectedCats.length > 0) {
                matchesCategory = false;
                for (String cat : selectedCats) {
                    if (item.getCategoryType() != null &&
                            item.getCategoryType().equalsIgnoreCase(cat.trim())) {
                        matchesCategory = true;
                        break;
                    }
                }
            }

            if (matchesPrice && matchesCategory) {
                filteredList.add(item);
            }
        }

        serviceList.clear();
        serviceList.addAll(filteredList);
        serviceAdapter.notifyDataSetChanged();
    }

    public void sortData(String sortType) {
        if (serviceList == null || serviceList.isEmpty()) return;

        switch (sortType) {
            case "PRICE_ASC":
                Collections.sort(serviceList, (i1, i2) -> Integer.compare(i1.getPrice(), i2.getPrice()));
                break;
            case "PRICE_DESC":
                Collections.sort(serviceList, (i1, i2) -> Integer.compare(i2.getPrice(), i1.getPrice()));
                break;
            case "RATING_DESC":
                Collections.sort(serviceList, (i1, i2) -> Float.compare(i2.getRating(), i1.getRating()));
                break;
        }
        serviceAdapter.notifyDataSetChanged();
    }
}