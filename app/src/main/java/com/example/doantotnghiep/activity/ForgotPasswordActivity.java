package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityForgotPasswordBinding;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {
    ActivityForgotPasswordBinding binding;
    private EditText edtEmail;
    private Button btnResetPassword;
    private boolean isEnableButtonResetPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
    }
    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.reset_password));
    }
    private void initUi() {
        edtEmail = binding.edtEmail;
        btnResetPassword = binding.btnResetPassword;
    }
    private void initListener() {
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtEmail.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtEmail.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }

                if (!StringUtil.isEmpty(s.toString())) {
                    isEnableButtonResetPassword = true;
                    btnResetPassword.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonResetPassword = false;
                    btnResetPassword.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        btnResetPassword.setOnClickListener(v -> onClickValidateResetPassword());
    }

    private void onClickValidateResetPassword() {
        if (!isEnableButtonResetPassword) return;
        String strEmail = edtEmail.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            Toast.makeText(ForgotPasswordActivity.this,
                    getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show();
        } else if (!StringUtil.isValidEmail(strEmail)) {
            Toast.makeText(ForgotPasswordActivity.this,
                    getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show();
        } else {
            resetPassword(strEmail);
        }
    }

    private void resetPassword(String email) {
        showProgressDialog(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.msg_reset_password_successfully),
                                Toast.LENGTH_SHORT).show();
                        edtEmail.setText("");
                    }
                });
    }
}
