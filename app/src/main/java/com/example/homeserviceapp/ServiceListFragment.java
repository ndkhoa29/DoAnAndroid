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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> serviceList;
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
        loadServiceData(categoryName); // Tải dữ liệu

        serviceAdapter = new ServiceAdapter(getContext(), serviceList);
        recyclerView.setAdapter(serviceAdapter);
    }

    private void loadServiceData(String category) {
        serviceList.clear();

        // 1. TRƯỜNG HỢP "TẤT CẢ"
        if ("Tất cả".equals(category)) {
            // Tải dữ liệu Cleaning
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office));
            serviceList.add(new ServiceItem("Dọn vệ sinh", 60, 4.4f, 80, R.drawable.cleaning));
            serviceList.add(new ServiceItem("Vệ sinh điều hòa", 50, 4.2f, 120, R.drawable.air));
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office));
            serviceList.add(new ServiceItem("Dọn vệ sinh", 60, 4.4f, 80, R.drawable.cleaning));
            serviceList.add(new ServiceItem("Vệ sinh điều hòa", 50, 4.2f, 120, R.drawable.air));

            // Tải dữ liệu Repairing
            serviceList.add(new ServiceItem("Sửa chữa điện - nước", 45, 4.3f, 200, R.drawable.repair));
            serviceList.add(new ServiceItem("Sửa dồ điện dân dụng", 80, 4.6f, 95, R.drawable.repair_electric));

            // Tải dữ liệu Laundry
            serviceList.add(new ServiceItem("Giặt ủi ", 55, 4.8f, 180, R.drawable.laundry));

            // Tải dữ liệu Painting
            serviceList.add(new ServiceItem("Sơn sửa nhà cửa", 70, 4.1f, 70, R.drawable.painting));
        }
        // 2. TRƯỜNG HỢP CÁC TAB CỤ THỂ
        else if ("Dọn vệ sinh".equals(category)) {
            serviceList.add(new ServiceItem("Vệ sinh văn phòng", 60, 4.7f, 150, R.drawable.clean_office));
            serviceList.add(new ServiceItem("Dọn vệ sinh", 60, 4.4f, 80, R.drawable.cleaning));
            serviceList.add(new ServiceItem("Vệ sinh điều hòa", 50, 4.2f, 120, R.drawable.air));
        } else if ("Sửa chữa".equals(category)) {
            serviceList.add(new ServiceItem("Sửa chữa điện - nước", 45, 4.3f, 200, R.drawable.repair));
            serviceList.add(new ServiceItem("Sửa dồ điện dân dụng", 80, 4.6f, 95, R.drawable.repair_electric));
        } else if ("Giặt ủi".equals(category)) {
            serviceList.add(new ServiceItem("Giặt ủi ", 55, 4.8f, 180, R.drawable.laundry));
        } else if ("Sơn sửa".equals(category)) {
            serviceList.add(new ServiceItem("Sơn sửa nhà cửa", 70, 4.1f, 70, R.drawable.painting));
        } else {
            // Dữ liệu mặc định cho các tab còn lại (Electric, Air...)
            serviceList.add(new ServiceItem("Default Service 1", 55, 4.0f, 50, R.drawable.placeholder_service));
        }
    }

    /**
     * Phương thức public để Activity gọi và sắp xếp dữ liệu
     * @param sortType Loại sắp xếp
     */
    public void sortData(String sortType) {
        if (serviceList == null || serviceAdapter == null) {
            return;
        }

        switch (sortType) {
            case "PRICE_ASC": // Giá tăng dần
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item1.getPrice(), item2.getPrice()));
                break;
            case "PRICE_DESC": // Giá giảm dần
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item2.getPrice(), item1.getPrice()));
                break;
            case "RATING_DESC": // Đánh giá cao nhất (giảm dần)
                Collections.sort(serviceList, (item1, item2) -> Float.compare(item2.getRating(), item1.getRating()));
                break;
            case "REVIEWS_DESC": // Nhiều đánh giá nhất (giảm dần)
                Collections.sort(serviceList, (item1, item2) -> Integer.compare(item2.getReviewCount(), item1.getReviewCount()));
                break;
        }

        // Thông báo cho Adapter biết dữ liệu đã thay đổi
        serviceAdapter.notifyDataSetChanged();
    }
}