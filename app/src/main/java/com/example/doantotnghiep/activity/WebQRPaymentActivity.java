package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import com.example.doantotnghiep.databinding.ActivityWebQrpaymentBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.helper.VietQRHelper;

public class WebQRPaymentActivity extends BaseActivity {

    private ActivityWebQrpaymentBinding binding;
    private WebView webView;
    private TextView tvAmount;
    private TextView tvStatus;
    private Button btnComplete;
    private Button btnCancel;

    private double paymentAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebQrpaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        loadDataFromIntent();
        setupWebView();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText("Thanh toán QR");
    }

    private void initUi() {
        webView = binding.webView;
        tvAmount = binding.tvAmount;
        tvStatus = binding.tvStatus;
        btnComplete = binding.btnComplete;
        btnCancel = binding.btnCancel;

        // Setup button listeners
        btnComplete.setOnClickListener(v -> handlePaymentComplete());
        btnCancel.setOnClickListener(v -> handlePaymentCancel());
    }

    private void loadDataFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            paymentAmount = bundle.getDouble("amount", 0);
            String description = bundle.getString("description", "Nạp tiền");

            tvAmount.setText(String.format("%,.0f đ", paymentAmount));
            tvStatus.setText("Vui lòng quét mã QR để thanh toán");
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("WebQRPayment", "Loading URL: " + url);

                // Kiểm tra nếu là URL callback thành công
                if (url.contains("success") || url.contains("completed")) {
                    handlePaymentComplete();
                    return true;
                }

                // Kiểm tra nếu là URL callback thất bại
                if (url.contains("cancel") || url.contains("failed")) {
                    handlePaymentCancel();
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebQRPayment", "Page finished loading: " + url);
                tvStatus.setText("Quét mã QR trên trang web để thanh toán");
            }
        });

        // Load QR payment URL
        loadQRPaymentUrl();
    }

    private void loadQRPaymentUrl() {
        User currentUser = DataStoreManager.getUser();
        if (currentUser == null) {
            showToastMessage("Lỗi: Không tìm thấy thông tin người dùng");
            return;
        }

        // Tạo URL thanh toán QR
        String qrUrl = buildQRPaymentUrl(paymentAmount, currentUser);

        Log.d("WebQRPayment", "Loading QR URL: " + qrUrl);
        tvStatus.setText("Đang tải trang thanh toán...");

        webView.loadUrl(qrUrl);
    }

    private String buildQRPaymentUrl(double amount, User user) {
        // Sử dụng VietQRHelper để tạo QR
        String description = "Nap tien ung dung";
        return VietQRHelper.generateVietQRUrl(amount, description, user.getEmail());
    }

    private void handlePaymentComplete() {
        Log.d("WebQRPayment", "Payment completed");

        // Cập nhật số dư người dùng
        User currentUser = DataStoreManager.getUser();
        if (currentUser != null) {
            currentUser.addBalance(paymentAmount);
            DataStoreManager.setUser(currentUser);
        }

        showToastMessage("✅ Thanh toán thành công!\nSố dư đã được cập nhật");

        // Trả kết quả về TopUpActivity
        setResult(RESULT_OK);
        finish();
    }

    private void handlePaymentCancel() {
        Log.d("WebQRPayment", "Payment cancelled");
        showToastMessage("Thanh toán bị hủy");

        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            handlePaymentCancel();
        }
    }
}