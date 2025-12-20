package com.example.homeserviceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private static final String TAG ="CalendarActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CalendarView calendarView;
    private TextView tvMonth;
    private ImageView btnBack;
    private ImageButton btnCalendarIcon, btnPrevMonth, btnNextMonth;
    private Button btnTime1, btnTime2, btnTime3, btnTime4, btnContinue;
    private Button selectedTimeButton = null;
    private Calendar calendar;
    private SimpleDateFormat monthFormat;

    private String serviceId;
    private String serviceName;
    private String servicePrice;
    private String providerName;
    private ServiceItem serviceItem;

    private String selectedDate;
    private String selectedTimeSlot;

    private List<BookingItem> bookedSlots = new ArrayList<>();
    private List<Button> timeSlotButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        serviceId = getIntent().getStringExtra("serviceId");
        serviceName = getIntent().getStringExtra("service_name");
        servicePrice = getIntent().getStringExtra("service_price");
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

        setDefaultButtonStyle(btnTime1);
        setDefaultButtonStyle(btnTime2);
        setDefaultButtonStyle(btnTime3);
        setDefaultButtonStyle(btnTime4);

        loadServiceDetails();

        loadBookedSlots(selectedDate);

        setupListeners();
    }

    private void loadBookedSlots(String selectedDate) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Reset all time slot buttons
        for (Button button : timeSlotButtons) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            setDefaultButtonStyle(button);
        }

        // Convert date string to Timestamp for query
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = sdf.parse(date);
            if (dateObj == null) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                return;
            }

            Timestamp startOfDay = new Timestamp(dateObj);

            // Add 24 hours for end of day
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Timestamp endOfDay = new Timestamp(cal.getTime());

            // Query bookings for this date and service
            db.collection("bookings")
                    .whereEqualTo("serviceId", serviceId)
                    .whereGreaterThanOrEqualTo("scheduleDate", startOfDay)
                    .whereLessThan("scheduleDate", endOfDay)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        bookedSlots.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            BookingItem booking = document.toObject(BookingItem.class);
                            booking.setBookingId(document.getId());

                            // Only count pending and confirmed bookings
                            if (booking.isPending() || booking.isConfirmed()) {
                                bookedSlots.add(booking);
                            }
                        }

                        Log.d(TAG, "Found " + bookedSlots.size() + " bookings for date: " + date);

                        // Disable booked time slots
                        disableBookedTimeSlots();
                    })
                    .addOnFailureListener(e -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "Lỗi tải lịch: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading bookings", e);
                    });

        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Log.e(TAG, "Error parsing date", e);
        }
    }

    private void setupListeners() {
    }

    private void loadServiceDetails() {
        Object progressBar = null;
        if (progressBar != null) {
            progressBar.notify(View.VISIBLE);
        }

        db.collection("services")
                .document(serviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) {
                        progressBar.notify(View.GONE);
                    }

                    if (documentSnapshot.exists()) {
                        serviceItem = documentSnapshot.toObject(ServiceItem.class);
                        if (serviceItem != null) {
                            serviceItem.setServiceId(documentSnapshot.getId());
                            Log.d(TAG, "Service loaded: " + serviceItem.getTitle());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.notify(View.GONE);
                    }
                    Log.e(TAG, "Error loading service", e);
                });
    }

    private void setDefaultButtonStyle(Button btnTime1) {

    }

    private String formatDateForFirebase(Date time) {
        return getString();
    }

    @Nullable
    private static String getString() {
        return null;
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

        View progressBar;
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        timeSlotButtons.add(btnTime1);
        timeSlotButtons.add(btnTime2);
        timeSlotButtons.add(btnTime3);
        timeSlotButtons.add(btnTime4);
    }


    private void updateMonthLabel() {
        tvMonth.setText(monthFormat.format(calendar.getTime()));
    }
}