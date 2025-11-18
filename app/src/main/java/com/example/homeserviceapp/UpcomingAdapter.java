package com.example.homeserviceapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.BookingViewHolder> {

    private List<BookingItem> bookingList;

    public UpcomingAdapter(List<BookingItem> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_upcoming, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItem booking = bookingList.get(position);

        holder.tvBookingId.setText(booking.getBookingId());
        holder.tvBookingDate.setText(booking.getDate());
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvLocation.setText(booking.getLocation());
        holder.tvTime.setText(booking.getTime());
        holder.tvPrice.setText(booking.getPrice());
        holder.ivServiceImage.setImageResource(booking.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChiTietDonHangActivity.class);
            intent.putExtra("BOOKING_ID", booking.getBookingId());
            intent.putExtra("SERVICE_NAME", booking.getServiceName());
            intent.putExtra("DATE", booking.getDate());
            intent.putExtra("TIME", booking.getTime());
            intent.putExtra("LOCATION", booking.getLocation());
            intent.putExtra("PRICE", booking.getPrice());
            intent.putExtra("STATUS", "upcoming");
            v.getContext().startActivity(intent);
        });

        holder.btnCancelBooking.setOnClickListener(v -> {
            Toast.makeText(v.getContext(),
                    "Đã huỷ đơn: " + booking.getBookingId(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvBookingDate, tvServiceName;
        TextView tvLocation, tvTime, tvPrice, btnCancelBooking;
        ImageView ivServiceImage;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
            btnCancelBooking = itemView.findViewById(R.id.btnCancel);
        }
    }
}