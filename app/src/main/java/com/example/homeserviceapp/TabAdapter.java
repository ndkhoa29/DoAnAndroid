package com.example.homeserviceapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.os.Bundle;

public class TabAdapter extends FragmentStateAdapter {

    private String[] tabTitles;

    public TabAdapter(@NonNull FragmentActivity fragmentActivity, String[] titles) {
        super(fragmentActivity);
        this.tabTitles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tạo Fragment mới cho mỗi tab
        ServiceListFragment fragment = new ServiceListFragment();

        // Truyền tên danh mục vào Fragment để nó có thể tải dữ liệu tương ứng
        Bundle args = new Bundle();
        args.putString("CATEGORY_NAME", tabTitles[position]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
}