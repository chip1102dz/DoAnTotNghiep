package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.TopUpAmountAdapter;
import com.example.doantotnghiep.databinding.ActivityTopUpBinding;
import com.example.doantotnghiep.model.TopUpAmount;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopUpActivity extends BaseActivity {

    private ActivityTopUpBinding binding;
    private TextView tvCurrentBalance;
    private EditText edtCustomAmount;
    private RecyclerView rcvTopUpAmounts;
    private Button btnTopUp;

    private TopUpAmountAdapter topUpAmountAdapter;
    private List<TopUpAmount> listTopUpAmounts;
    private double selectedAmount = 0;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        loadCurrentBalance();
        setupTopUpAmounts();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText("Nạp tiền");
    }

    private void initUi() {
        tvCurrentBalance = binding.tvCurrentBalance;
        edtCustomAmount = binding.edtCustomAmount;
        rcvTopUpAmounts = binding.rcvTopUpAmounts;
        btnTopUp = binding.btnTopUp;
    }

    private void initListener() {
        btnTopUp.setOnClickListener(v -> processTopUp());

        edtCustomAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Clear selection from preset amounts
                clearPresetSelection();
                selectedAmount = 0;
            }
        });
    }

    private void loadCurrentBalance() {
        currentUser = DataStoreManager.getUser();
        if (currentUser != null) {
            tvCurrentBalance.setText(currentUser.getFormattedBalance());
        }
    }

    private void setupTopUpAmounts() {
        listTopUpAmounts = new ArrayList<>();
        listTopUpAmounts.add(new TopUpAmount(50000, "50.000đ"));
        listTopUpAmounts.add(new TopUpAmount(100000, "100.000đ"));
        listTopUpAmounts.add(new TopUpAmount(200000, "200.000đ"));
        listTopUpAmounts.add(new TopUpAmount(500000, "500.000đ"));
        listTopUpAmounts.add(new TopUpAmount(1000000, "1.000.000đ"));
        listTopUpAmounts.add(new TopUpAmount(2000000, "2.000.000đ"));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rcvTopUpAmounts.setLayoutManager(gridLayoutManager);

        topUpAmountAdapter = new TopUpAmountAdapter(listTopUpAmounts, new TopUpAmountAdapter.IClickTopUpAmountListener() {
            @Override
            public void onClickTopUpAmount(TopUpAmount topUpAmount) {
                selectedAmount = topUpAmount.getAmount();
                edtCustomAmount.setText("");
                edtCustomAmount.clearFocus();
                GlobalFunction.hideSoftKeyboard(TopUpActivity.this);
            }
        });
        rcvTopUpAmounts.setAdapter(topUpAmountAdapter);
    }

    private void clearPresetSelection() {
        for (TopUpAmount amount : listTopUpAmounts) {
            amount.setSelected(false);
        }
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.notifyDataSetChanged();
        }
    }

    private void processTopUp() {
        double amountToTopUp = getSelectedAmount();

        if (amountToTopUp <= 0) {
            showToastMessage("Vui lòng chọn hoặc nhập số tiền cần nạp");
            return;
        }

        if (amountToTopUp < 10000) {
            showToastMessage("Số tiền nạp tối thiểu là 10.000đ");
            return;
        }

        if (amountToTopUp > 10000000) {
            showToastMessage("Số tiền nạp tối đa là 10.000.000đ");
            return;
        }

        // Simulate payment process
        showProgressDialog(true);

        // In real app, integrate with payment gateway (ZaloPay, VNPay, etc.)
        simulatePaymentProcess(amountToTopUp);
    }

    private double getSelectedAmount() {
        if (selectedAmount > 0) {
            return selectedAmount;
        }

        String customAmountStr = edtCustomAmount.getText().toString().trim();
        if (!StringUtil.isEmpty(customAmountStr)) {
            try {
                return Double.parseDouble(customAmountStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    private void simulatePaymentProcess(double amount) {
        // Simulate network delay
        binding.getRoot().postDelayed(() -> {
            showProgressDialog(false);

            // Update user balance
            currentUser.addBalance(amount);
            DataStoreManager.setUser(currentUser);

            // Update Firebase
            updateBalanceInFirebase(currentUser.getBalance());

            // Show success message
            showToastMessage("Nạp tiền thành công! Số dư: " + currentUser.getFormattedBalance());

            // Update UI
            tvCurrentBalance.setText(currentUser.getFormattedBalance());

            // Clear selections
            clearPresetSelection();
            edtCustomAmount.setText("");
            selectedAmount = 0;

        }, 2000); // Simulate 2 second processing time
    }

    private void updateBalanceInFirebase(double newBalance) {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        DatabaseReference userRef = MyApplication.get(this).getAdminDatabaseReference()
                .getParent().child("users").child(userKey);

        Map<String, Object> balanceUpdate = new HashMap<>();
        balanceUpdate.put("balance", newBalance);

        userRef.updateChildren(balanceUpdate)
                .addOnFailureListener(e -> {
                    showToastMessage("Lỗi khi cập nhật số dư: " + e.getMessage());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.release();
        }
    }
}