package com.example.homeserviceapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.homeserviceapp.models.ServiceItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> serviceList;
    private List<ServiceItem> originalServiceList;
    private String categoryName;

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

        serviceList = new ArrayList<>();

        loadServiceData(categoryName);
        
        originalServiceList = new ArrayList<>(serviceList);

        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);
    }

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

        String[] selectedCats = selectedCategories.isEmpty() ? new String[0] : selectedCategories.split(", ");

        List<ServiceItem> filteredList = new ArrayList<>();

        for (ServiceItem item : originalServiceList) {
            boolean matchesPrice = item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
            boolean matchesCategory = false;

            if ("Tất cả".equals(categoryName) && selectedCats.length > 0) {
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

    
    public void sortData(String sortType) {
        if (serviceList == null || serviceAdapter == null) {
            return;
        }

        switch (sortType) {
            case "PRICE_ASC":
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item1.getPrice(), item2.getPrice()));
                break;
            case "PRICE_DESC":
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item2.getPrice(), item1.getPrice()));
                break;
            case "RATING_DESC":
                Collections.sort(serviceList, (item1, item2) -> Double.compare(item2.getRating(), item1.getRating()));
                break;
            case "REVIEWS_DESC":
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item2.getReviewCount(), item1.getReviewCount()));
                break;
        }

        serviceAdapter.notifyDataSetChanged();
    }
}