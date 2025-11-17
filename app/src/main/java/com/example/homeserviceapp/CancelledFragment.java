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

public class CancelledFragment extends Fragment {

    private RecyclerView rvCancelled;
    private CancelledAdapter adapter;
    private List<BookingItem> cancelledList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled, container, false);

        rvCancelled = view.findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(getContext()));

        cancelledList = new ArrayList<>();
        cancelledList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));
        cancelledList.add(new BookingItem(
                "ID526550",
                "10/12/2025",
                "Dọn dẹp",
                "100 ABC, Đà Nẵng",
                "14:00 PM",
                "60.000/h",
                R.drawable.ic_service_repo
        ));

        adapter = new CancelledAdapter(cancelledList);
        rvCancelled.setAdapter(adapter);

        return view;
    }
}