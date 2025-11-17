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

public class CompletedFragment extends Fragment {

    private RecyclerView rvCompleted;
    private CompletedAdapter adapter;
    private List<BookingItem> completedList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        rvCompleted = view.findViewById(R.id.rvCompleted);
        rvCompleted.setLayoutManager(new LinearLayoutManager(getContext()));

        completedList = new ArrayList<>();
        completedList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));
        completedList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));

        adapter = new CompletedAdapter(completedList);
        rvCompleted.setAdapter(adapter);

        return view;
    }
}