package com.example.doantotnghiep.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.TopUpAmountAdapter;
import com.example.doantotnghiep.databinding.ActivityTopUpBinding;
import com.example.doantotnghiep.helper.VietQRHelper;
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

    private static final String TAG = "TopUpActivity";

    private ActivityTopUpBinding binding;

    // UI Components
    private TextView tvCurrentBalance;
    private EditText edtCustomAmount;
    private RecyclerView rcvTopUpAmounts;
    private Button btnTopUp;

    // Data
    private TopUpAmountAdapter topUpAmountAdapter;
    private List<TopUpAmount> listTopUpAmounts;
    private double selectedAmount = 0;
    private User currentUser;

    // QR Payment Launcher
    private ActivityResultLauncher<Intent> qrPaymentLauncher;

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
        setupActivityResultLauncher();

        // Kiểm tra thông tin ngân hàng
        checkBankInfo();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText("Nạp tiền VietQR");
    }

    private void initUi() {
        tvCurrentBalance = binding.tvCurrentBalance;
        edtCustomAmount = binding.edtCustomAmount;
        rcvTopUpAmounts = binding.rcvTopUpAmounts;
        btnTopUp = binding.btnTopUp;
    }

    private void initListener() {
        btnTopUp.setOnClickListener(v -> processVietQRTopUp());

        edtCustomAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearPresetSelection();
                selectedAmount = 0;
            }
        });

        // Clear focus khi click ngoài EditText
        binding.getRoot().setOnClickListener(v -> {
            edtCustomAmount.clearFocus();
            GlobalFunction.hideSoftKeyboard(this);
        });
    }

    private void loadCurrentBalance() {
        currentUser = DataStoreManager.getUser();
        if (currentUser != null) {
            tvCurrentBalance.setText(currentUser.getFormattedBalance());
        } else {
            tvCurrentBalance.setText("0đ");
            showToastMessage("Lỗi tải thông tin người dùng");
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

    private void setupActivityResultLauncher() {
        qrPaymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // QR Payment thành công
                        handlePaymentSuccess();
                    } else {
                        // QR Payment bị hủy hoặc thất bại
                        showToastMessage("Thanh toán bị hủy");
                    }
                }
        );
    }

    private void checkBankInfo() {
        if (!VietQRHelper.isValidBankInfo()) {
            showBankInfoWarning();
        }
    }

    private void showBankInfoWarning() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Thông báo")
                .setMessage("Ứng dụng đang sử dụng thông tin ngân hàng mẫu.\n\n" +
                        "Để sử dụng thực tế, vui lòng:\n" +
                        "1. Cập nhật thông tin ngân hàng thật trong VietQRHelper\n" +
                        "2. Thay đổi BANK_CODE, ACCOUNT_NUMBER, ACCOUNT_NAME")
                .setPositiveButton("Đã hiểu", null)
                .setNegativeButton("Xem thông tin", (dialog, which) -> showCurrentBankInfo())
                .show();
    }

    private void showCurrentBankInfo() {
        String bankName = VietQRHelper.getBankName(VietQRHelper.BANK_CODE);
        new AlertDialog.Builder(this)
                .setTitle("Thông tin ngân hàng hiện tại")
                .setMessage("🏦 Ngân hàng: " + bankName + "\n" +
                        "📱 Mã ngân hàng: " + VietQRHelper.BANK_CODE + "\n" +
                        "💳 Số tài khoản: " + VietQRHelper.ACCOUNT_NUMBER + "\n" +
                        "👤 Chủ tài khoản: " + VietQRHelper.ACCOUNT_NAME)
                .setPositiveButton("OK", null)
                .show();
    }

    private void clearPresetSelection() {
        for (TopUpAmount amount : listTopUpAmounts) {
            amount.setSelected(false);
        }
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.notifyDataSetChanged();
        }
    }

    private void processVietQRTopUp() {
        double amountToTopUp = getSelectedAmount();

        // Validation
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

        // Hiển thị dialog xác nhận
        showPaymentConfirmDialog(amountToTopUp);
    }

    private void showPaymentConfirmDialog(double amount) {
        String bankName = VietQRHelper.getBankName(VietQRHelper.BANK_CODE);
        String message = String.format(
                "💰 Số tiền: %s\n" +
                        "🏦 Ngân hàng: %s\n" +
                        "💳 Số TK: %s\n" +
                        "👤 Chủ TK: %s\n\n" +
                        "Bạn sẽ được chuyển đến trang QR để thanh toán.",
                String.format("%,.0f", amount) + "đ",
                bankName,
                VietQRHelper.ACCOUNT_NUMBER,
                VietQRHelper.ACCOUNT_NAME
        );

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận nạp tiền")
                .setMessage(message)
                .setPositiveButton("Tiếp tục", (dialog, which) -> openQRPayment(amount))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openQRPayment(double amount) {
        try {
            Intent intent = new Intent(this, WebQRPaymentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble("amount", amount);
            bundle.putString("description", "Nạp tiền ứng dụng");
            intent.putExtras(bundle);

            qrPaymentLauncher.launch(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error opening QR payment: " + e.getMessage());
            showToastMessage("Lỗi mở trang thanh toán. Vui lòng thử lại.");
        }
    }

    private void handlePaymentSuccess() {
        double amount = getSelectedAmount();

        // Cập nhật số dư người dùng
        currentUser.addBalance(amount);
        DataStoreManager.setUser(currentUser);

        // Cập nhật Firebase
        updateBalanceInFirebase(currentUser.getBalance(), amount);

        // Hiển thị thông báo thành công
        showSuccessDialog(amount);

        // Cập nhật UI
        tvCurrentBalance.setText(currentUser.getFormattedBalance());

        // Reset selections
        resetSelections();
    }

    private void showSuccessDialog(double amount) {
        new AlertDialog.Builder(this)
                .setTitle("✅ Nạp tiền thành công!")
                .setMessage(String.format(
                        "Đã nạp: %s\n" +
                                "Số dư hiện tại: %s\n\n" +
                                "Cảm ơn bạn đã sử dụng dịch vụ!",
                        String.format("%,.0f", amount) + "đ",
                        currentUser.getFormattedBalance()
                ))
                .setPositiveButton("OK", null)
                .show();
    }

    private void resetSelections() {
        // Clear selections
        clearPresetSelection();
        edtCustomAmount.setText("");
        selectedAmount = 0;
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

    private void updateBalanceInFirebase(double newBalance, double topUpAmount) {
        if (currentUser == null) return;

        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        DatabaseReference userRef = MyApplication.get(this).getUserDatabaseReference(userKey);

        Map<String, Object> balanceUpdate = new HashMap<>();
        balanceUpdate.put("balance", newBalance);
        balanceUpdate.put("lastTopUpTime", System.currentTimeMillis());
        balanceUpdate.put("lastTopUpAmount", topUpAmount);
        balanceUpdate.put("email", currentUser.getEmail());

        // Thêm các thông tin user khác để đảm bảo tính toàn vẹn dữ liệu
        if (!StringUtil.isEmpty(currentUser.getFullName())) {
            balanceUpdate.put("fullName", currentUser.getFullName());
        }
        if (!StringUtil.isEmpty(currentUser.getPhoneNumber())) {
            balanceUpdate.put("phoneNumber", currentUser.getPhoneNumber());
        }
        if (!StringUtil.isEmpty(currentUser.getAddress())) {
            balanceUpdate.put("address", currentUser.getAddress());
        }
        if (!StringUtil.isEmpty(currentUser.getProfileImageUrl())) {
            balanceUpdate.put("profileImageUrl", currentUser.getProfileImageUrl());
        }
        if (!StringUtil.isEmpty(currentUser.getDateOfBirth())) {
            balanceUpdate.put("dateOfBirth", currentUser.getDateOfBirth());
        }
        if (!StringUtil.isEmpty(currentUser.getGender())) {
            balanceUpdate.put("gender", currentUser.getGender());
        }

        userRef.updateChildren(balanceUpdate)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Balance updated successfully in Firebase: " + newBalance);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update balance in Firebase: " + e.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh balance khi quay lại
        loadCurrentBalance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hide keyboard khi pause
        GlobalFunction.hideSoftKeyboard(this);
    }
}