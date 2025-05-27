package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.admin.AdminMainActivity;
import com.example.doantotnghiep.databinding.ActivityRegisterBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {
    ActivityRegisterBinding binding;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnRegister;
    private LinearLayout layoutLogin;
    private boolean isEnableButtonRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
        initListener();
    }
    private void initUi() {
        edtEmail = binding.edtEmail;
        edtPassword = binding.edtPassword;
        btnRegister = binding.btnRegister;
        layoutLogin = binding.layoutLogin;
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

                String strPassword = edtPassword.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strPassword)) {
                    isEnableButtonRegister = true;
                    btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    edtPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_main);
                } else {
                    edtPassword.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray);
                }

                String strEmail = edtEmail.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strEmail)) {
                    isEnableButtonRegister = true;
                    btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        layoutLogin.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> onClickValidateRegister());
    }

    private void onClickValidateRegister() {
        if (!isEnableButtonRegister) return;

        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(getString(R.string.msg_password_require));
        } else if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(getString(R.string.msg_email_invalid));
        } else {
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(this, getString(R.string.msg_email_invalid_user), Toast.LENGTH_SHORT).show();
            } else {
                registerUserFirebase(strEmail, strPassword);
            }
        }
    }

    private void registerUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            User userObject = new User(user.getEmail(), password);
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }
                            DataStoreManager.setUser(userObject);
                            goToMainActivity();
                        }
                    } else {
                        showToastMessage(getString(R.string.msg_register_error));
                    }
                });
    }

    private void goToMainActivity() {
        if (DataStoreManager.getUser().isAdmin()) {
            GlobalFunction.startActivity(this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(this, MainActivity.class);
        }
        finishAffinity();
    }
}