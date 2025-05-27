package com.example.doantotnghiep.activity.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.databinding.ActivityAdminAddRoleBinding;
import com.example.doantotnghiep.model.Admin;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AdminAddRoleActivity extends BaseActivity {

    ActivityAdminAddRoleBinding binding;
    private EditText edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddRoleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
    }

    private void initUi() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        tvToolbarTitle.setText(getString(R.string.label_add_role));
        edtEmail = binding.edtEmail;
        edtPassword = binding.edtPassword;
        Button btnAdd = binding.btnAdd;

        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        btnAdd.setOnClickListener(v -> addRole());
    }

    private void addRole() {
        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(getString(R.string.msg_email_admin_empty));
            return;
        }

        if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(getString(R.string.msg_password_admin_empty));
            return;
        }

        if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(getString(R.string.msg_email_invalid));
            return;
        }

        if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
            showToastMessage(getString(R.string.msg_email_invalid_admin));
            return;
        }

        // Add admin
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            long adminId = System.currentTimeMillis();
                            Admin admin = new Admin(adminId, user.getEmail());
                            MyApplication.get(this).getAdminDatabaseReference()
                                    .child(String.valueOf(adminId)).setValue(admin, (error, ref) -> {
                                        edtEmail.setText("");
                                        edtPassword.setText("");
                                        GlobalFunction.hideSoftKeyboard(this);
                                        showToastMessage(getString(R.string.msg_add_admin_success));
                                    });
                        }
                    } else {
                        showToastMessage(getString(R.string.msg_register_error));
                    }
                });
    }
}