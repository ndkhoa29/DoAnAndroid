package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {

    private LinearLayout tabUpcoming, tabCompleted, tabCancelled;
    private TextView tvTabUpcoming, tvTabCompleted, tvTabCancelled;
    private View indicatorUpcoming, indicatorCompleted, indicatorCancelled;
    private RecyclerView rvBookings;
    private BookingAdapter bookingAdapter;
    private List<BookingItem> upcomingList, completedList, cancelledList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Khởi tạo views
        tabUpcoming = view.findViewById(R.id.tabUpcoming);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tabCancelled = view.findViewById(R.id.tabCancelled);

        tvTabUpcoming = view.findViewById(R.id.tvTabUpcoming);
        tvTabCompleted = view.findViewById(R.id.tvTabCompleted);
        tvTabCancelled = view.findViewById(R.id.tvTabCancelled);

        indicatorUpcoming = view.findViewById(R.id.indicatorUpcoming);
        indicatorCompleted = view.findViewById(R.id.indicatorCompleted);
        indicatorCancelled = view.findViewById(R.id.indicatorCancelled);

        rvBookings = view.findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo dữ liệu mẫu
        createSampleData();

        // Setup adapter với dữ liệu upcoming
        bookingAdapter = new BookingAdapter(upcomingList);
        rvBookings.setAdapter(bookingAdapter);

        // Xử lý click tabs
        tabUpcoming.setOnClickListener(v -> {
            setActiveTab(0);
            bookingAdapter = new BookingAdapter(upcomingList);
            rvBookings.setAdapter(bookingAdapter);
        });

        tabCompleted.setOnClickListener(v -> {
            setActiveTab(1);
            bookingAdapter = new BookingAdapter(completedList);
            rvBookings.setAdapter(bookingAdapter);
        });

        tabCancelled.setOnClickListener(v -> {
            setActiveTab(2);
            bookingAdapter = new BookingAdapter(cancelledList);
            rvBookings.setAdapter(bookingAdapter);
        });

        return view;
    }

    private void createSampleData() {
        // Upcoming bookings
        upcomingList = new ArrayList<>();
        upcomingList.add(new BookingItem(
                "ID526565",
                "22 Jan 2024",
                "Office Cleaning",
                "8502 Preston Rd.",
                "08:00 AM",
                "$30/h",
                R.drawable.service_placeholder
        ));
        upcomingList.add(new BookingItem(
                "ID526565",
                "23 Jan 2024",
                "Home Cleaning",
                "3517 W. Gray St. Utica",
                "10:00 AM",
                "$30/h",
                R.drawable.service_placeholder
        ));

        // Completed bookings
        completedList = new ArrayList<>();
        completedList.add(new BookingItem(
                "ID526560",
                "15 Jan 2024",
                "Kitchen Cleaning",
                "1234 Main St.",
                "09:00 AM",
                "$35/h",
                R.drawable.service_placeholder
        ));

        // Cancelled bookings
        cancelledList = new ArrayList<>();
        cancelledList.add(new BookingItem(
                "ID526550",
                "10 Jan 2024",
                "Car Washing",
                "5678 Oak Ave.",
                "14:00 PM",
                "$40/h",
                R.drawable.service_placeholder
        ));
    }

    private void setActiveTab(int position) {
        // Reset tất cả tabs
        tvTabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        tvTabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        tvTabCancelled.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        tvTabUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabCompleted.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabCancelled.setTypeface(null, android.graphics.Typeface.NORMAL);

        indicatorUpcoming.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        indicatorCompleted.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        indicatorCancelled.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));

        // Set active tab
        int activeColor = ContextCompat.getColor(requireContext(), R.color.blue);
        switch (position) {
            case 0:
                tvTabUpcoming.setTextColor(activeColor);
                tvTabUpcoming.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorUpcoming.setBackgroundColor(activeColor);
                break;
            case 1:
                tvTabCompleted.setTextColor(activeColor);
                tvTabCompleted.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorCompleted.setBackgroundColor(activeColor);
                break;
            case 2:
                tvTabCancelled.setTextColor(activeColor);
                tvTabCancelled.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorCancelled.setBackgroundColor(activeColor);
                break;
        }
    }
}