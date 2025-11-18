package com.example.homeserviceapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class BookingCompletedDialog extends Dialog {

    private Button btnGoToHome;
    private Button btnViewBooking;
    private TextView tvBookingId;
    private ImageView iconSuccess;
    private View outerCircle;
    private OnDialogActionListener listener;

    public interface OnDialogActionListener {
        void onGoToHome();
        void onViewBooking();
    }

    public BookingCompletedDialog(@NonNull Context context) {
        super(context);
    }

    public BookingCompletedDialog(@NonNull Context context, OnDialogActionListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_booking_completed);

        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Make dialog not cancelable by touching outside
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        initViews();
        setupListeners();
        startAnimations();
    }

    private void initViews() {
        btnGoToHome = findViewById(R.id.btnGoToHome);
 //       btnViewBooking = findViewById(R.id.btnViewBooking);
        tvBookingId = findViewById(R.id.tvBookingId);
        iconSuccess = findViewById(R.id.iconSuccess);
        outerCircle = findViewById(R.id.outerCircle);

        // Generate random booking ID
        String bookingId = "#DV" + (100000 + (int)(Math.random() * 900000));
        tvBookingId.setText(bookingId);
    }

    private void setupListeners() {
        btnGoToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onGoToHome();
                }
            }
        });

//        btnViewBooking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//                if (listener != null) {
//                    listener.onViewBooking();
//                }
//            }
//        });


//        btnViewBooking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//                Intent intent = new Intent(v.getContext(), BookingFragment.class);
//                v.getContext().startActivity(intent);
//            }
//        });
    }

    private void startAnimations() {
        // Scale animation for icon (bounce effect)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconSuccess, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconSuccess, "scaleY", 0f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());

        AnimatorSet iconAnimSet = new AnimatorSet();
        iconAnimSet.playTogether(scaleX, scaleY);
        iconAnimSet.setStartDelay(200);
        iconAnimSet.start();

        // Rotation animation for icon
        ObjectAnimator rotate = ObjectAnimator.ofFloat(iconSuccess, "rotation", 0f, 360f);
        rotate.setDuration(800);
        rotate.setStartDelay(200);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.start();

        // Pulse animation for outer circle
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(outerCircle, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(outerCircle, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(outerCircle, "alpha", 0.2f, 0.5f, 0.2f);

        pulseX.setDuration(1500);
        pulseY.setDuration(1500);
        alpha.setDuration(1500);

        pulseX.setRepeatCount(ObjectAnimator.INFINITE);
        pulseY.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet pulseAnimSet = new AnimatorSet();
        pulseAnimSet.playTogether(pulseX, pulseY, alpha);
        pulseAnimSet.start();

        // Slide up animation for buttons
        ObjectAnimator slideUp1 = ObjectAnimator.ofFloat(btnGoToHome, "translationY", 200f, 0f);
        slideUp1.setDuration(500);
        slideUp1.setStartDelay(400);
        slideUp1.setInterpolator(new OvershootInterpolator());
        slideUp1.start();

        ObjectAnimator slideUp2 = ObjectAnimator.ofFloat(btnViewBooking, "translationY", 200f, 0f);
        slideUp2.setDuration(500);
        slideUp2.setStartDelay(500);
        slideUp2.setInterpolator(new OvershootInterpolator());
        slideUp2.start();

        // Fade in for buttons
        ObjectAnimator fadeIn1 = ObjectAnimator.ofFloat(btnGoToHome, "alpha", 0f, 1f);
        fadeIn1.setDuration(400);
        fadeIn1.setStartDelay(400);
        fadeIn1.start();

        ObjectAnimator fadeIn2 = ObjectAnimator.ofFloat(btnViewBooking, "alpha", 0f, 1f);
        fadeIn2.setDuration(400);
        fadeIn2.setStartDelay(500);
        fadeIn2.start();
    }

    public static void show(Context context, OnDialogActionListener listener) {
        BookingCompletedDialog dialog = new BookingCompletedDialog(context, listener);
        dialog.show();
    }
}