package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardMomo, cardVnpay, cardZalopay, cardViettelMoney;
    private RadioButton rbMomo, rbVnpay, rbZalopay, rbViettelMoney;
    private Button btnAddNewCard, btnPayNow;
    private TextView tvTotalAmount;

    private String selectedPaymentMethod = "VNPay";
    private double totalAmount = 150.000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

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
            totalAmount = intent.getDoubleExtra("total_amount", 150.00);
            selectedPaymentMethod = intent.getStringExtra("payment_method");
            if (selectedPaymentMethod == null) selectedPaymentMethod = "VNPay";
        }

        tvTotalAmount.setText(String.format("$%.2f", totalAmount));
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

        Toast.makeText(this, "Đang xử lý thanh toán qua " + selectedPaymentMethod + "...", Toast.LENGTH_SHORT).show();

        switch (selectedPaymentMethod) {
            case "MoMo": processMoMoPayment(); break;
            case "VNPay": processVNPayPayment(); break;
            case "ZaloPay": processZaloPayPayment(); break;
            case "Viettel Money": processViettelMoneyPayment(); break;
        }
    }

    private void processMoMoPayment() {
        Toast.makeText(this, "Thanh toán MoMo: $" + totalAmount, Toast.LENGTH_LONG).show();
        returnPaymentResult();
    }

    private void processVNPayPayment() {
        Toast.makeText(this, "Thanh toán VNPay: $" + totalAmount, Toast.LENGTH_LONG).show();
        returnPaymentResult();
    }

    private void processZaloPayPayment() {
        Toast.makeText(this, "Thanh toán ZaloPay: $" + totalAmount, Toast.LENGTH_LONG).show();
        returnPaymentResult();
    }

    private void processViettelMoneyPayment() {
        Toast.makeText(this, "Thanh toán Viettel Money: $" + totalAmount, Toast.LENGTH_LONG).show();
        returnPaymentResult();
    }

    private void returnPaymentResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_method", selectedPaymentMethod);
        resultIntent.putExtra("payment_success", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
