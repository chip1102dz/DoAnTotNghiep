package com.example.doantotnghiep.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.PaymentMethodAdapter;
import com.example.doantotnghiep.databinding.ActivityPaymentMethodBinding;
import com.example.doantotnghiep.event.PaymentMethodSelectedEvent;
import com.example.doantotnghiep.model.PaymentMethod;
import com.example.doantotnghiep.utils.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodActivity extends BaseActivity {
    ActivityPaymentMethodBinding binding;
    private List<PaymentMethod> listPaymentMethod;
    private PaymentMethodAdapter paymentMethodAdapter;
    private int paymentMethodSelectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentMethodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDataIntent();
        initToolbar();
        initUi();
        getListPaymentMethodFromFirebase();
    }
    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        paymentMethodSelectedId = bundle.getInt(Constant.PAYMENT_METHOD_ID, 0);
    }
    private void initUi() {
        RecyclerView rcvPaymentMethod = findViewById(R.id.rcv_payment_method);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvPaymentMethod.setLayoutManager(linearLayoutManager);
        listPaymentMethod = new ArrayList<>();
        paymentMethodAdapter = new PaymentMethodAdapter(listPaymentMethod,
                this::handleClickPaymentMethod);
        rcvPaymentMethod.setAdapter(paymentMethodAdapter);
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText(getString(R.string.title_payment_method));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListPaymentMethodFromFirebase() {
        resetListPaymentMethod();
        listPaymentMethod.add(new PaymentMethod(1, getString(R.string.title_payment_method_cash), getString(R.string.title_payment_method_cash_desc)));
        listPaymentMethod.add(new PaymentMethod(2, getString(R.string.title_payment_method_zalopay), getString(R.string.title_payment_method_zalopay_desc)));

        if (paymentMethodSelectedId > 0 && listPaymentMethod != null) {
            for (PaymentMethod paymentMethod : listPaymentMethod) {
                if (paymentMethod.getId() == paymentMethodSelectedId) {
                    paymentMethod.setSelected(true);
                    break;
                }
            }
        }
        if (paymentMethodAdapter != null) paymentMethodAdapter.notifyDataSetChanged();
    }

    private void resetListPaymentMethod() {
        if (listPaymentMethod != null) {
            listPaymentMethod.clear();
        } else {
            listPaymentMethod = new ArrayList<>();
        }
    }

    private void handleClickPaymentMethod(PaymentMethod paymentMethod) {
        EventBus.getDefault().post(new PaymentMethodSelectedEvent(paymentMethod));
        finish();
    }
}