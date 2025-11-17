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
import androidx.fragment.app.FragmentTransaction;

public class BookingFragment extends Fragment {

    private LinearLayout tabUpcoming, tabCompleted, tabCancelled;
    private TextView tvTabUpcoming, tvTabCompleted, tvTabCancelled;
    private View indicatorUpcoming, indicatorCompleted, indicatorCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        tabUpcoming = view.findViewById(R.id.tabUpcoming);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tabCancelled = view.findViewById(R.id.tabCancelled);

        tvTabUpcoming = view.findViewById(R.id.tvTabUpcoming);
        tvTabCompleted = view.findViewById(R.id.tvTabCompleted);
        tvTabCancelled = view.findViewById(R.id.tvTabCancelled);

        indicatorUpcoming = view.findViewById(R.id.indicatorUpcoming);
        indicatorCompleted = view.findViewById(R.id.indicatorCompleted);
        indicatorCancelled = view.findViewById(R.id.indicatorCancelled);

        loadChildFragment(new UpcomingFragment());

        tabUpcoming.setOnClickListener(v -> {
            setActiveTab(0);
            loadChildFragment(new UpcomingFragment());
        });

        tabCompleted.setOnClickListener(v -> {
            setActiveTab(1);
            loadChildFragment(new CompletedFragment());
        });

        tabCancelled.setOnClickListener(v -> {
            setActiveTab(2);
            loadChildFragment(new CancelledFragment());
        });

        return view;
    }

    private void loadChildFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.tabContentContainer, fragment);
        transaction.commit();
    }

    private void setActiveTab(int position) {
        tvTabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        tvTabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        tvTabCancelled.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        tvTabUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabCompleted.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabCancelled.setTypeface(null, android.graphics.Typeface.NORMAL);

        indicatorUpcoming.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        indicatorCompleted.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        indicatorCancelled.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));

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