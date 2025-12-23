package com.example.homeserviceapp;

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

import com.example.homeserviceapp.models.BookingItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Views
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

    // Data from Intent
    private String serviceId;
    private String serviceName;
    private String servicePrice;
    private String providerName;
    private ServiceItem serviceItem;

    // Selected values
    private String selectedDate;
    private String selectedTimeSlot;

    // Booked slots
    private List<BookingItem> bookedSlots = new ArrayList<>();
    private List<Button> timeSlotButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get data from Intent
        serviceId = getIntent().getStringExtra("serviceId");
        serviceName = getIntent().getStringExtra("service_name");
        servicePrice = getIntent().getStringExtra("service_price");
        providerName = getIntent().getStringExtra("provider_name");

        if (serviceId == null || serviceId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Initialize calendar
        calendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi"));

        // Set selected date to today
        selectedDate = formatDateForFirebase(calendar.getTime());

        updateMonthLabel();

        // Set default button styles
        setDefaultButtonStyle(btnTime1);
        setDefaultButtonStyle(btnTime2);
        setDefaultButtonStyle(btnTime3);
        setDefaultButtonStyle(btnTime4);

        // Load service details from Firebase
        loadServiceDetails();

        // Setup calendar (IMPORTANT: Disable past dates)
        setupCalendar();

        // Load booked slots for today
        loadBookedSlots(selectedDate);

        // Setup listeners
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
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Add all time buttons to list
        timeSlotButtons.add(btnTime1);
        timeSlotButtons.add(btnTime2);
        timeSlotButtons.add(btnTime3);
        timeSlotButtons.add(btnTime4);
    }

    private void setupCalendar() {
        // IMPORTANT: Set minimum date to today (midnight)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Disable all dates before today
        calendarView.setMinDate(today.getTimeInMillis());

        Log.d(TAG, "Calendar minDate set to: " + today.getTime());
    }

    private void setupListeners() {
        // Previous month button
        btnPrevMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthLabel();
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        });

        // Next month button
        btnNextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthLabel();
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        });

        // Calendar date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateMonthLabel();

            // Update selected date
            selectedDate = formatDateForFirebase(calendar.getTime());

            // Reset selected time slot
            if (selectedTimeButton != null) {
                setDefaultButtonStyle(selectedTimeButton);
                selectedTimeButton = null;
                selectedTimeSlot = null;
            }

            // Load booked slots for new date
            loadBookedSlots(selectedDate);
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Time slot click listener
        View.OnClickListener timeClickListener = v -> {
            Button clicked = (Button) v;

            if (!clicked.isEnabled()) {
                Toast.makeText(this, "Khung giờ này đã được đặt", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reset old button
            if (selectedTimeButton != null) {
                setDefaultButtonStyle(selectedTimeButton);
            }

            // Set new button as selected
            setSelectedButtonStyle(clicked);
            selectedTimeButton = clicked;
            selectedTimeSlot = clicked.getText().toString();

            Log.d(TAG, "Selected time slot: " + selectedTimeSlot);
        };

        btnTime1.setOnClickListener(timeClickListener);
        btnTime2.setOnClickListener(timeClickListener);
        btnTime3.setOnClickListener(timeClickListener);
        btnTime4.setOnClickListener(timeClickListener);

        // Continue button
        btnContinue.setOnClickListener(v -> onContinueClicked());

        // Calendar icon button
        btnCalendarIcon.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CalendarActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        calendarView.setDate(calendar.getTimeInMillis(), false, true);
                        updateMonthLabel();

                        // Update selected date and load slots
                        selectedDate = formatDateForFirebase(calendar.getTime());
                        loadBookedSlots(selectedDate);
                    },
                    year, month, day
            );

            // IMPORTANT: Set minDate for DatePickerDialog too
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());

            datePickerDialog.show();
        });
    }

    private void loadServiceDetails() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        db.collection("services")
                .document(serviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
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
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "Error loading service", e);
                });
    }

    private void loadBookedSlots(String date) {
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

    private void disableBookedTimeSlots() {
        for (BookingItem booking : bookedSlots) {
            String timeSlot = booking.getScheduleTime();

            if (timeSlot == null || timeSlot.isEmpty()) {
                continue;
            }

            for (Button button : timeSlotButtons) {
                if (button.getText().toString().equals(timeSlot)) {
                    // Disable button
                    button.setEnabled(false);
                    button.setAlpha(0.3f);

                    // Set disabled background
                    button.setBackgroundResource(R.drawable.time_button_bg_disabled);

                    // If this was the selected button, clear selection
                    if (button == selectedTimeButton) {
                        selectedTimeButton = null;
                        selectedTimeSlot = null;
                    }
                    break;
                }
            }
        }
    }

    private void onContinueClicked() {
        // Validate inputs
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeSlot == null || selectedTimeSlot.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = "";
        if (etAddress != null) {
            address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                etAddress.setError("Vui lòng nhập địa chỉ");
                etAddress.requestFocus();
                return;
            }
        }

        // Check authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnContinue.setEnabled(false);

        // Double-check slot availability and create booking
        checkAndCreateBooking(address, currentUser);
    }

    private void checkAndCreateBooking(String address, FirebaseUser user) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = sdf.parse(selectedDate);
            if (dateObj == null) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnContinue.setEnabled(true);
                Toast.makeText(this, "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
                return;
            }

            Timestamp scheduleTimestamp = new Timestamp(dateObj);
            Timestamp startOfDay = scheduleTimestamp;

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Timestamp endOfDay = new Timestamp(cal.getTime());

            // Check if slot is still available
            db.collection("bookings")
                    .whereEqualTo("serviceId", serviceId)
                    .whereEqualTo("scheduleTime", selectedTimeSlot)
                    .whereGreaterThanOrEqualTo("scheduleDate", startOfDay)
                    .whereLessThan("scheduleDate", endOfDay)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Check if any booking is pending or confirmed
                        boolean slotTaken = false;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            BookingItem existingBooking = doc.toObject(BookingItem.class);
                            if (existingBooking.isPending() || existingBooking.isConfirmed()) {
                                slotTaken = true;
                                break;
                            }
                        }

                        if (!slotTaken) {
                            // Slot is still available, create booking
                            createBooking(address, user, scheduleTimestamp);
                        } else {
                            // Slot was just booked
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            btnContinue.setEnabled(true);
                            Toast.makeText(this,
                                    "Khung giờ này vừa được đặt. Vui lòng chọn khung giờ khác",
                                    Toast.LENGTH_LONG).show();

                            // Reload available slots
                            loadBookedSlots(selectedDate);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        btnContinue.setEnabled(true);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error checking availability", e);
                    });

        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            btnContinue.setEnabled(true);
            Toast.makeText(this, "Lỗi xử lý ngày: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing date", e);
        }
    }

    private void createBooking(String address, FirebaseUser user, Timestamp scheduleDate) {
        // Prepare location map
        Map<String, Object> location = new HashMap<>();
        location.put("address", address);
        location.put("coordinates", null);

        // Prepare customer info
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("customerId", user.getUid());
        customerInfo.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
        customerInfo.put("email", user.getEmail() != null ? user.getEmail() : "");
        customerInfo.put("phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

        // Prepare service info
        Map<String, Object> serviceInfo = new HashMap<>();
        if (serviceItem != null) {
            serviceInfo.put("serviceId", serviceItem.getServiceId());
            serviceInfo.put("title", serviceItem.getTitle());
            serviceInfo.put("price", serviceItem.getPrice());
            serviceInfo.put("priceUnit", serviceItem.getPriceUnit());
            serviceInfo.put("duration", serviceItem.getDuration());
        } else {
            // Use data from Intent if service not loaded
            serviceInfo.put("serviceId", serviceId);
            serviceInfo.put("title", serviceName != null ? serviceName : "");
            serviceInfo.put("price", 0);
        }

        // Create booking data
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("customerId", user.getUid());
        bookingData.put("serviceId", serviceId);
        bookingData.put("assignedAdminId", null);
        bookingData.put("serviceInfo", serviceInfo);
        bookingData.put("customerInfo", customerInfo);
        bookingData.put("scheduleDate", scheduleDate);
        bookingData.put("scheduleTime", selectedTimeSlot);
        bookingData.put("location", location);
        bookingData.put("totalPrice", serviceItem != null ? serviceItem.getPrice() : 0);
        bookingData.put("status", "pending");
        bookingData.put("paymentMethod", "cash");
        bookingData.put("paymentStatus", "unpaid");
        bookingData.put("notes", "");
        bookingData.put("cancellationReason", null);
        bookingData.put("createdAt", Timestamp.now());
        bookingData.put("updatedAt", Timestamp.now());
        bookingData.put("completedAt", null);

        // Save to Firestore
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    btnContinue.setEnabled(true);

                    Log.d(TAG, "Booking created with ID: " + documentReference.getId());

                    Toast.makeText(this, "Đặt lịch thành công!", Toast.LENGTH_LONG).show();

                    // Go to next screen
                    Intent intent = new Intent(CalendarActivity.this, PaymentMethodActivity.class);
                    intent.putExtra("bookingId", documentReference.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, "Lỗi đặt lịch: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error creating booking", e);
                });
    }

    private String formatDateForFirebase(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private void setDefaultButtonStyle(Button button) {
        button.setBackgroundResource(R.drawable.time_button_bg_default);
        button.setTextColor(Color.parseColor("#424242"));
    }

    private void setSelectedButtonStyle(Button button) {
        button.setBackgroundResource(R.drawable.time_button_bg_selected);
        button.setTextColor(Color.WHITE);
    }

    private void updateMonthLabel() {
        tvMonth.setText(monthFormat.format(calendar.getTime()));
    }
}
