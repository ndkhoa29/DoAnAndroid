package com.example.homeserviceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeserviceapp.models.BannerItem;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<BannerItem> bannerList;

    public BannerAdapter(Context context, List<BannerItem> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem banner = bannerList.get(position);
        
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            String optimizedUrl = banner.getImageUrl();
            if (banner.getImageUrl().contains("cloudinary.com")) {
                optimizedUrl = com.example.homeserviceapp.helpers.CloudinaryHelper.getThumbnailUrl(banner.getImageUrl());
            }
            
            com.bumptech.glide.Glide.with(context)
                .load(optimizedUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(R.drawable.banner1)
                .error(R.drawable.banner1)
                .into(holder.ivBanner);
        } else {
            holder.ivBanner.setImageResource(R.drawable.banner1);
        }
    }

    @Override
    public int getItemCount() {
        return bannerList != null ? bannerList.size() : 0;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
        }
    }
}
