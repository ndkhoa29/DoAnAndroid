package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.homeserviceapp.models.Category;
import com.example.homeserviceapp.models.BannerItem;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private BannerAdapter bannerAdapter;
    private List<BannerItem> bannerList;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private TextView tvAllCategory, tvAllRated, tvAllPopular, tvUserName, tvNotificationBadge;
    private View dot1, dot2, dot3;
    private ImageView ivNotification, icFilter;
    
    private android.widget.EditText etSearch;
    private RecyclerView rvSearchResults;
    private android.widget.ScrollView homeContentScrollView;
    private ServiceAdapter searchResultAdapter;
    private List<ServiceItem> searchResultList;
    private List<ServiceItem> allServicesBackup; // Cache for local search

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    
    private RecyclerView rvRatedServices;
    private ServiceAdapter ratedServiceAdapter;
    private List<ServiceItem> ratedServiceList;

    private RecyclerView rvPopularServices;
    private ServiceAdapter popularServiceAdapter;
    private List<ServiceItem> popularServiceList;

    private androidx.activity.result.ActivityResultLauncher<Intent> filterLauncher;
    private float filterMinPrice = 0;
    private float filterMaxPrice = Float.MAX_VALUE;
    private List<String> filterCategoryIds = new ArrayList<>();
    private String currentSearchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filterLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    filterMinPrice = result.getData().getFloatExtra("MIN_PRICE", 0);
                    filterMaxPrice = result.getData().getFloatExtra("MAX_PRICE", Float.MAX_VALUE);
                    String catIdsStr = result.getData().getStringExtra("SELECTED_CATEGORY_IDS");
                    
                    filterCategoryIds.clear();
                    if (catIdsStr != null && !catIdsStr.isEmpty()) {
                        String[] ids = catIdsStr.split(",");
                        for (String id : ids) {
                            filterCategoryIds.add(id.trim());
                        }
                    }

                    filterServices(currentSearchQuery);
                    

                    if (currentSearchQuery.isEmpty() && (filterMinPrice > 0 || filterMaxPrice < Float.MAX_VALUE || !filterCategoryIds.isEmpty())) {
                         rvSearchResults.setVisibility(View.VISIBLE);
                         homeContentScrollView.setVisibility(View.GONE);
                    } else if (currentSearchQuery.isEmpty() && filterMinPrice == 0 && filterMaxPrice == Float.MAX_VALUE && filterCategoryIds.isEmpty()) {

                        rvSearchResults.setVisibility(View.GONE);
                        homeContentScrollView.setVisibility(View.VISIBLE);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);

        ivNotification = view.findViewById(R.id.ivNotification);
        tvAllCategory = view.findViewById(R.id.tvAllCategory);
        tvAllRated = view.findViewById(R.id.tvAllRated);
        tvAllPopular = view.findViewById(R.id.tvAllPopular);
        icFilter = view.findViewById(R.id.icFilter);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        homeContentScrollView = view.findViewById(R.id.homeContentScrollView);
        
        rvCategories = view.findViewById(R.id.rvCategories);
        rvRatedServices = view.findViewById(R.id.rvRatedServices);
        rvPopularServices = view.findViewById(R.id.rvPopularServices);

        setupCategoriesRecyclerView();
        setupServicesRecyclerViews();
        setupSearchRecyclerView();
        
        loadUserData();
        loadCategories();
        loadNotificationCount();
        loadRatedServices();
        loadPopularServices();
        loadAllServicesForSearch();

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterServices(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        rvSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    com.example.homeserviceapp.helpers.KeyboardUtils.hideKeyboard(requireActivity());
                }
            }
        });

        icFilter.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FilterActivity.class);
            filterLauncher.launch(intent);
        });

        ivNotification.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ThongBaoActivity.class);
            startActivity(intent);
        });

        tvAllCategory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryActivity.class);
            startActivity(intent);
        });

        tvAllRated.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ViewAllServicesActivity.class);
            intent.putExtra("TYPE", "RATED");
            startActivity(intent);
        });

        tvAllPopular.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ViewAllServicesActivity.class);
            intent.putExtra("TYPE", "POPULAR");
            startActivity(intent);
        });

        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);
        
        loadBanners();

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        sliderHandler.postDelayed(sliderRunnable, 3000);

        return view;
    }

    private Runnable sliderRunnable = () -> {
        int currentItem = viewPagerBanner.getCurrentItem();
        int nextItem = (currentItem + 1) % (bannerList != null && !bannerList.isEmpty() ? bannerList.size() : 1);
        viewPagerBanner.setCurrentItem(nextItem, true);
    };

    private void updateDots(int position) {
        if (getContext() == null) return;
        dot1.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 0 ? R.color.blue : R.color.gray));
        dot2.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 1 ? R.color.blue : R.color.gray));
        dot3.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), position == 2 ? R.color.blue : R.color.gray));
    }

    private void loadUserData() {
        if (getContext() == null) return;
        com.example.homeserviceapp.helpers.UserPreferences userPrefs = 
            new com.example.homeserviceapp.helpers.UserPreferences(requireContext());

        if (userPrefs.hasCachedData()) {
            String cachedName = userPrefs.getUserName();
            if (!cachedName.isEmpty()) {
                tvUserName.setText(cachedName);
            }
        }

        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String avatarUrl = documentSnapshot.getString("avatarUrl");

                        userPrefs.saveUserData(fullName, email, phone, avatarUrl);

                        if (fullName != null && !fullName.isEmpty()) {
                            tvUserName.setText(fullName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
        }
    }

    private void setupCategoriesRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            Intent intent = new Intent(requireContext(), CategoryActivity.class);
            intent.putExtra("CATEGORY_NAME", category.getName());
            startActivity(intent);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);
    }
    
    private void setupServicesRecyclerViews() {

        ratedServiceList = new ArrayList<>();
        ratedServiceAdapter = new ServiceAdapter(getContext(), ratedServiceList);
        LinearLayoutManager ratedLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvRatedServices.setLayoutManager(ratedLayoutManager);
        rvRatedServices.setAdapter(ratedServiceAdapter);

        popularServiceList = new ArrayList<>();
        popularServiceAdapter = new ServiceAdapter(getContext(), popularServiceList);
        LinearLayoutManager popularLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPopularServices.setLayoutManager(popularLayoutManager);
        rvPopularServices.setAdapter(popularServiceAdapter);
    }
    
    private void loadCategories() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        db.collection("categories")
            .orderBy("displayOrder")
            .limit(6)
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
            });
    }
    
    private void loadRatedServices() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("services")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                ratedServiceList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ServiceItem service = doc.toObject(ServiceItem.class);
                    if (service != null) {
                        service.setServiceId(doc.getId());
                        ratedServiceList.add(service);
                    }
                }
                ratedServiceAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                 db.collection("services")
                     .limit(10)
                     .get()
                     .addOnSuccessListener(retrySnapshots -> {
                         ratedServiceList.clear();
                         for (com.google.firebase.firestore.DocumentSnapshot doc : retrySnapshots) {
                             ServiceItem service = doc.toObject(ServiceItem.class);
                             if (service != null) {
                                 service.setServiceId(doc.getId());
                                 ratedServiceList.add(service);
                             }
                         }
                         // Sort manually
                         ratedServiceList.sort((s1, s2) -> Double.compare(s2.getRating(), s1.getRating()));
                         ratedServiceAdapter.notifyDataSetChanged();
                     });
            });
    }

    private void loadPopularServices() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // Load popular services (order by bookingCount)
        db.collection("services")
            .orderBy("bookingCount", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                popularServiceList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ServiceItem service = doc.toObject(ServiceItem.class);
                    if (service != null) {
                        service.setServiceId(doc.getId());
                        popularServiceList.add(service);
                    }
                }
                popularServiceAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                 // Try without ordering in case index is missing
                 db.collection("services")
                     .limit(10)
                     .get()
                     .addOnSuccessListener(retrySnapshots -> {
                         popularServiceList.clear();
                         for (com.google.firebase.firestore.DocumentSnapshot doc : retrySnapshots) {
                             ServiceItem service = doc.toObject(ServiceItem.class);
                             if (service != null) {
                                 service.setServiceId(doc.getId());
                                 popularServiceList.add(service);
                             }
                         }
                         // Sort manually
                         popularServiceList.sort((s1, s2) -> Integer.compare(s2.getBookingCount(), s1.getBookingCount()));
                         popularServiceAdapter.notifyDataSetChanged();
                     });
            });
    }
    
    private void loadNotificationCount() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count > 0) {
                        tvNotificationBadge.setVisibility(View.VISIBLE);
                        tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    } else {
                        tvNotificationBadge.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    tvNotificationBadge.setVisibility(View.GONE);
                });
        }
    }

    private void loadBanners() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        db.collection("banners")
            .orderBy("displayOrder")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                bannerList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    BannerItem banner = doc.toObject(BannerItem.class);
                    if (banner != null) {
                        banner.setBannerId(doc.getId());
                        bannerList.add(banner);
                    }
                }
                
                // Nếu không có banners, thêm placeholder
                if (bannerList.isEmpty()) {
                    // Keep empty or add default banner if needed
                }
                
                bannerAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                // Keep empty if failed
            });
    }

    private void setupSearchRecyclerView() {
        searchResultList = new ArrayList<>();
        allServicesBackup = new ArrayList<>();
        searchResultAdapter = new ServiceAdapter(getContext(), searchResultList);
        
        // Use Grid for search results
        searchResultAdapter.setGridMode(true);
        rvSearchResults.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));
        
         // Add Spacing (reusing the one we made)
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvSearchResults.addItemDecoration(new com.example.homeserviceapp.helpers.GridSpacingItemDecoration(2, spacingInPixels, true));
        
        rvSearchResults.setAdapter(searchResultAdapter);
    }

    private void loadAllServicesForSearch() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("services")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allServicesBackup.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ServiceItem service = doc.toObject(ServiceItem.class);
                    if (service != null) {
                        service.setServiceId(doc.getId());
                        allServicesBackup.add(service);
                    }
                }
            })
            .addOnFailureListener(e -> {
                // Fail silently or log
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

        if (allServicesBackup == null || allServicesBackup.isEmpty()) {
            return;
        }

        List<ServiceItem> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (ServiceItem item : allServicesBackup) {
            boolean matchesQuery = hasQuery ? item.getTitle().toLowerCase().contains(lowerCaseQuery) : true;
            boolean matchesPrice = item.getPrice() >= filterMinPrice && item.getPrice() <= filterMaxPrice;
            boolean matchesCategory = filterCategoryIds.isEmpty() || filterCategoryIds.contains(item.getCategoryId());

            if (matchesQuery && matchesPrice && matchesCategory) {
                filteredList.add(item);
            }
        }

        searchResultList.clear();
        searchResultList.addAll(filteredList);
        searchResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
