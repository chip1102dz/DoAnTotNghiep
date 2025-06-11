package com.example.doantotnghiep.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityQrpaymentBinding;
import com.example.doantotnghiep.helper.MoMoHelper;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;

public class QRPaymentActivity extends BaseActivity {

    private static final String TAG = "QRPaymentActivity";

    ActivityQrpaymentBinding binding;
    private ImageView imgQRCode;
    private TextView tvAmount, tvOrderInfo, tvStatus;
    private ProgressBar progressBar;

    private String currentOrderId = "";
    private String currentRequestId = "";
    private double paymentAmount = 0;

    private Handler paymentCheckHandler;
    private Runnable paymentCheckRunnable;
    private static final int MAX_PAYMENT_CHECK_ATTEMPTS = 120; // 10 minutes
    private int paymentCheckAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrpaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        loadDataFromIntent();

        paymentCheckHandler = new Handler();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText("Thanh toán QR MoMo");
    }

    private void initUi() {
        imgQRCode = binding.imgQrCode;
        tvAmount = binding.tvAmount;
        tvOrderInfo = binding.tvOrderInfo;
        tvStatus = binding.tvStatus;
        progressBar = binding.progressBar;
    }

    private void loadDataFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            paymentAmount = bundle.getDouble("amount", 0);
            String description = bundle.getString("description", "Nạp tiền");

            // Hiển thị thông tin
            tvAmount.setText(String.format("%,.0f đ", paymentAmount));
            tvOrderInfo.setText(description);
            tvStatus.setText("Đang tạo mã QR...");

            // Tạo QR payment
            createQRPayment();
        }
    }

    private void createQRPayment() {
        User currentUser = DataStoreManager.getUser();
        String description = "Nạp " + String.format("%,.0f", paymentAmount) + "đ vào tài khoản";

        MoMoHelper.createQRPayment(this, paymentAmount, description, currentUser.getEmail(),
                new MoMoHelper.MoMoListener() {
                    @Override
                    public void onCreateOrderSuccess(String payUrl, String orderId) {
                        // Không dùng cho QR
                    }

                    @Override
                    public void onCreateQRSuccess(String qrCodeData, String orderId) {
                        runOnUiThread(() -> {
                            currentOrderId = orderId;
                            currentRequestId = "REQ_" + System.currentTimeMillis();

                            // Generate và hiển thị QR code
                            Bitmap qrBitmap = MoMoHelper.generateQRCode(qrCodeData, 300, 300);
                            if (qrBitmap != null) {
                                imgQRCode.setImageBitmap(qrBitmap);
                                imgQRCode.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);

                                tvStatus.setText("Quét mã QR bằng app MoMo để thanh toán");

                                // Bắt đầu check payment status
                                startPaymentStatusCheck();
                            } else {
                                tvStatus.setText("Lỗi tạo mã QR");
                            }
                        });
                    }

                    @Override
                    public void onCreateOrderError(String error) {
                        runOnUiThread(() -> {
                            tvStatus.setText("Lỗi: " + error);
                            progressBar.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onPaymentResult(boolean success, String orderId, String message) {
                        runOnUiThread(() -> {
                            if (success) {
                                stopPaymentStatusCheck();
                                handlePaymentSuccess();
                            }
                            // Continue checking if not success
                        });
                    }
                });
    }

    private void startPaymentStatusCheck() {
        paymentCheckAttempts = 0;
        paymentCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (paymentCheckAttempts < MAX_PAYMENT_CHECK_ATTEMPTS) {
                    checkPaymentStatus();
                    paymentCheckAttempts++;
                    paymentCheckHandler.postDelayed(this, 5000); // Check every 5 seconds
                } else {
                    tvStatus.setText("Hết thời gian chờ. Vui lòng thử lại.");
                }
            }
        };

        paymentCheckHandler.post(paymentCheckRunnable);
    }

    private void stopPaymentStatusCheck() {
        if (paymentCheckHandler != null && paymentCheckRunnable != null) {
            paymentCheckHandler.removeCallbacks(paymentCheckRunnable);
        }
    }

    private void checkPaymentStatus() {
        MoMoHelper.queryPaymentStatus(currentOrderId, currentRequestId,
                new MoMoHelper.MoMoListener() {
                    @Override
                    public void onCreateOrderSuccess(String payUrl, String orderId) {
                        // Not used
                    }

                    @Override
                    public void onCreateQRSuccess(String qrCodeData, String orderId) {
                        // Not used
                    }

                    @Override
                    public void onCreateOrderError(String error) {
                        // Not used
                    }

                    @Override
                    public void onPaymentResult(boolean success, String orderId, String message) {
                        runOnUiThread(() -> {
                            if (success) {
                                stopPaymentStatusCheck();
                                handlePaymentSuccess();
                            } else {
                                tvStatus.setText("Đang chờ thanh toán... (" + (paymentCheckAttempts + 1) + "/" + MAX_PAYMENT_CHECK_ATTEMPTS + ")");
                            }
                        });
                    }
                });
    }

    private void handlePaymentSuccess() {
        tvStatus.setText("✅ Thanh toán thành công!");

        // Cập nhật số dư người dùng
        User currentUser = DataStoreManager.getUser();
        currentUser.addBalance(paymentAmount);
        DataStoreManager.setUser(currentUser);

        // Cập nhật Firebase (tương tự TopUpActivity)
        // updateBalanceInFirebase(currentUser.getBalance());

        showToastMessage("Nạp tiền thành công: " + String.format("%,.0f", paymentAmount) + "đ");

        // Delay rồi quay về
        paymentCheckHandler.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentStatusCheck();
    }
}