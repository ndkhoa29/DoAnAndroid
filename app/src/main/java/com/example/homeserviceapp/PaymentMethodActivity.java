package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardMomo, cardVnpay, cardZalopay, cardViettelMoney;
    private RadioButton rbMomo, rbVnpay, rbZalopay, rbViettelMoney;
    private Button btnAddNewCard, btnPayNow;
    private TextView tvTotalAmount;

    private String selectedPaymentMethod = "VNPay";
    private double totalAmount = 150.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

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
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // MoMo
        cardMomo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod("MoMo", rbMomo);
            }
        });

        // VNPay
        cardVnpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod("VNPay", rbVnpay);
            }
        });

        // ZaloPay
        cardZalopay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod("ZaloPay", rbZalopay);
            }
        });

        // Viettel Money
        cardViettelMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod("Viettel Money", rbViettelMoney);
            }
        });

        // Add new card
        btnAddNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PaymentMethodActivity.this,
                        "Chức năng thêm thẻ mới", Toast.LENGTH_SHORT).show();
                // Navigate to add card screen
                // Intent intent = new Intent(PaymentMethodActivity.this, AddCardActivity.class);
                // startActivity(intent);
            }
        });

        // Pay Now button
        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void loadData() {
        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            totalAmount = intent.getDoubleExtra("total_amount", 150.00);
            selectedPaymentMethod = intent.getStringExtra("payment_method");

            if (selectedPaymentMethod == null) {
                selectedPaymentMethod = "VNPay";
            }
        }

        // Set total amount
        tvTotalAmount.setText(String.format("$%.2f", totalAmount));

        // Set default selected payment method
        setDefaultPaymentMethod();
    }

    private void setDefaultPaymentMethod() {
        switch (selectedPaymentMethod) {
            case "MoMo":
                selectPaymentMethod("MoMo", rbMomo);
                break;
            case "VNPay":
                selectPaymentMethod("VNPay", rbVnpay);
                break;
            case "ZaloPay":
                selectPaymentMethod("ZaloPay", rbZalopay);
                break;
            case "Viettel Money":
                selectPaymentMethod("Viettel Money", rbViettelMoney);
                break;
            default:
                selectPaymentMethod("VNPay", rbVnpay);
                break;
        }
    }

    private void selectPaymentMethod(String methodName, RadioButton selectedRadio) {
        // Uncheck all radio buttons
        rbMomo.setChecked(false);
        rbVnpay.setChecked(false);
        rbZalopay.setChecked(false);
        rbViettelMoney.setChecked(false);

        // Check selected radio button
        selectedRadio.setChecked(true);
        selectedPaymentMethod = methodName;

        Toast.makeText(this, "Đã chọn: " + methodName, Toast.LENGTH_SHORT).show();
    }

    private void processPayment() {
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Đang xử lý thanh toán qua " + selectedPaymentMethod + "...",
                Toast.LENGTH_SHORT).show();

        // Here you would integrate with payment gateway
        switch (selectedPaymentMethod) {
            case "MoMo":
                // Integrate MoMo SDK
                processMoMoPayment();
                break;
            case "VNPay":
                // Integrate VNPay SDK
                processVNPayPayment();
                break;
            case "ZaloPay":
                // Integrate ZaloPay SDK
                processZaloPayPayment();
                break;
            case "Viettel Money":
                // Integrate Viettel Money SDK
                processViettelMoneyPayment();
                break;
        }
    }

    private void processMoMoPayment() {
        // TODO: Implement MoMo payment integration
        // Use MoMo SDK here
        Toast.makeText(this, "Thanh toán MoMo: $" + totalAmount,
                Toast.LENGTH_LONG).show();

        // Return result to previous activity
        returnPaymentResult();
    }

    private void processVNPayPayment() {
        // TODO: Implement VNPay payment integration
        // Use VNPay SDK here
        Toast.makeText(this, "Thanh toán VNPay: $" + totalAmount,
                Toast.LENGTH_LONG).show();

        returnPaymentResult();
    }

    private void processZaloPayPayment() {
        // TODO: Implement ZaloPay payment integration
        // Use ZaloPay SDK here
        Toast.makeText(this, "Thanh toán ZaloPay: $" + totalAmount,
                Toast.LENGTH_LONG).show();

        returnPaymentResult();
    }

    private void processViettelMoneyPayment() {
        // TODO: Implement Viettel Money payment integration
        // Use Viettel Money SDK here
        Toast.makeText(this, "Thanh toán Viettel Money: $" + totalAmount,
                Toast.LENGTH_LONG).show();

        returnPaymentResult();
    }

    private void returnPaymentResult() {
        // Return selected payment method to previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_method", selectedPaymentMethod);
        resultIntent.putExtra("payment_success", true);
        setResult(RESULT_OK, resultIntent);

        // Navigate to payment success screen or return
        // Intent intent = new Intent(this, PaymentSuccessActivity.class);
        // intent.putExtra("payment_method", selectedPaymentMethod);
        // intent.putExtra("amount", totalAmount);
        // startActivity(intent);

        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}