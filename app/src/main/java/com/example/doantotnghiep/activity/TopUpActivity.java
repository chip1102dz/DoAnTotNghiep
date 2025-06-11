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

    // MoMo Payment Variables
    private String currentOrderId = "";
    private String currentRequestId = "";
    private Handler paymentCheckHandler;
    private Runnable paymentCheckRunnable;
    private static final int MAX_PAYMENT_CHECK_ATTEMPTS = 60; // 5 minutes
    private int paymentCheckAttempts = 0;
    private boolean isPaymentInProgress = false;

    // Activity Result Launcher for QR Payment
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

        paymentCheckHandler = new Handler();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> {
            if (isPaymentInProgress) {
                showExitPaymentDialog();
            } else {
                finish();
            }
        });
        tvToolbarTitle.setText("Nạp tiền MoMo");
    }

    private void initUi() {
        tvCurrentBalance = binding.tvCurrentBalance;
        edtCustomAmount = binding.edtCustomAmount;
        rcvTopUpAmounts = binding.rcvTopUpAmounts;
        btnTopUp = binding.btnTopUp;
    }

    private void initListener() {
        btnTopUp.setOnClickListener(v -> {
            if (!isPaymentInProgress) {
                processMoMoTopUp();
            } else {
                showToastMessage("Đang có giao dịch đang thực hiện...");
            }
        });

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
                    isPaymentInProgress = false;
                    updateUIState();

                    if (result.getResultCode() == RESULT_OK) {
                        // QR Payment thành công
                        handlePaymentSuccess();
                    } else {
                        // QR Payment bị hủy hoặc thất bại
                        showToastMessage("Thanh toán bị hủy");
                        resetPaymentState();
                    }
                }
        );
    }

    private void clearPresetSelection() {
        for (TopUpAmount amount : listTopUpAmounts) {
            amount.setSelected(false);
        }
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.notifyDataSetChanged();
        }
    }

    private void processMoMoTopUp() {
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

        // Chuyển thẳng sang QR Payment (bỏ dialog chọn phương thức)
        openQRPayment(amountToTopUp);
    }

    private void showPaymentMethodDialog(double amount) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn phương thức thanh toán")
                .setMessage("Số tiền: " + String.format("%,.0f", amount) + "đ")
                .setItems(new String[]{
                        "📱 Mở app MoMo",
                        "📷 Quét QR Code"
                }, (dialog, which) -> {
                    if (which == 0) {
                        // Mở app MoMo
                        createMoMoAppPayment(amount);
                    } else {
                        // Hiển thị QR Code
                        openQRPayment(amount);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createMoMoAppPayment(double amount) {
        isPaymentInProgress = true;
        updateUIState();
        showProgressDialog(true);

        String description = "Nạp " + String.format("%,.0f", amount) + "đ vào tài khoản " + currentUser.getEmail();

        MoMoHelper.createAppPayment(this, amount, description, currentUser.getEmail(),
                new MoMoHelper.MoMoListener() {
                    @Override
                    public void onCreateOrderSuccess(String payUrl, String orderId) {
                        runOnUiThread(() -> {
                            showProgressDialog(false);
                            currentOrderId = orderId;
                            currentRequestId = "REQ_" + System.currentTimeMillis();

                            // Log để debug
                            Log.d("TopUpActivity", "PayUrl: " + payUrl);
                            Log.d("TopUpActivity", "OrderId: " + orderId);

                            // Kiểm tra nếu payUrl hợp lệ
                            if (payUrl != null && !payUrl.isEmpty()) {
                                // Mở MoMo app để thanh toán
                                MoMoHelper.openMoMoApp(TopUpActivity.this, payUrl);

                                // Bắt đầu kiểm tra trạng thái thanh toán
                                startPaymentStatusCheck();

                                showToastMessage("Đang chuyển đến MoMo...");
                            } else {
                                // Fallback nếu không có payUrl
                                isPaymentInProgress = false;
                                updateUIState();
                                showToastMessage("Không thể mở MoMo. Vui lòng thử phương thức QR.");
                            }
                        });
                    }

                    @Override
                    public void onCreateQRSuccess(String qrCodeData, String orderId) {
                        // Không sử dụng cho app payment
                    }

                    @Override
                    public void onCreateOrderError(String error) {
                        runOnUiThread(() -> {
                            showProgressDialog(false);
                            isPaymentInProgress = false;
                            updateUIState();

                            // Log lỗi để debug
                            Log.e("TopUpActivity", "MoMo Error: " + error);

                            // Hiển thị lỗi chi tiết hơn
                            showToastMessage("Lỗi tạo đơn hàng MoMo: " + error);

                            // Suggest alternative
                            new AlertDialog.Builder(TopUpActivity.this)
                                    .setTitle("Lỗi thanh toán")
                                    .setMessage("Không thể kết nối MoMo: " + error + "\n\nBạn có muốn thử phương thức QR không?")
                                    .setPositiveButton("Thử QR", (dialog, which) -> openQRPayment(amount))
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                    }

                    @Override
                    public void onPaymentResult(boolean success, String orderId, String message) {
                        runOnUiThread(() -> {
                            if (success) {
                                stopPaymentStatusCheck();
                                handlePaymentSuccess();
                            } else {
                                // Log để debug
                                Log.d("TopUpActivity", "Payment check result: " + message);

                                // Tiếp tục check nếu chưa hết thời gian
                                if (paymentCheckAttempts >= MAX_PAYMENT_CHECK_ATTEMPTS) {
                                    stopPaymentStatusCheck();
                                    isPaymentInProgress = false;
                                    updateUIState();
                                    showToastMessage("Hết thời gian chờ thanh toán: " + message);
                                    resetPaymentState();
                                }
                            }
                        });
                    }
                });
    }

    private void openQRPayment(double amount) {
        Intent intent = new Intent(this, QRPaymentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("amount", amount);
        bundle.putString("description", "Nạp " + String.format("%,.0f", amount) + "đ vào tài khoản");
        intent.putExtras(bundle);

        isPaymentInProgress = true;
        updateUIState();
        qrPaymentLauncher.launch(intent);
    }

    private void startPaymentStatusCheck() {
        paymentCheckAttempts = 0;
        paymentCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (paymentCheckAttempts < MAX_PAYMENT_CHECK_ATTEMPTS && isPaymentInProgress) {
                    checkPaymentStatus();
                    paymentCheckAttempts++;
                    paymentCheckHandler.postDelayed(this, 5000); // Check every 5 seconds
                } else {
                    runOnUiThread(() -> {
                        isPaymentInProgress = false;
                        updateUIState();
                        if (paymentCheckAttempts >= MAX_PAYMENT_CHECK_ATTEMPTS) {
                            showToastMessage("Hết thời gian kiểm tra thanh toán. Vui lòng kiểm tra lại sau.");
                        }
                        resetPaymentState();
                    });
                }
            }
        };

        paymentCheckHandler.post(paymentCheckRunnable);
        showToastMessage("Đang kiểm tra trạng thái thanh toán...");
    }

    private void stopPaymentStatusCheck() {
        if (paymentCheckHandler != null && paymentCheckRunnable != null) {
            paymentCheckHandler.removeCallbacks(paymentCheckRunnable);
        }
    }

    private void checkPaymentStatus() {
        if (!StringUtil.isEmpty(currentOrderId) && !StringUtil.isEmpty(currentRequestId)) {
            // Log để debug
            Log.d("TopUpActivity", "Checking payment status for order: " + currentOrderId);

            MoMoHelper.queryPaymentStatus(currentOrderId, currentRequestId,
                    new MoMoHelper.MoMoListener() {
                        @Override
                        public void onCreateOrderSuccess(String payUrl, String orderId) {
                            // Not used in query
                        }

                        @Override
                        public void onCreateQRSuccess(String qrCodeData, String orderId) {
                            // Not used in query
                        }

                        @Override
                        public void onCreateOrderError(String error) {
                            // Not used in query - có thể log để debug
                            Log.d("TopUpActivity", "Query error (ignored): " + error);
                        }

                        @Override
                        public void onPaymentResult(boolean success, String orderId, String message) {
                            runOnUiThread(() -> {
                                Log.d("TopUpActivity", "Payment status: success=" + success + ", message=" + message);

                                if (success) {
                                    stopPaymentStatusCheck();
                                    handlePaymentSuccess();
                                }
                                // Nếu không success, tiếp tục check trong loop
                            });
                        }
                    });
        } else {
            Log.e("TopUpActivity", "Missing orderId or requestId for payment check");
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
        showToastMessage("✅ Nạp tiền thành công!\nSố dư: " + currentUser.getFormattedBalance());

        // Cập nhật UI
        tvCurrentBalance.setText(currentUser.getFormattedBalance());

        // Reset state
        resetPaymentState();
        isPaymentInProgress = false;
        updateUIState();
    }

    private void resetPaymentState() {
        // Clear selections
        clearPresetSelection();
        edtCustomAmount.setText("");
        selectedAmount = 0;
        currentOrderId = "";
        currentRequestId = "";
        paymentCheckAttempts = 0;
    }

    private void updateUIState() {
        // Disable/Enable UI based on payment progress
        btnTopUp.setEnabled(!isPaymentInProgress);
        btnTopUp.setText(isPaymentInProgress ? "Đang xử lý..." : "Nạp tiền");
        edtCustomAmount.setEnabled(!isPaymentInProgress);
        rcvTopUpAmounts.setEnabled(!isPaymentInProgress);

        // Update adapter state
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.setEnabled(!isPaymentInProgress);
        }
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
        DatabaseReference userRef = MyApplication.get(this).getAdminDatabaseReference()
                .getParent().child("users").child(userKey);

        Map<String, Object> balanceUpdate = new HashMap<>();
        balanceUpdate.put("balance", newBalance);
        balanceUpdate.put("lastTopUpTime", System.currentTimeMillis());
        balanceUpdate.put("lastTopUpAmount", topUpAmount);
        balanceUpdate.put("email", currentUser.getEmail());

        userRef.updateChildren(balanceUpdate)
                .addOnSuccessListener(aVoid -> {
                    // Success - no need to show message
                })
                .addOnFailureListener(e -> {
                    showToastMessage("Lỗi khi cập nhật số dư: " + e.getMessage());
                });
    }

    private void showExitPaymentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát thanh toán")
                .setMessage("Bạn có giao dịch đang thực hiện. Bạn có chắc chắn muốn thoát không?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    stopPaymentStatusCheck();
                    isPaymentInProgress = false;
                    resetPaymentState();
                    finish();
                })
                .setNegativeButton("Tiếp tục", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentStatusCheck();
        if (topUpAmountAdapter != null) {
            topUpAmountAdapter.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra lại trạng thái thanh toán khi user quay lại app
        if (!StringUtil.isEmpty(currentOrderId) && isPaymentInProgress) {
            checkPaymentStatus();
        }

        // Refresh balance
        loadCurrentBalance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hide keyboard khi pause
        GlobalFunction.hideSoftKeyboard(this);
    }

    @Override
    public void onBackPressed() {
        if (isPaymentInProgress) {
            showExitPaymentDialog();
        } else {
            super.onBackPressed();
        }
    }
}