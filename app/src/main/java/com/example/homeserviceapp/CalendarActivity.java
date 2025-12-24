package com.example.homeserviceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserviceapp.models.BookingFirestore;
import com.example.homeserviceapp.models.ServiceItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

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

    private Calendar calendar;
    private SimpleDateFormat monthFormat;

    private Button selectedTimeButton;
    private String selectedDate;
    private String selectedTimeSlot;

    private String serviceId, serviceName, servicePrice, serviceImage;
    private ServiceItem serviceItem;

    private List<BookingFirestore> bookedSlots = new ArrayList<>();
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
        serviceImage = getIntent().getStringExtra("service_image");

        if (serviceId == null || serviceId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        calendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi"));
        selectedDate = formatDate(calendar.getTime());

        setupCalendar();
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

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        calendarView.setMinDate(today.getTimeInMillis());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, y, m, d) -> {
            calendar.set(y, m, d);
            selectedDate = formatDate(calendar.getTime());
            selectedTimeSlot = null;
            selectedTimeButton = null;
            loadBookedSlots(selectedDate);
            updateMonthLabel();
        });

        View.OnClickListener timeClick = v -> {
            Button b = (Button) v;
            if (!b.isEnabled()) return;

            if (selectedTimeButton != null) {
                setDefaultButtonStyle(selectedTimeButton);
            }
            setSelectedButtonStyle(b);
            selectedTimeButton = b;
            selectedTimeSlot = b.getText().toString();
        };

        btnTime1.setOnClickListener(timeClick);
        btnTime2.setOnClickListener(timeClick);
        btnTime3.setOnClickListener(timeClick);
        btnTime4.setOnClickListener(timeClick);

        btnContinue.setOnClickListener(v -> onContinue());
    }

    private void loadServiceDetails() {
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        serviceItem = doc.toObject(ServiceItem.class);
                        if (serviceItem != null)
                            serviceItem.setServiceId(doc.getId());
                    }
                });
    }

    private void loadBookedSlots(String date) {
        for (Button b : timeSlotButtons) {
            b.setEnabled(true);
            setDefaultButtonStyle(b);
        }

        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            Timestamp start = new Timestamp(d);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.add(Calendar.DAY_OF_MONTH, 1);
            Timestamp end = new Timestamp(c.getTime());

            db.collection("bookings")
                    .whereEqualTo("serviceId", serviceId)
                    .whereGreaterThanOrEqualTo("scheduleDate", start)
                    .whereLessThan("scheduleDate", end)
                    .get()
                    .addOnSuccessListener(qs -> {
                        bookedSlots.clear();
                        for (QueryDocumentSnapshot doc : qs) {
                            BookingFirestore b = doc.toObject(BookingFirestore.class);
                            b.setBookingId(doc.getId());
                            if (b.isPending() || b.isConfirmed()) {
                                bookedSlots.add(b);
                            }
                        }
                        disableBookedTimeSlots();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Date error", e);
        }
    }

    private void disableBookedTimeSlots() {
        for (BookingFirestore b : bookedSlots) {
            for (Button btn : timeSlotButtons) {
                if (btn.getText().toString().equals(b.getScheduleTime())) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }
            }
        }
    }

    private void onContinue() {
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Chọn khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Nhập địa chỉ");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        createBooking(user, etAddress.getText().toString().trim());
    }

    private void createBooking(FirebaseUser user, String address) {
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", user.getUid());
        data.put("serviceId", serviceId);
        data.put("scheduleDate", Timestamp.now());
        data.put("scheduleTime", selectedTimeSlot);
        data.put("status", "pending");
        data.put("location", address);

        db.collection("bookings").add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đặt lịch thành công", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, PaymentMethodActivity.class));
                    finish();
                });
    }

    private String formatDate(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
    }

    private void setDefaultButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.time_button_bg_default);
        b.setTextColor(Color.BLACK);
    }

    private void setSelectedButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.time_button_bg_selected);
        b.setTextColor(Color.WHITE);
    }

    private void updateMonthLabel() {
        tvMonth.setText(monthFormat.format(calendar.getTime()));
    }
}
