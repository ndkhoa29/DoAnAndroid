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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> serviceList;
    private List<ServiceItem> originalServiceList; // Danh sách dịch vụ gốc
    private String categoryName;

    public ServiceListFragment() {
        // Required empty public constructor
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
        loadServiceData(categoryName); // Tải dữ liệu vào serviceList

        // Lưu bản sao danh sách gốc ngay sau khi tải dữ liệu
        originalServiceList = new ArrayList<>(serviceList);

        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);
    }

    /**
     * TẢI DỮ LIỆU GỐC (Đã thêm Category Type)
     */
    private void loadServiceData(String category) {
        serviceList.clear();

        // Định nghĩa các loại danh mục
        String CAT_CLEANING = "Dọn dẹp";
        String CAT_REPAIRING = "Sửa chữa";
        String CAT_LAUNDRY = "Giặt là";
        String CAT_PAINTING = "Sơn";

        if ("Tất cả".equals(category)) {
            // Tải dữ liệu Cleaning
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office, CAT_CLEANING));
            serviceList.add(new ServiceItem("Dọn vệ sinh", 60, 4.4f, 80, R.drawable.cleaning, CAT_CLEANING));
            serviceList.add(new ServiceItem("Vệ sinh điều hòa", 50, 4.2f, 120, R.drawable.air, CAT_CLEANING));
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office, CAT_CLEANING));

            // Tải dữ liệu Repairing
            serviceList.add(new ServiceItem("Sửa chữa điện - nước", 45, 4.3f, 200, R.drawable.repair, CAT_REPAIRING));
            serviceList.add(new ServiceItem("Sửa dồ điện dân dụng", 80, 4.6f, 95, R.drawable.repair_electric, CAT_REPAIRING));

            // Tải dữ liệu Laundry
            serviceList.add(new ServiceItem("Giặt ủi ", 55, 4.8f, 180, R.drawable.laundry, CAT_LAUNDRY));

            // Tải dữ liệu Painting
            serviceList.add(new ServiceItem("Sơn sửa nhà cửa", 70, 4.1f, 70, R.drawable.painting, CAT_PAINTING));
        }
        // 2. TRƯỜNG HỢP CÁC TAB CỤ THỂ
        else if ("Dọn vệ sinh".equals(category)) {
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office, CAT_CLEANING));
            serviceList.add(new ServiceItem("Dọn vệ sinh", 60, 4.4f, 80, R.drawable.cleaning, CAT_CLEANING));
            serviceList.add(new ServiceItem("Vệ sinh điều hòa", 50, 4.2f, 120, R.drawable.air, CAT_CLEANING));
        } else if ("Sửa chữa".equals(category)) {
            serviceList.add(new ServiceItem("Sửa chữa điện - nước", 45, 4.3f, 200, R.drawable.repair, CAT_REPAIRING));
            serviceList.add(new ServiceItem("Sửa dồ điện dân dụng", 80, 4.6f, 95, R.drawable.repair_electric, CAT_REPAIRING));
        } else if ("Giặt ủi".equals(category)) {
            serviceList.add(new ServiceItem("Giặt ủi ", 55, 4.8f, 180, R.drawable.laundry, CAT_LAUNDRY));
        } else if ("Sơn sửa".equals(category)) {
            serviceList.add(new ServiceItem("Sơn sửa nhà cửa", 70, 4.1f, 70, R.drawable.painting, CAT_PAINTING));
        } else {
            serviceList.add(new ServiceItem("Default Service 1", 55, 4.0f, 50, R.drawable.placeholder_service, "Khác"));
        }
    }

    /**
     * PHƯƠNG THỨC LỌC MỚI: Áp dụng bộ lọc Giá và Danh mục
     */
    public void applyFilter(float minPrice, float maxPrice, String selectedCategories) {
        if (originalServiceList == null || serviceAdapter == null) {
            return;
        }

        // Tách chuỗi danh mục đã chọn thành một mảng (ví dụ: ["Dọn dẹp", "Giặt là"])
        String[] selectedCats = selectedCategories.isEmpty() ? new String[0] : selectedCategories.split(", ");

        List<ServiceItem> filteredList = new ArrayList<>();

        // 1. Lọc dữ liệu từ danh sách GỐC
        for (ServiceItem item : originalServiceList) {
            boolean matchesPrice = item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
            boolean matchesCategory = false;

            // Logic lọc danh mục
            if ("Tất cả".equals(categoryName) && selectedCats.length > 0) {
                // Nếu đang ở tab "Tất cả" VÀ có Chip được chọn
                for (String cat : selectedCats) {
                    if (item.getCategoryType().equals(cat.trim())) { // Kiểm tra khớp chính xác
                        matchesCategory = true;
                        break;
                    }
                }
            } else {
                // Trường hợp 1: Không có Chip nào được chọn (Hoặc tab cụ thể) -> Bỏ qua lọc danh mục (luôn TRUE)
                matchesCategory = true;
            }

            if (matchesPrice && matchesCategory) {
                filteredList.add(item);
            }
        }

        // 2. Cập nhật serviceList và Adapter
        serviceList.clear();
        serviceList.addAll(filteredList);

        // 3. (Tùy chọn) Sắp xếp lại danh sách sau khi lọc
        sortData("RATING_DESC");

        serviceAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(),
                "Đã áp dụng lọc: " + filteredList.size() + " dịch vụ",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Phương thức public để Activity gọi và sắp xếp dữ liệu (Giữ nguyên)
     */
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
                Collections.sort(serviceList, (item1, item2) -> Float.compare(item2.getRating(), item1.getRating()));
                break;
            case "REVIEWS_DESC":
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item2.getReviewCount(), item1.getReviewCount()));
                break;
        }

        serviceAdapter.notifyDataSetChanged();
    }
}