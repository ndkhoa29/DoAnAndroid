package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

public class MessageActivity extends AppCompatActivity {

    private ImageView ivBack, ivPhoneCall, btnSend;
    private EditText etMessage;
    private ScrollView scrollView;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ivBack = findViewById(R.id.ivBack);
        ivPhoneCall = findViewById(R.id.ivPhoneCall);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        scrollView = findViewById(R.id.scrollView);
        rootLayout = findViewById(R.id.main);

        ivBack.setOnClickListener(v -> finish());

        ivPhoneCall.setOnClickListener(v -> {
            // TODO: Thêm chức năng gọi điện
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // TODO: Xử lý gửi tin nhắn
                etMessage.setText("");
            }
        });

        setupKeyboardListener();

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() ->
                        scrollView.fullScroll(View.FOCUS_DOWN), 200);
            }
        });
    }

    private void setupKeyboardListener() {
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    private int previousHeight = 0;

                    @Override
                    public void onGlobalLayout() {
                        int currentHeight = rootLayout.getHeight();

                        if (previousHeight > currentHeight && previousHeight != 0) {
                            scrollView.postDelayed(() ->
                                    scrollView.fullScroll(View.FOCUS_DOWN), 100);
                        }

                        previousHeight = currentHeight;
                    }
                }
        );
    }
}