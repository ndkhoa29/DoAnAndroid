package com.example.homeserviceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class ServiceListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private ProgressBar progressBar;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<ServiceItem> originalServiceList = new ArrayList<>();
    private String categoryName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) categoryName = getArguments().getString("CATEGORY_NAME");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        loadData();
    }

    private void loadData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("services").get().addOnCompleteListener(task -> {
            if (!isAdded()) return;
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            if (task.isSuccessful() && task.getResult() != null) {
                serviceList.clear();
                String currentTabId = mapTabNameToId(categoryName);

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    ServiceItem item = doc.toObject(ServiceItem.class);
                    String dbCatId = (item.getCategoryType() != null) ? item.getCategoryType().trim() : "";

                    // Lọc theo Tab hiện tại
                    if ("Tất cả".equalsIgnoreCase(categoryName) || dbCatId.equalsIgnoreCase(currentTabId)) {
                        serviceList.add(item);
                    }
                }
                // QUAN TRỌNG: Lưu danh sách gốc để lọc giá không bị mất dữ liệu
                originalServiceList = new ArrayList<>(serviceList);
                serviceAdapter.notifyDataSetChanged();
            }
        });
    }

    public void applyFilter(float min, float max, String selectedCats) {
        if (originalServiceList == null || originalServiceList.isEmpty()) return;

        // 1. Chuyển đổi tên Chip sang danh sách ID
        List<String> selectedIdList = new ArrayList<>();
        if (selectedCats != null && !selectedCats.isEmpty()) {
            for (String s : selectedCats.split(",")) {
                String id = mapTabNameToId(s.trim());
                if (!id.isEmpty()) selectedIdList.add(id.toLowerCase());
            }
        }

        List<ServiceItem> filtered = new ArrayList<>();
        for (ServiceItem item : originalServiceList) {
            // 2. Logic lọc giá chuẩn VNĐ (ví dụ: 80000 nằm trong 0 - 120000)
            boolean matchesPrice = item.getPrice() >= min && item.getPrice() <= max;

            // 3. Logic lọc danh mục: Mặc định là True để không chặn lọc giá
            boolean matchesCat = true;
            if (!selectedIdList.isEmpty()) {
                matchesCat = false;
                if (item.getCategoryType() != null) {
                    String itemCatId = item.getCategoryType().toLowerCase().trim();
                    if (selectedIdList.contains(itemCatId)) matchesCat = true;
                }
            }

            if (matchesPrice && matchesCat) filtered.add(item);
        }

        // 4. Cập nhật giao diện
        serviceList.clear();
        serviceList.addAll(filtered);
        serviceAdapter.notifyDataSetChanged();

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
        }
    }

    private String mapTabNameToId(String name) {
        if (name == null) return "";
        switch (name.trim()) {
            case "Dọn dẹp": return "cat_cleaning";
            case "Sửa chữa": return "cat_repair";
            case "Làm đẹp": return "cat_beauty";
            case "Sức khỏe": return "cat_healthcare";
            case "Vận chuyển": return "cat_moving";
            case "Gia sư": return "cat_tutoring";
            default: return "";
        }
    }
}