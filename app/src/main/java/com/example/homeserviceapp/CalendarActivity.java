package com.example.homeserviceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvMonth;
    private ImageView btnBack;
    private ImageButton btnCalendarIcon, btnPrevMonth, btnNextMonth;
    private Button btnTime1, btnTime2, btnTime3, btnTime4, btnContinue;
    private Button selectedTimeButton = null;
    private Calendar calendar;
    private SimpleDateFormat monthFormat;

    private String serviceName;
    private String serviceId;
    private String servicePrice;
    private String serviceImage;
    private String selectedTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        serviceName = getIntent().getStringExtra("service_name");
        serviceId = getIntent().getStringExtra("service_id");
        servicePrice = getIntent().getStringExtra("service_price");
        serviceImage = getIntent().getStringExtra("service_image"); // Get Image URL

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

        android.widget.EditText etAddress = findViewById(R.id.etAddress);

        calendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi")); // Việt Nam

        updateMonthLabel();

        setDefaultButtonStyle(btnTime1);
        setDefaultButtonStyle(btnTime2);
        setDefaultButtonStyle(btnTime3);
        setDefaultButtonStyle(btnTime4);

        btnPrevMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthLabel();
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        });

        btnNextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthLabel();
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateMonthLabel();
        });

        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener timeClickListener = v -> {
            Button clicked = (Button) v;

            if (selectedTimeButton != null) {
                setDefaultButtonStyle(selectedTimeButton);
            }

            setSelectedButtonStyle(clicked);

            selectedTimeButton = clicked;
            selectedTime = clicked.getText().toString();
        };

        btnTime1.setOnClickListener(timeClickListener);
        btnTime2.setOnClickListener(timeClickListener);
        btnTime3.setOnClickListener(timeClickListener);
        btnTime4.setOnClickListener(timeClickListener);

        btnContinue.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();

            if (selectedTime == null) {
                Toast.makeText(this, "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show();
                return;
            }
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String selectedDate = dateFormat.format(calendar.getTime());

            Intent intent = new Intent(CalendarActivity.this, ChiTietDonDatActivity.class);
            intent.putExtra("SERVICE_NAME", serviceName);
            intent.putExtra("SERVICE_ID", serviceId);
            intent.putExtra("SERVICE_PRICE", servicePrice);
            intent.putExtra("SERVICE_IMAGE", serviceImage); // Pass Image URL
            intent.putExtra("BOOKING_DATE", selectedDate);
            intent.putExtra("BOOKING_TIME", selectedTime);
            intent.putExtra("BOOKING_ADDRESS", address);
            startActivity(intent);
        });

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
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
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