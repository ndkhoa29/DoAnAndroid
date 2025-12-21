package com.example.homeserviceapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class TabAdapter extends FragmentStateAdapter {
    private final List<String> categoryNames;

    public TabAdapter(@NonNull FragmentActivity fragmentActivity, List<String> titles) {
        super(fragmentActivity);
        this.categoryNames = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ServiceListFragment fragment = new ServiceListFragment();
        Bundle args = new Bundle();
        // Truyền tên tiếng Việt (ví dụ: "Dọn dẹp") sang Fragment
        args.putString("CATEGORY_NAME", categoryNames.get(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return categoryNames != null ? categoryNames.size() : 0;
    }
}