package com.example.doantotnghiep.activity.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.databinding.ActivityAdminAddVoucherBinding;
import com.example.doantotnghiep.helper.VoucherNotificationHelper;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AdminAddVoucherActivity extends BaseActivity {
    ActivityAdminAddVoucherBinding binding;
    private TextView tvToolbarTitle;
    private EditText edtDiscount, edtMinimum;
    private Button btnAddOrEdit;

    private boolean isUpdate;
    private Voucher mVoucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initUi();
        initView();
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mVoucher = (Voucher) bundleReceived.get(Constant.KEY_INTENT_VOUCHER_OBJECT);
        }
    }

    private void initUi() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        edtDiscount = binding.edtDiscount;
        edtMinimum = binding.edtMinimum;
        btnAddOrEdit = binding.btnAddOrEdit;

        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        btnAddOrEdit.setOnClickListener(v -> addOrEditVoucher());
    }

    private void initView() {
        if (isUpdate) {
            tvToolbarTitle.setText(getString(R.string.label_update_voucher));
            btnAddOrEdit.setText(getString(R.string.action_edit));

            edtDiscount.setText(String.valueOf(mVoucher.getDiscount()));
            edtMinimum.setText(String.valueOf(mVoucher.getMinimum()));
        } else {
            tvToolbarTitle.setText(getString(R.string.label_add_voucher));
            btnAddOrEdit.setText(getString(R.string.action_add));
        }
    }

    private void addOrEditVoucher() {
        String strDiscount = edtDiscount.getText().toString().trim();
        String strMinimum = edtMinimum.getText().toString().trim();

        if (StringUtil.isEmpty(strDiscount) || Integer.parseInt(strDiscount) <= 0) {
            Toast.makeText(this, getString(R.string.msg_discount_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strMinimum)) {
            strMinimum = "0";
        }

        if (isUpdate) {
            // Update voucher
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("discount", Integer.parseInt(strDiscount));
            map.put("minimum", Integer.parseInt(strMinimum));

            MyApplication.get(this).getVoucherDatabaseReference()
                    .child(String.valueOf(mVoucher.getId())).updateChildren(map, (error, ref) -> {
                        showProgressDialog(false);
                        Toast.makeText(this,
                                getString(R.string.msg_edit_voucher_success), Toast.LENGTH_SHORT).show();
                        GlobalFunction.hideSoftKeyboard(this);
                    });
        } else {
            // Add new voucher
            showProgressDialog(true);
            long voucherId = System.currentTimeMillis();
            Voucher voucher = new Voucher(voucherId, Integer.parseInt(strDiscount), Integer.parseInt(strMinimum));

            MyApplication.get(this).getVoucherDatabaseReference()
                    .child(String.valueOf(voucherId)).setValue(voucher, (error, ref) -> {
                        showProgressDialog(false);
                        if (error == null) {
                            // Gửi thông báo voucher mới cho tất cả users
                            VoucherNotificationHelper.broadcastNewVoucherNotification(this, voucher);

                            Toast.makeText(this, getString(R.string.msg_add_voucher_success), Toast.LENGTH_SHORT).show();
                            edtDiscount.setText("");
                            edtMinimum.setText("");
                            GlobalFunction.hideSoftKeyboard(this);
                        } else {
                            Toast.makeText(this, "Lỗi tạo voucher: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}