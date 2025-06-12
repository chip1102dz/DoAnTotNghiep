package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.admin.AdminMainActivity;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.example.doantotnghiep.MyApplication;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(this::goToActivity, 2000);
    }

    private void goToActivity() {
        User user = DataStoreManager.getUser();
        if (user != null && !StringUtil.isEmpty(user.getEmail())) {
            // Đồng bộ dữ liệu từ Firebase trước khi chuyển activity
            syncUserDataBeforeNavigation(user);
        } else {
            GlobalFunction.startActivity(this, LoginActivity.class);
            finish();
        }
    }

    private void syncUserDataBeforeNavigation(User user) {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        MyApplication.get(this).getUserDatabaseReference(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "Syncing user data from Firebase");

                            // Cập nhật số dư và thông tin khác
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            String dateOfBirth = snapshot.child("dateOfBirth").getValue(String.class);
                            String gender = snapshot.child("gender").getValue(String.class);
                            Double balance = snapshot.child("balance").getValue(Double.class);

                            // Cập nhật User object
                            if (fullName != null) user.setFullName(fullName);
                            if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
                            if (address != null) user.setAddress(address);
                            if (profileImageUrl != null) user.setProfileImageUrl(profileImageUrl);
                            if (dateOfBirth != null) user.setDateOfBirth(dateOfBirth);
                            if (gender != null) user.setGender(gender);
                            if (balance != null) {
                                user.setBalance(balance);
                                Log.d(TAG, "Updated balance: " + balance);
                            }

                            // Lưu lại vào local
                            DataStoreManager.setUser(user);
                        } else {
                            Log.d(TAG, "No user data found in Firebase");
                        }

                        // Chuyển activity
                        navigateToMainActivity(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to sync user data: " + error.getMessage());
                        // Vẫn chuyển activity nếu có lỗi
                        navigateToMainActivity(user);
                    }
                });
    }

    private void navigateToMainActivity(User user) {
        if (user.isAdmin()) {
            GlobalFunction.startActivity(this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(this, MainActivity.class);
        }
        finish();
    }
}