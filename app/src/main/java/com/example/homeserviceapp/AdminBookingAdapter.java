package com.example.homeserviceapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.models.Booking;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private OnBookingAdminActionListener listener;

    public interface OnBookingAdminActionListener {
        void onActionClick(Booking booking, String action);
    }

    public AdminBookingAdapter(Context context, List<Booking> bookingList, OnBookingAdminActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvBookingId.setText(booking.getCode() != null ? booking.getCode() : booking.getBookingId());
        holder.tvCustomerName.setText(booking.getUserName() != null ? booking.getUserName() : "Khách hàng");
        holder.tvServiceName.setText(booking.getServiceName());
        
        holder.tvDateTime.setText(booking.getBookingTime() + " - " + booking.getBookingDate());
        
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(currencyFormat.format(booking.getPrice()) + " đ");

        // Status Logic
        String status = booking.getStatus();
        holder.tvStatus.setText(status);

        if ("Pending".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.tvStatus.setText("Chờ xác nhận");
        } else if ("InProgress".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            holder.tvStatus.setText("Đang thực hiện");
        } else if ("TaskCompleted".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#9C27B0"));
            holder.tvStatus.setText("Chờ thanh toán");
        } else if ("Paid".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.tvStatus.setText("Hoàn thành");
        } else if ("Cancelled".equals(status)) {
            holder.tvStatus.setTextColor(Color.RED);
            holder.tvStatus.setText("Đã hủy");
        }

        holder.btnAction.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onActionClick(booking, "view"));

        if ("Pending".equals(status)) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Xác nhận");
            holder.btnAction.setOnClickListener(v -> listener.onActionClick(booking, "confirm_start"));
        } else if ("InProgress".equals(status)) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Xong việc");
            holder.btnAction.setOnClickListener(v -> listener.onActionClick(booking, "confirm_finish"));
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvCustomerName, tvServiceName, tvDateTime, tvPrice, tvStatus;
        Button btnAction;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime); // Matches XML ID
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAdminAction);
        }
    }
}
