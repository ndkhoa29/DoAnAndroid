package com.example.homeserviceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardMomo, cardVnpay, cardZalopay, cardViettelMoney;
    private RadioButton rbMomo, rbVnpay, rbZalopay, rbViettelMoney;
    private Button btnAddNewCard, btnPayNow;
    private TextView tvTotalAmount;

    private String selectedPaymentMethod = "VNPay";
    private double totalAmount = 0;
    private String bookingId;
    
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        db = FirebaseFirestore.getInstance();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        cardMomo = findViewById(R.id.cardMomo);
        cardVnpay = findViewById(R.id.cardVnpay);
        cardZalopay = findViewById(R.id.cardZalopay);
        cardViettelMoney = findViewById(R.id.cardViettelMoney);

        rbMomo = findViewById(R.id.rbMomo);
        rbVnpay = findViewById(R.id.rbVnpay);
        rbZalopay = findViewById(R.id.rbZalopay);
        rbViettelMoney = findViewById(R.id.rbViettelMoney);

        btnAddNewCard = findViewById(R.id.btnAddNewCard);
        btnPayNow = findViewById(R.id.btnPayNow);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        setupPaymentCard(cardMomo, rbMomo, "MoMo");
        setupPaymentCard(cardVnpay, rbVnpay, "VNPay");
        setupPaymentCard(cardZalopay, rbZalopay, "ZaloPay");
        setupPaymentCard(cardViettelMoney, rbViettelMoney, "Viettel Money");

        btnAddNewCard.setOnClickListener(v ->
                Toast.makeText(PaymentMethodActivity.this,
                        "Chức năng thêm thẻ mới", Toast.LENGTH_SHORT).show()
        );

        btnPayNow.setOnClickListener(v -> processPayment());
    }

    private void setupPaymentCard(CardView card, RadioButton radio, String method) {
        card.setOnClickListener(v -> selectPaymentMethod(method, radio));
    }

    private void loadData() {
        Intent intent = getIntent();
        if (intent != null) {
            totalAmount = intent.getDoubleExtra("total_amount", 0);
            bookingId = intent.getStringExtra("booking_id");
            selectedPaymentMethod = intent.getStringExtra("payment_method");
            if (selectedPaymentMethod == null) selectedPaymentMethod = "VNPay";
        }

        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(currencyFormat.format(totalAmount) + " đ");
        setDefaultPaymentMethod();
    }

    private void setDefaultPaymentMethod() {
        switch (selectedPaymentMethod) {
            case "MoMo": selectPaymentMethod("MoMo", rbMomo); break;
            case "VNPay": selectPaymentMethod("VNPay", rbVnpay); break;
            case "ZaloPay": selectPaymentMethod("ZaloPay", rbZalopay); break;
            case "Viettel Money": selectPaymentMethod("Viettel Money", rbViettelMoney); break;
            default: selectPaymentMethod("VNPay", rbVnpay); break;
        }
    }

    private void selectPaymentMethod(String method, RadioButton selectedRadio) {
        rbMomo.setChecked(false);
        rbVnpay.setChecked(false);
        rbZalopay.setChecked(false);
        rbViettelMoney.setChecked(false);

        selectedRadio.setChecked(true);
        selectedPaymentMethod = method;

        Toast.makeText(this, "Đã chọn: " + method, Toast.LENGTH_SHORT).show();
    }

    private void processPayment() {
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        showProcessingDialog();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismissProcessingDialog();
            updateBookingStatus();
        }, 2000);
    }

    private void showProcessingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý thanh toán qua " + selectedPaymentMethod + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProcessingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateBookingStatus() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Paid");
        updates.put("paymentMethod", selectedPaymentMethod);

        android.util.Log.d("PaymentMethod", "💳 Processing payment for booking: " + bookingId);

        db.collection("bookings").document(bookingId).update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                
                android.util.Log.d("PaymentMethod", "✅ Payment successful, sending notification...");

                sendPaymentNotificationToAdmins(bookingId, selectedPaymentMethod);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_method", selectedPaymentMethod);
                resultIntent.putExtra("payment_success", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendPaymentNotificationToAdmins(String bookingId, String paymentMethod) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        
        android.util.Log.d("PaymentMethod", "🔍 Fetching booking and user info...");

        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener(bookingDoc -> {
                if (!bookingDoc.exists()) return;
                
                com.example.homeserviceapp.models.Booking booking = 
                    bookingDoc.toObject(com.example.homeserviceapp.models.Booking.class);
                if (booking == null) return;
                
                String bookingCode = booking.getCode() != null ? booking.getCode() : bookingId;
                String serviceName = booking.getServiceName();

                db.collection("users").document(userId).get()
                    .addOnSuccessListener(userDoc -> {
                        String userName = userDoc.getString("fullName");
                        if (userName == null || userName.isEmpty()) {
                            userName = "Khách hàng";
                        }
                        
                        final String finalUserName = userName;
                        android.util.Log.d("PaymentMethod", "👤 User: " + finalUserName);

                        db.collection("users")
                            .whereEqualTo("userType", "admin")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                                    String adminId = doc.getId();
                                    
                                    String title = "Thanh toán từ " + finalUserName;
                                    String message = "Đơn hàng " + bookingCode + " - " + serviceName + " đã được thanh toán qua " + paymentMethod;
                                    
                                    android.util.Log.d("PaymentMethod", "📤 Sending to admin: " + adminId);
                                    
                                    com.example.homeserviceapp.models.Notification notification = 
                                        new com.example.homeserviceapp.models.Notification(
                                            adminId,
                                            title,
                                            message,
                                            "booking_paid",
                                            bookingCode
                                        );
                                    
                                    db.collection("notifications").add(notification)
                                        .addOnSuccessListener(ref -> {
                                            android.util.Log.d("PaymentMethod", "✅ Notification sent!");
                                        });
                                }
                            });
                    });
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProcessingDialog();
    }
}
