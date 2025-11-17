package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceDetailActivity extends AppCompatActivity {

    ImageView btnChat, btnCall;
    Button btnBookNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnBookNow = findViewById(R.id.btnBookNow);

        // Nút chat (giả lập)
        btnChat.setOnClickListener(v ->
                Toast.makeText(this, "Opening chat with Jenny...", Toast.LENGTH_SHORT).show()
        );

        // Nút gọi điện
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+1800123456"));
            startActivity(intent);
        });

        // Nút "Book Now"
        btnBookNow.setOnClickListener(v ->
                Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show()
        );
    }
}
