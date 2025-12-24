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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homeserviceapp.helpers.UserPreferences;
import com.example.homeserviceapp.helpers.CloudinaryHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {

    private RelativeLayout menuMyProfile, menuNotification, menuFavourite;
    private RelativeLayout menuMyBooking, menuSecurity, menuSettings, menuLogout, menuSupport;
    
    private de.hdodenhof.circleimageview.CircleImageView ivProfileAvatar;
    private TextView tvProfileName, tvProfileEmail;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        menuMyProfile = view.findViewById(R.id.menuMyProfile);
        menuNotification = view.findViewById(R.id.menuNotification);
        menuFavourite = view.findViewById(R.id.menuFavourite);
        menuMyBooking = view.findViewById(R.id.menuMyBooking);
        menuSecurity = view.findViewById(R.id.menuSecurity);
        menuSettings = view.findViewById(R.id.menuSettings);
        menuLogout = view.findViewById(R.id.menuLogout);

        loadUserProfile();

        menuNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThongBaoActivity.class);
            startActivity(intent);
        });

        menuMyBooking.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToBooking();
            }
        });

        menuMyProfile = view.findViewById(R.id.menuMyProfile);

        menuMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HoSoActivity.class);
            startActivity(intent);
        });

        menuLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserProfile() {
        UserPreferences userPrefs = new UserPreferences(requireContext());
            
        if (userPrefs.hasCachedData()) {
            String cachedName = userPrefs.getUserName();
            String cachedEmail = userPrefs.getUserEmail();
            String cachedAvatar = userPrefs.getUserAvatar();
            
            if (!cachedName.isEmpty()) tvProfileName.setText(cachedName);
            if (!cachedEmail.isEmpty()) tvProfileEmail.setText(cachedEmail);
            
            if (!cachedAvatar.isEmpty()) {
                String optimizedUrl = cachedAvatar;
                if (cachedAvatar.contains("cloudinary.com")) {
                    optimizedUrl = CloudinaryHelper.getThumbnailUrl(cachedAvatar);
                }
                Glide.with(this)
                    .load(optimizedUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.avatar_admin)
                    .into(ivProfileAvatar);
            }
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String avatarUrl = documentSnapshot.getString("avatarUrl");
                        
                        userPrefs.saveUserData(fullName, email, phone, avatarUrl);

                        if (fullName != null && !fullName.isEmpty()) {
                            tvProfileName.setText(fullName);
                        }

                        if (email != null && !email.isEmpty()) {
                            tvProfileEmail.setText(email);
                        }

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            String optimizedUrl = avatarUrl;
                            if (avatarUrl.contains("cloudinary.com")) {
                                optimizedUrl = CloudinaryHelper.getThumbnailUrl(avatarUrl);
                            }
                            
                            Glide.with(this)
                                .load(optimizedUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .skipMemoryCache(false)
                                .placeholder(R.drawable.avatar_admin)
                                .error(R.drawable.avatar_admin)
                                .into(ivProfileAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
        }
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performLogout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("isOnline", false)
                    .addOnCompleteListener(task -> {
                        auth.signOut();
                        navigateToLogin();
                    });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Toast.makeText(getContext(), "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
