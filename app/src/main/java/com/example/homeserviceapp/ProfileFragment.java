package com.example.homeserviceapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private RelativeLayout menuMyProfile, menuNotification, menuFavourite;
    private RelativeLayout menuMyBooking, menuSecurity, menuSettings, menuLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        menuMyProfile = view.findViewById(R.id.menuMyProfile);
        menuNotification = view.findViewById(R.id.menuNotification);
        menuFavourite = view.findViewById(R.id.menuFavourite);
        menuMyBooking = view.findViewById(R.id.menuMyBooking);
        menuSecurity = view.findViewById(R.id.menuSecurity);
        menuSettings = view.findViewById(R.id.menuSettings);
        menuLogout = view.findViewById(R.id.menuLogout);


        menuLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void showLogoutDialog() {
        // Tạo Dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        Button btnNo = dialog.findViewById(R.id.btnNo);
        Button btnYes = dialog.findViewById(R.id.btnYes);

        btnNo.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }

    private void performLogout() {

        Toast.makeText(getContext(), "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Intent intent = new Intent(getActivity(), LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // getActivity().finish();
    }
}
