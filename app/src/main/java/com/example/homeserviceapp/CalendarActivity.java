package com.example.homeserviceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvMonth;
    private ImageView btnBack;
    private ImageButton btnCalendarIcon, btnPrevMonth, btnNextMonth;
    private Button btnTime1, btnTime2, btnTime3, btnTime4, btnContinue;
    private Button selectedTimeButton = null; // lưu button giờ đang chọn
    private Calendar calendar;
    private SimpleDateFormat monthFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Ánh xạ view
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

        calendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi")); // Việt Nam

        updateMonthLabel();

        // Bấm Prev / Next đổi tháng
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

        // Chọn ngày trong CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateMonthLabel();
        });

        // -------------------- Nút Back --------------------
        btnBack.setOnClickListener(v -> finish());

        // -------------------- Chọn giờ --------------------
        View.OnClickListener timeClickListener = v -> {
            Button clicked = (Button) v;
            if (selectedTimeButton != null) {
                selectedTimeButton.setBackgroundResource(R.drawable.time_button_bg_default);
            }
            clicked.setBackgroundResource(R.drawable.time_button_bg_selected);
            selectedTimeButton = clicked;
        };

        btnTime1.setOnClickListener(timeClickListener);
        btnTime2.setOnClickListener(timeClickListener);
        btnTime3.setOnClickListener(timeClickListener);
        btnTime4.setOnClickListener(timeClickListener);

        // -------------------- Continue --------------------
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, ReviewSummaryActivity.class);
            startActivity(intent);
        });

        // -------------------- Calendar Icon --------------------
        btnCalendarIcon.setOnClickListener(v -> {
            // Lấy ngày hiện tại
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Tạo DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CalendarActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Cập nhật Calendar
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        // Cập nhật CalendarView
                        calendarView.setDate(calendar.getTimeInMillis(), false, true);

                        // Cập nhật nhãn tháng
                        updateMonthLabel();
                    },
                    year, month, day
            );

            // Hiển thị dialog
            datePickerDialog.show();
        });
    }

    private void updateMonthLabel() {
        tvMonth.setText(monthFormat.format(calendar.getTime()));
    }
}
