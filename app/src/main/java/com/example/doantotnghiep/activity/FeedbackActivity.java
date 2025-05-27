package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityFeedbackBinding;
import com.example.doantotnghiep.model.Feedback;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;

public class FeedbackActivity extends BaseActivity {
    ActivityFeedbackBinding binding;
    private EditText edtName, edtPhone, edtEmail, edtComment;
    private TextView tvSendFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initData();

    }
    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.feedback));
    }

    private void initUi() {
        edtName = binding.edtName;
        edtPhone = binding.edtPhone;
        edtEmail = binding.edtEmail;
        edtComment = binding.edtComment;
        tvSendFeedback = binding.tvSendFeedback;
    }

    private void initData() {
        edtEmail.setText(DataStoreManager.getUser().getEmail());
        tvSendFeedback.setOnClickListener(v -> onClickSendFeedback());
    }

    private void onClickSendFeedback() {
        String strName = edtName.getText().toString();
        String strPhone = edtPhone.getText().toString();
        String strEmail = edtEmail.getText().toString();
        String strComment = edtComment.getText().toString();

        if (StringUtil.isEmpty(strName)) {
            GlobalFunction.showToastMessage(this, getString(R.string.name_require));
        } else if (StringUtil.isEmpty(strComment)) {
            GlobalFunction.showToastMessage(this, getString(R.string.comment_require));
        } else {
            showProgressDialog(true);
            Feedback feedback = new Feedback(strName, strPhone, strEmail, strComment);
            MyApplication.get(this).getFeedbackDatabaseReference()
                    .child(String.valueOf(System.currentTimeMillis()))
                    .setValue(feedback, (databaseError, databaseReference) -> {
                        showProgressDialog(false);
                        sendFeedbackSuccess();
                    });
        }
    }

    public void sendFeedbackSuccess() {
        GlobalFunction.hideSoftKeyboard(this);
        GlobalFunction.showToastMessage(this, getString(R.string.msg_send_feedback_success));
        edtName.setText("");
        edtPhone.setText("");
        edtComment.setText("");
    }
}