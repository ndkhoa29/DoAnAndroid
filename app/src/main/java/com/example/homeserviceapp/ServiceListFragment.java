package com.example.homeserviceapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private static final String TAG = "ServiceListFragment";

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<ServiceItem> originalServiceList = new ArrayList<>();
    private ProgressBar progressBar;

    private String categoryName;
    private String categoryId;
    private FirebaseFirestore db;

    public ServiceListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString("CATEGORY_NAME");
        } else {
            categoryName = "Tất cả";
        }
        categoryId = mapCategoryNameToId(categoryName);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_services_fragment);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        progressBar = view.findViewById(R.id.progressBar);

        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);

        loadServicesFromFirebase();
    }

    private String mapCategoryNameToId(String name) {
        if (name == null) return null;
        switch (name) {
            case "Dọn vệ sinh": return "cat_cleaning";
            case "Sửa chữa": return "cat_repair";
            case "Giặt ủi": return "cat_laundry";
            case "Sơn sửa": return "cat_painting";
            case "Đồ điện tử": return "cat_electronics";
            case "điều hòa": return "cat_air_conditioning";
            default: return null;
        }
    }

    private void loadServicesFromFirebase() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        com.google.firebase.firestore.Query query = db.collection("services");

        // Bạn có thể thêm .whereEqualTo("active", true) nếu trong DB có field này
        if (categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            serviceList.clear();
            originalServiceList.clear();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ServiceItem service = document.toObject(ServiceItem.class);
                service.setServiceId(document.getId());
                serviceList.add(service);
                originalServiceList.add(service);
            }

            sortData("RATING_DESC");
            if (serviceList.isEmpty()) {
                Toast.makeText(getContext(), "Không có dịch vụ nào", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error loading services", e);
            loadHardcodedDataForTesting();
        });
    }

    public void applyFilter(float minPrice, float maxPrice, String selectedCategories) {
        if (originalServiceList == null || originalServiceList.isEmpty()) return;

        List<ServiceItem> filteredList = new ArrayList<>();
        String[] selectedCats = selectedCategories.isEmpty() ? new String[0] : selectedCategories.split(", ");

        for (ServiceItem item : originalServiceList) {
            boolean matchesPrice = item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
            boolean matchesCategory = true;

            // Logic lọc theo Category IDs nếu có chọn Chip lọc
            if (selectedCats.length > 0) {
                matchesCategory = false;
                for (String catName : selectedCats) {
                    String id = mapCategoryNameToId(catName.trim());
                    if (id != null && id.equals(item.getCategoryId())) {
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
                Collections.sort(serviceList, (a, b) -> Integer.compare(a.getPrice(), b.getPrice()));
                break;
            case "PRICE_DESC":
                Collections.sort(serviceList, (a, b) -> Integer.compare(b.getPrice(), a.getPrice()));
                break;
            case "RATING_DESC":
                Collections.sort(serviceList, (a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "REVIEWS_DESC":
                Collections.sort(serviceList, (a, b) -> Integer.compare(b.getReviewCount(), a.getReviewCount()));
                break;
        }
        serviceAdapter.notifyDataSetChanged();
    }

    private void loadHardcodedDataForTesting() {
        serviceList.clear();
        // Thêm dữ liệu giả ở đây nếu cần...
        serviceAdapter.notifyDataSetChanged();
    }
}