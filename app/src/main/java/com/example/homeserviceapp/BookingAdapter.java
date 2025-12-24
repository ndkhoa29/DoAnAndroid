package com.example.homeserviceapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Booking;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onActionClick(Booking booking, String action);
        void onItemClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvBookingCode.setText(booking.getCode() != null ? booking.getCode() : booking.getBookingId());
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvDateTime.setText(booking.getBookingTime() + " - " + booking.getBookingDate());
        holder.tvAddress.setText(booking.getAddress());

        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(currencyFormat.format(booking.getPrice()) + " đ");

        if (booking.getServiceImage() != null && !booking.getServiceImage().isEmpty()) {
            Glide.with(context)
                .load(booking.getServiceImage())
                .placeholder(R.drawable.img_office_cleaning)
                .into(holder.imgService);
        } else {
            holder.imgService.setImageResource(R.drawable.img_office_cleaning);
        }

        String status = booking.getStatus();
        holder.btnAction.setVisibility(View.GONE);
        holder.tvStatus.setVisibility(View.VISIBLE);

        if ("Pending".equals(status)) {
            holder.tvStatus.setText("Chờ xác nhận");
            holder.tvStatus.setTextColor(Color.parseColor("#FFA000")); // Orange
            
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Hủy đơn");
            holder.btnAction.setBackgroundColor(Color.RED);
            holder.btnAction.setOnClickListener(v -> listener.onActionClick(booking, "cancel"));

        } else if ("InProgress".equals(status)) {
            holder.tvStatus.setText("Đang thực hiện");
            holder.tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));

            holder.btnAction.setVisibility(View.GONE);
            
        } else if ("TaskCompleted".equals(status)) {
            holder.tvStatus.setText("Chờ thanh toán");
            holder.tvStatus.setTextColor(Color.parseColor("#9C27B0"));
            
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Thanh toán");
            holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.btnAction.setOnClickListener(v -> listener.onActionClick(booking, "pay"));

        } else if ("Paid".equals(status)) {
            holder.tvStatus.setText("Hoàn thành");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));

            if (!booking.getHasReview()) {
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText("Đánh giá");
                holder.btnAction.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                holder.btnAction.setOnClickListener(v -> listener.onActionClick(booking, "rate"));
            } else {
                holder.btnAction.setVisibility(View.GONE);
            }

        } else if ("Cancelled".equals(status)) {
            holder.tvStatus.setText("Đã hủy");
            holder.tvStatus.setTextColor(Color.RED);
            holder.btnAction.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText(status);
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgService;
        TextView tvServiceName, tvDateTime, tvAddress, tvPrice, tvStatus, tvBookingCode;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgService = itemView.findViewById(R.id.imgService);
            tvBookingCode = itemView.findViewById(R.id.tvBookingCode);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
