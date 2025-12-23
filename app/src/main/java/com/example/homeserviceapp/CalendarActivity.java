package com.example.homeserviceapp;

import com.example.homeserviceapp.BookingItem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private CalendarView calendarView;
    private TextView tvMonth;
    private ImageView btnBack;
    private ImageButton btnCalendarIcon, btnPrevMonth, btnNextMonth;
    private Button btnTime1, btnTime2, btnTime3, btnTime4, btnContinue;
    private EditText etAddress;
    private ProgressBar progressBar;

    private Button selectedTimeButton = null;
    private Calendar calendar;
    private SimpleDateFormat monthFormat;

    private String serviceId, serviceName, servicePrice, serviceImage, providerName;
    private ServiceItem serviceItem;
    private String selectedDate;
    private String selectedTimeSlot = null;

    private List<BookingItem> bookedSlots = new ArrayList<>();
    private List<Button> timeSlotButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Nhận dữ liệu từ Intent
        serviceId = getIntent().getStringExtra("service_id");
        serviceName = getIntent().getStringExtra("service_name");
        servicePrice = getIntent().getStringExtra("service_price");
        serviceImage = getIntent().getStringExtra("service_image");
        providerName = getIntent().getStringExtra("provider_name");

        if (serviceId == null || serviceId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        calendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi"));
        selectedDate = formatDateForFirebase(calendar.getTime());

        updateMonthLabel();
        loadServiceDetails();
        loadBookedSlots(selectedDate);
        setupListeners();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        tvMonth = findViewById(R.id.tvMonth);
        btnBack = findViewById(R.id.btnBack);
        btnCalendarIcon = findViewById(R.id.btnCalendarIcon);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnTime1 = findViewById(R.id.btnTime1);
        btnTime2 = findViewById(R.id.btnTime2);
        btnTime3 = findViewById(R.id.btnTime3);
        btnTime4 = findViewById(R.id.btnTime4);
        btnContinue = findViewById(R.id.btnContinue);
        etAddress = findViewById(R.id.etAddress);
        progressBar = findViewById(R.id.progressBar);

        timeSlotButtons.add(btnTime1);
        timeSlotButtons.add(btnTime2);
        timeSlotButtons.add(btnTime3);
        timeSlotButtons.add(btnTime4);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateMonthLabel();
            selectedDate = formatDateForFirebase(calendar.getTime());
            resetTimeSelection();
            loadBookedSlots(selectedDate);
        });

        View.OnClickListener timeClickListener = v -> {
            Button clicked = (Button) v;
            if (selectedTimeButton != null) setDefaultButtonStyle(selectedTimeButton);
            setSelectedButtonStyle(clicked);
            selectedTimeButton = clicked;
            selectedTimeSlot = clicked.getText().toString();
        };

        for (Button btn : timeSlotButtons) btn.setOnClickListener(timeClickListener);

        btnContinue.setOnClickListener(v -> onContinueClicked());

        btnCalendarIcon.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                calendarView.setDate(calendar.getTimeInMillis(), false, true);
                updateMonthLabel();
                selectedDate = formatDateForFirebase(calendar.getTime());
                resetTimeSelection();
                loadBookedSlots(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void resetTimeSelection() {
        if (selectedTimeButton != null) setDefaultButtonStyle(selectedTimeButton);
        selectedTimeButton = null;
        selectedTimeSlot = null;
    }

    private void loadServiceDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(doc -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (doc.exists()) {
                        serviceItem = doc.toObject(ServiceItem.class);
                        if (serviceItem != null) serviceItem.setServiceId(doc.getId());
                    }
                });
    }

    private void loadBookedSlots(String date) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        for (Button btn : timeSlotButtons) {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
            setDefaultButtonStyle(btn);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = sdf.parse(date);
            Timestamp start = new Timestamp(dateObj);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Timestamp end = new Timestamp(cal.getTime());

            db.collection("bookings")
                    .whereEqualTo("serviceId", serviceId)
                    .whereGreaterThanOrEqualTo("scheduleDate", start)
                    .whereLessThan("scheduleDate", end)
                    .get()
                    .addOnSuccessListener(queryDocs -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        bookedSlots.clear();
                        for (QueryDocumentSnapshot doc : queryDocs) {
                            BookingItem booking = doc.toObject(BookingItem.class);
                            if (booking.isPending() || booking.isConfirmed()) bookedSlots.add(booking);
                        }
                        disableBookedTimeSlots();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private void disableBookedTimeSlots() {
        for (BookingItem booking : bookedSlots) {
            for (Button btn : timeSlotButtons) {
                if (btn.getText().toString().equals(booking.getScheduleTime())) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }
            }
        }
    }

    private void onContinueClicked() {
        String address = etAddress.getText().toString().trim();
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Vui lòng chọn giờ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            return;
        }

        Intent intent = new Intent(this, ChiTietDonDatActivity.class);
        intent.putExtra("SERVICE_ID", serviceId);
        intent.putExtra("SERVICE_NAME", serviceName);
        intent.putExtra("SERVICE_PRICE", servicePrice);
        intent.putExtra("SERVICE_IMAGE", serviceImage);
        intent.putExtra("BOOKING_DATE", selectedDate);
        intent.putExtra("BOOKING_TIME", selectedTimeSlot);
        intent.putExtra("BOOKING_ADDRESS", address);
        startActivity(intent);
    }

    private String formatDateForFirebase(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private void setDefaultButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.time_button_bg_default);
        b.setTextColor(Color.parseColor("#424242"));
    }

    private void setSelectedButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.time_button_bg_selected);
        b.setTextColor(Color.WHITE);
    }

    private void updateMonthLabel() {
        tvMonth.setText(monthFormat.format(calendar.getTime()));
    }
}