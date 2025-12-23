package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.homeserviceapp.models.BannerItem;
import com.example.homeserviceapp.models.Category;
import com.example.homeserviceapp.models.ServiceItem;
import com.example.homeserviceapp.helpers.GridSpacingItemDecoration;
import com.example.homeserviceapp.helpers.KeyboardUtils;
import com.example.homeserviceapp.helpers.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ===== UI ELEMENTS =====
    private ViewPager2 viewPagerBanner;
    private View dot1, dot2, dot3;
    private TextView tvCustomerName, tvNotificationBadge, tvAllCategory, tvAllRated, tvAllPopular;
    private ImageView ivNotification, icFilter;
    private EditText etSearch;
    private ScrollView homeContentScrollView;

    // ===== ADAPTERS & LISTS =====
    private BannerAdapter bannerAdapter;
    private List<BannerItem> bannerList = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();

    private ServiceAdapter ratedServiceAdapter, popularServiceAdapter, searchResultAdapter;
    private List<ServiceItem> ratedServiceList = new ArrayList<>();
    private List<ServiceItem> popularServiceList = new ArrayList<>();
    private List<ServiceItem> searchResultList = new ArrayList<>();
    private List<ServiceItem> allServicesBackup = new ArrayList<>();

    private RecyclerView rvCategories, rvRatedServices, rvPopularServices, rvSearchResults;

    // ===== LOGIC VARIABLES =====
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private FirebaseFirestore db;
    private float filterMinPrice = 0;
    private float filterMaxPrice = Float.MAX_VALUE;
    private List<String> filterCategoryIds = new ArrayList<>();
    private String currentSearchQuery = "";
    private androidx.activity.result.ActivityResultLauncher<Intent> filterLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setupFilterLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupRecyclerViews();
        setupBanner();
        setupSearchLogic();

        loadAllData();
        return view;
    }

    private void initViews(View view) {
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        ivNotification = view.findViewById(R.id.ivNotification);
        tvAllCategory = view.findViewById(R.id.tvAllCategory);
        tvAllRated = view.findViewById(R.id.tvAllRated);
        tvAllPopular = view.findViewById(R.id.tvAllPopular);
        icFilter = view.findViewById(R.id.icFilter);
        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        homeContentScrollView = view.findViewById(R.id.homeContentScrollView);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);

        ivNotification.setOnClickListener(v -> startActivity(new Intent(requireContext(), ThongBaoActivity.class)));
        tvAllCategory.setOnClickListener(v -> startActivity(new Intent(requireContext(), CategoryActivity.class)));
        icFilter.setOnClickListener(v -> filterLauncher.launch(new Intent(requireContext(), FilterActivity.class)));

        View.OnClickListener viewAllServices = v -> {
            Intent intent = new Intent(requireContext(), TabServiceActivity.class);
            startActivity(intent);
        };
        tvAllRated.setOnClickListener(viewAllServices);
        tvAllPopular.setOnClickListener(viewAllServices);
    }

    private void setupRecyclerViews() {
        // Categories
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            Intent intent = new Intent(requireContext(), CategoryActivity.class);
            intent.putExtra("CATEGORY_NAME", category.getName());
            startActivity(intent);
        });
        rvCategories = getView().findViewById(R.id.rvCategories); // Sẽ gán lại sau khi inflate
        // Fix: Use the 'view' from initViews
    }

    // Tách nhỏ các hàm để tránh lỗi lồng nhau
    private void setupBanner() {
        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void setupSearchLogic() {
        searchResultAdapter = new ServiceAdapter(getContext(), searchResultList);
        searchResultAdapter.setGridMode(true);
        rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSearchResults.setAdapter(searchResultAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterServices(currentSearchQuery);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterServices(String query) {
        boolean hasQuery = !query.isEmpty();
        boolean hasFilter = (filterMinPrice > 0 || filterMaxPrice < Float.MAX_VALUE || !filterCategoryIds.isEmpty());

        if (!hasQuery && !hasFilter) {
            rvSearchResults.setVisibility(View.GONE);
            homeContentScrollView.setVisibility(View.VISIBLE);
            return;
        }

        rvSearchResults.setVisibility(View.VISIBLE);
        homeContentScrollView.setVisibility(View.GONE);

        List<ServiceItem> filteredList = new ArrayList<>();
        for (ServiceItem item : allServicesBackup) {
            boolean matchesQuery = !hasQuery || item.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchesPrice = item.getPrice() >= filterMinPrice && item.getPrice() <= filterMaxPrice;
            boolean matchesCategory = filterCategoryIds.isEmpty() || filterCategoryIds.contains(item.getCategoryId());

            if (matchesQuery && matchesPrice && matchesCategory) filteredList.add(item);
        }
        searchResultList.clear();
        searchResultList.addAll(filteredList);
        searchResultAdapter.notifyDataSetChanged();
    }

    private void updateDots(int position) {
        if (!isAdded()) return;
        dot1.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 0 ? R.color.blue : R.color.gray));
        dot2.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 1 ? R.color.blue : R.color.gray));
        dot3.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 2 ? R.color.blue : R.color.gray));
    }

    private Runnable sliderRunnable = () -> {
        if (bannerList.isEmpty()) return;
        int nextItem = (viewPagerBanner.getCurrentItem() + 1) % bannerList.size();
        viewPagerBanner.setCurrentItem(nextItem, true);
    };

    private void setupFilterLauncher() {
        filterLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                filterMinPrice = result.getData().getFloatExtra("MIN_PRICE", 0);
                filterMaxPrice = result.getData().getFloatExtra("MAX_PRICE", Float.MAX_VALUE);
                filterServices(currentSearchQuery);
            }
        });
    }

    private void loadAllData() {
        loadUserData();
        loadBanners();
        loadCategories();
        loadRatedServices();
        loadPopularServices();
        loadAllServicesForSearch();
    }

    // Các hàm load data từ Firestore (giữ nguyên logic của bạn nhưng dọn dẹp biến thừa)
    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString("fullName");
                tvCustomerName.setText("Xin chào, " + (name != null ? name : "Khách hàng"));
            }
        });
    }

    private void loadBanners() {
        db.collection("banners").orderBy("displayOrder").get().addOnSuccessListener(snapshots -> {
            bannerList.clear();
            for (DocumentSnapshot doc : snapshots) {
                BannerItem item = doc.toObject(BannerItem.class);
                if (item != null) bannerList.add(item);
            }
            bannerAdapter.notifyDataSetChanged();
        });
    }

    private void loadCategories() {
        db.collection("categories").limit(6).get().addOnSuccessListener(snapshots -> {
            categoryList.clear();
            for (DocumentSnapshot doc : snapshots) {
                Category cat = doc.toObject(Category.class);
                if (cat != null) categoryList.add(cat);
            }
            categoryAdapter.notifyDataSetChanged();
        });
    }

    private void loadRatedServices() {
        db.collection("services").orderBy("rating", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(snapshots -> {
            ratedServiceList.clear();
            for (DocumentSnapshot doc : snapshots) {
                ServiceItem item = doc.toObject(ServiceItem.class);
                if (item != null) ratedServiceList.add(item);
            }
            ratedServiceAdapter.notifyDataSetChanged();
        });
    }

    private void loadPopularServices() {
        db.collection("services").orderBy("bookingCount", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(snapshots -> {
            popularServiceList.clear();
            for (DocumentSnapshot doc : snapshots) {
                ServiceItem item = doc.toObject(ServiceItem.class);
                if (item != null) popularServiceList.add(item);
            }
            popularServiceAdapter.notifyDataSetChanged();
        });
    }

    private void loadAllServicesForSearch() {
        db.collection("services").whereEqualTo("isActive", true).get().addOnSuccessListener(snapshots -> {
            allServicesBackup.clear();
            for (DocumentSnapshot doc : snapshots) {
                ServiceItem item = doc.toObject(ServiceItem.class);
                if (item != null) {
                    item.setServiceId(doc.getId());
                    allServicesBackup.add(item);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}