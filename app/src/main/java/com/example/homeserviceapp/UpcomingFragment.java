package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UpcomingFragment extends Fragment {

    private RecyclerView rvUpcoming;
    private UpcomingAdapter adapter;
    private List<BookingItem> upcomingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        upcomingList = new ArrayList<>();
        upcomingList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));
        upcomingList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));

        adapter = new UpcomingAdapter(upcomingList);
        rvUpcoming.setAdapter(adapter);

        return view;
    }
}