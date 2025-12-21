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
    private List<ServiceItem> serviceList;
    private List<ServiceItem> originalServiceList;
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
            categoryName = "Unknown";
        }

        // Map category name to categoryId in Firebase
        categoryId = mapCategoryNameToId(categoryName);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_services_fragment);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // ProgressBar (nếu có trong layout)
        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        serviceList = new ArrayList<>();
        originalServiceList = new ArrayList<>();
        serviceList = new ArrayList<>();

        loadServiceData(categoryName);
        
        originalServiceList = new ArrayList<>(serviceList);

        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);

        // Load services from Firebase
        loadServicesFromFirebase();
    }

    /**
     * Map category name to Firebase categoryId
     */
    private String mapCategoryNameToId(String categoryName) {
        switch (categoryName) {
            case "Dọn vệ sinh":
                return "cat_cleaning";
            case "Sửa chữa":
                return "cat_repair";
            case "Giặt ủi":
                return "cat_laundry";
            case "Sơn sửa":
                return "cat_painting";
            case "Đồ điện tử":
                return "cat_electronics";
            case "điều hòa":
                return "cat_air_conditioning";
            case "Tất cả":
            default:
                return null; // Load all services
        }
    }

    /**
     * Load services from Firebase Firestore
     */
    private void loadServicesFromFirebase() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Build query
        com.google.firebase.firestore.Query query = db.collection("services")
                .whereEqualTo("active", true); // Only active services

        // Filter by category if not "Tất cả"
        if (categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId);
        }

        // Execute query
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    serviceList.clear();
                    originalServiceList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ServiceItem service = document.toObject(ServiceItem.class);
                        service.setServiceId(document.getId());
                        serviceList.add(service);
                        originalServiceList.add(service);
                    }

                    Log.d(TAG, "Loaded " + serviceList.size() + " services for category: " + categoryName);

                    // Sort by rating by default
                    sortData("RATING_DESC");

                    serviceAdapter.notifyDataSetChanged();

                    if (serviceList.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Chưa có dịch vụ trong danh mục này",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    Log.e(TAG, "Error loading services", e);
                    Toast.makeText(getContext(),
                            "Lỗi tải dịch vụ: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Load fallback hardcoded data for testing
                    loadHardcodedDataForTesting();
                });
    }

    /**
     * Fallback: Load hardcoded data if Firebase fails (for testing only)
     */
    private void loadHardcodedDataForTesting() {
        Log.d(TAG, "Loading hardcoded data as fallback");

        serviceList.clear();
        originalServiceList.clear();

        // Create some test services
        ServiceItem testService1 = new ServiceItem();
        testService1.setServiceId("test_1");
        testService1.setTitle("Vệ sinh văn phòng (Test)");
        testService1.setPrice(60000);
        testService1.setPriceUnit("/giờ");
        testService1.setRating(4.7);
        testService1.setReviewCount(150);

        ServiceItem testService2 = new ServiceItem();
        testService2.setServiceId("test_2");
        testService2.setTitle("Dọn vệ sinh (Test)");
        testService2.setPrice(50000);
        testService2.setPriceUnit("/lần");
        testService2.setRating(4.4);
        testService2.setReviewCount(80);

        serviceList.add(testService1);
        serviceList.add(testService2);
        originalServiceList.addAll(serviceList);

        serviceAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(),
                "Đang dùng dữ liệu test (Firebase chưa kết nối)",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Apply filter for price and categories
     */
    private void loadServiceData(String category) {
        serviceList.clear();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();



        com.google.firebase.firestore.Query query = db.collection("services");
        
        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                serviceList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ServiceItem service = doc.toObject(ServiceItem.class);
                    if (service != null) {
                        service.setServiceId(doc.getId());
                        serviceList.add(service);
                    }
                }

                originalServiceList = new ArrayList<>(serviceList);
                serviceAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi tải dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    public void applyFilter(float minPrice, float maxPrice, String selectedCategories) {
        if (originalServiceList == null || serviceAdapter == null) {
            return;
        }

        String[] selectedCats = selectedCategories.isEmpty() ? new String[0]
                : selectedCategories.split(", ");

        List<ServiceItem> filteredList = new ArrayList<>();

        for (ServiceItem item : originalServiceList) {
            boolean matchesPrice = item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
            boolean matchesCategory = false;

            if ("Tất cả".equals(categoryName) && selectedCats.length > 0) {
                // If in "Tất cả" tab AND chips are selected
                for (String cat : selectedCats) {
                    if (item.getCategoryId().equals(mapCategoryNameToId(cat.trim()))) {
                        matchesCategory = true;
                        break;
                    }
                }
            } else {
                // No chips selected or specific tab -> skip category filter
                for (String cat : selectedCats) {
                    matchesCategory = true;
                }
            } else {
                matchesCategory = true;
            }

            if (matchesPrice && matchesCategory) {
                filteredList.add(item);
            }
        }

        serviceList.clear();
        serviceList.addAll(filteredList);

        sortData("RATING_DESC");

        serviceAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(),
                "Đã áp dụng lọc: " + filteredList.size() + " dịch vụ",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Sort data by different criteria
     */
    public void sortData(String sortType) {
        if (serviceList == null || serviceAdapter == null) {
            return;
        }

        switch (sortType) {
            case "PRICE_ASC":
                Collections.sort(serviceList, (item1, item2) ->
                        Integer.compare(item1.getPrice(), item2.getPrice()));
                break;
            case "PRICE_DESC":
                Collections.sort(serviceList, (item1, item2) ->
                        Integer.compare(item2.getPrice(), item1.getPrice()));
                break;
            case "RATING_DESC":
                Collections.sort(serviceList, (item1, item2) ->
                        Double.compare(item2.getRating(), item1.getRating()));
                Collections.sort(serviceList, (item1, item2) -> Double.compare(item2.getRating(), item1.getRating()));
                break;
            case "REVIEWS_DESC":
                Collections.sort(serviceList, (item1, item2) ->
                        Integer.compare(item2.getReviewCount(), item1.getReviewCount()));
                break;
        }

        serviceAdapter.notifyDataSetChanged();
    }
}