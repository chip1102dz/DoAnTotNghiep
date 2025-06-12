package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.admin.AdminMainActivity;
import com.example.doantotnghiep.databinding.ActivityLoginBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    ActivityLoginBinding binding;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private LinearLayout layoutRegister;
    private TextView tvForgotPassword;
    private boolean isEnableButtonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
        initListener();
    }

    private void initUi() {
        edtEmail = binding.edtEmail;
        edtPassword = binding.edtPassword;
        btnLogin = binding.btnLogin;
        layoutRegister = binding.layoutRegister;
        tvForgotPassword = binding.tvForgotPassword;
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
                    isEnableButtonLogin = true;
                    btnLogin.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonLogin = false;
                    btnLogin.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
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
                    isEnableButtonLogin = true;
                    btnLogin.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonLogin = false;
                    btnLogin.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        layoutRegister.setOnClickListener(
                v -> GlobalFunction.startActivity(this, RegisterActivity.class));

        btnLogin.setOnClickListener(v -> onClickValidateLogin());
        tvForgotPassword.setOnClickListener(
                v -> GlobalFunction.startActivity(this, ForgotPasswordActivity.class));
    }

    private void onClickValidateLogin() {
        if (!isEnableButtonLogin) return;

        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(getString(R.string.msg_password_require));
        } else if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(getString(R.string.msg_email_invalid));
        } else {
            loginUserFirebase(strEmail, strPassword);
        }
    }

    private void loginUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Tạo User object cơ bản
                            User userObject = new User(user.getEmail(), password);
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }

                            // Lưu user cơ bản trước
                            DataStoreManager.setUser(userObject);

                            // Sau đó đồng bộ dữ liệu từ Firebase
                            syncUserDataAfterLogin(userObject);
                        }
                    } else {
                        showProgressDialog(false);
                        showToastMessage(getString(R.string.msg_login_error));
                    }
                });
    }

    private void syncUserDataAfterLogin(User userObject) {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        MyApplication.get(this).getUserDatabaseReference(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "Syncing user data after login");

                            // Lấy dữ liệu từ Firebase
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            String dateOfBirth = snapshot.child("dateOfBirth").getValue(String.class);
                            String gender = snapshot.child("gender").getValue(String.class);
                            Double balance = snapshot.child("balance").getValue(Double.class);

                            // Cập nhật User object
                            if (fullName != null) userObject.setFullName(fullName);
                            if (phoneNumber != null) userObject.setPhoneNumber(phoneNumber);
                            if (address != null) userObject.setAddress(address);
                            if (profileImageUrl != null) userObject.setProfileImageUrl(profileImageUrl);
                            if (dateOfBirth != null) userObject.setDateOfBirth(dateOfBirth);
                            if (gender != null) userObject.setGender(gender);
                            if (balance != null) {
                                userObject.setBalance(balance);
                                Log.d(TAG, "User balance loaded: " + balance);
                            }

                            // Lưu lại vào local
                            DataStoreManager.setUser(userObject);
                        } else {
                            Log.d(TAG, "No existing user data in Firebase");
                        }

                        // Tiến hành chuyển activity
                        showProgressDialog(false);
                        goToMainActivity(userObject);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to sync user data after login: " + error.getMessage());
                        // Vẫn chuyển activity nếu có lỗi
                        showProgressDialog(false);
                        goToMainActivity(userObject);
                    }
                });
    }

    private void goToMainActivity(User userObject) {
        if (userObject.isAdmin()) {
            GlobalFunction.startActivity(this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(this, MainActivity.class);
        }
        finishAffinity();
    }
}