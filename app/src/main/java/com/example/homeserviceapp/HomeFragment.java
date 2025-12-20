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
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ===== FIRESTORE =====
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private TextView tvCustomerName;

    // ===== UI =====
    private ViewPager2 viewPagerBanner;
    private BannerAdapter bannerAdapter;
    private List<BannerItem> bannerList;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    private TextView tvAllCategory, tvAllRated, tvAllPopular;
    private View dot1, dot2, dot3;
    private ImageView ivNotification, icFilter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ===== FIND VIEW =====
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

        // ===== FIRESTORE INIT =====
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document("admin_001");

        userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) return;

            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString("fullName");
                if (name != null && tvCustomerName != null) {
                    tvCustomerName.setText("Xin chào, " + name);
                }
            }
        });

        // ===== CLICK EVENTS =====
        icFilter.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FilterActivity.class))
        );

        ivNotification.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ThongBaoActivity.class))
        );

        tvAllCategory.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CategoryActivity.class))
        );

        tvAllRated.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TabServiceActivity.class))
        );

        tvAllPopular.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TabServiceActivity.class))
        );

        // ===== BANNER =====
        bannerList = new ArrayList<>();
        bannerList.add(new BannerItem(R.drawable.banner1));
        bannerList.add(new BannerItem(R.drawable.banner2));
        bannerList.add(new BannerItem(R.drawable.banner3));

        bannerAdapter = new BannerAdapter(bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);

        viewPagerBanner.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        updateDots(position);
                        sliderHandler.removeCallbacks(sliderRunnable);
                        sliderHandler.postDelayed(sliderRunnable, 3000);
                    }
                }
        );

        sliderHandler.postDelayed(sliderRunnable, 3000);

        setClickForAllCards(view);

        return view;
    }

    // ===== SERVICE CARD CLICK =====
    private void setClickForAllCards(View rootView) {
        findAndSetClickListener(rootView);
    }

    private void findAndSetClickListener(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);

                if (child.getId() == R.id.cardServiceItem) {
                    child.setOnClickListener(v -> openServiceDetail());
                }
                findAndSetClickListener(child);
            }
        }
    }

    private void openServiceDetail() {
        startActivity(new Intent(getActivity(), ServiceDetailActivity.class));
    }

    // ===== SLIDER =====
    private final Runnable sliderRunnable = () -> {
        if (bannerList == null || bannerList.isEmpty()) return;

        int currentItem = viewPagerBanner.getCurrentItem();
        int nextItem = (currentItem + 1) % bannerList.size();
        viewPagerBanner.setCurrentItem(nextItem, true);
    };

    private void updateDots(int position) {
        dot1.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        requireContext(),
                        position == 0 ? R.color.blue : R.color.gray
                )
        );
        dot2.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        requireContext(),
                        position == 1 ? R.color.blue : R.color.gray
                )
        );
        dot3.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        requireContext(),
                        position == 2 ? R.color.blue : R.color.gray
                )
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
