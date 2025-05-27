package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityChangePasswordBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {
    ActivityChangePasswordBinding binding;
    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        initToolbar();
        initUi();
        initListener();

    }
    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.change_password));
    }

    private void initUi() {
        edtOldPassword = binding.edtNewPassword;
        edtNewPassword = binding.edtNewPassword;
        edtConfirmPassword = binding.edtConfirmPassword;
        btnChangePassword = binding.btnChangePassword;
    }

    private void initListener() {
        edtOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtOldPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtOldPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });
        edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtNewPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtNewPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtConfirmPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtConfirmPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }
            }
        });

        btnChangePassword.setOnClickListener(v -> onClickValidateChangePassword());
    }

    private void onClickValidateChangePassword() {
        String strOldPassword = edtOldPassword.getText().toString().trim();
        String strNewPassword = edtNewPassword.getText().toString().trim();
        String strConfirmPassword = edtConfirmPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strOldPassword)) {
            showToastMessage(getString(R.string.msg_old_password_require));
        } else if (StringUtil.isEmpty(strNewPassword)) {
            showToastMessage(getString(R.string.msg_new_password_require));
        } else if (StringUtil.isEmpty(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_confirm_password_require));
        } else if (!DataStoreManager.getUser().getPassword().equals(strOldPassword)) {
            showToastMessage(getString(R.string.msg_old_password_invalid));
        } else if (!strNewPassword.equals(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_confirm_password_invalid));
        } else if (strOldPassword.equals(strNewPassword)) {
            showToastMessage(getString(R.string.msg_new_password_invalid));
        } else {
            changePassword(strNewPassword);
        }
    }

    private void changePassword(String newPassword) {
        showProgressDialog(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        showToastMessage(getString(R.string.msg_change_password_successfully));
                        User userLogin = DataStoreManager.getUser();
                        userLogin.setPassword(newPassword);
                        DataStoreManager.setUser(userLogin);
                        edtOldPassword.setText("");
                        edtNewPassword.setText("");
                        edtConfirmPassword.setText("");
                    }
                });
    }
}