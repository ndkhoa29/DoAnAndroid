package com.example.homeserviceapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class CancelledAdapter extends RecyclerView.Adapter<CancelledAdapter.ViewHolder> {

    private List<BookingItem> bookingList;

    public CancelledAdapter(List<BookingItem> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_cancelled, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingItem booking = bookingList.get(position);

        holder.tvBookingId.setText(booking.getBookingId());
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvLocation.setText(booking.getLocation());
        holder.tvPrice.setText(booking.getPrice());
        holder.tvTime.setText(booking.getScheduleTime());
        holder.ivServiceImage.setImageResource(booking.getImageResId());

        // 🔹 Format ngày từ Timestamp
        if (booking.getScheduleDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(booking.getScheduleDate().toDate());
            holder.tvBookingDate.setText(dateStr);
        } else {
            holder.tvBookingDate.setText("N/A");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChiTietDonHangActivity.class);
            intent.putExtra("BOOKING_ID", booking.getBookingId());
            intent.putExtra("SERVICE_NAME", booking.getServiceName());
            intent.putExtra("DATE", holder.tvBookingDate.getText().toString());
            intent.putExtra("TIME", booking.getScheduleTime());
            intent.putExtra("LOCATION", booking.getLocation());
            intent.putExtra("PRICE", booking.getPrice());
            intent.putExtra("STATUS", "cancelled");
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvBookingDate, tvServiceName;
        TextView tvLocation, tvTime, tvPrice;
        ImageView ivServiceImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
        }
    }
}
