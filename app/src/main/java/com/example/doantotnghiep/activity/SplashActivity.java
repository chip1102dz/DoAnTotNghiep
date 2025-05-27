package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.os.Handler;


import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.admin.AdminMainActivity;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(this::goToActivity, 2000);
    }

    private void goToActivity() {
        if (DataStoreManager.getUser() != null
                && !StringUtil.isEmpty(DataStoreManager.getUser().getEmail())) {
            if (DataStoreManager.getUser().isAdmin()) {
                GlobalFunction.startActivity(this, AdminMainActivity.class);
            } else {
                GlobalFunction.startActivity(this, MainActivity.class);
            }
        } else {
            GlobalFunction.startActivity(this, LoginActivity.class);
        }
        finish();
    }
}