package com.example.doantotnghiep.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.VoucherAdapter;
import com.example.doantotnghiep.databinding.ActivityVoucherBinding;
import com.example.doantotnghiep.event.VoucherSelectedEvent;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.utils.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class VoucherActivity extends BaseActivity {
    ActivityVoucherBinding binding;
    private List<Voucher> listVoucher;
    private VoucherAdapter voucherAdapter;
    private int amount;
    private long voucherSelectedId;
    private ValueEventListener mValueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getDataIntent();
        initToolbar();
        initUi();
        getListVoucherFromFirebase();
    }
    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        voucherSelectedId = bundle.getLong(Constant.VOUCHER_ID, 0);
        amount = bundle.getInt(Constant.AMOUNT_VALUE, 0);
    }

    private void initUi() {
        RecyclerView rcvVoucher = binding.rcvVoucher;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvVoucher.setLayoutManager(linearLayoutManager);
        listVoucher = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(this, listVoucher, amount, this::handleClickVoucher);
        rcvVoucher.setAdapter(voucherAdapter);
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText(getString(R.string.title_voucher));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListVoucherFromFirebase() {
        showProgressDialog(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);

                resetListVoucher();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Voucher voucher = dataSnapshot.getValue(Voucher.class);
                    if (voucher != null) {
                        listVoucher.add(0, voucher);
                    }
                }

                if (voucherSelectedId > 0 && listVoucher != null && !listVoucher.isEmpty()) {
                    for (Voucher voucher : listVoucher) {
                        if (voucher.getId() == voucherSelectedId) {
                            voucher.setSelected(true);
                            break;
                        }
                    }
                }
                if (voucherAdapter != null) voucherAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).getVoucherDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    private void resetListVoucher() {
        if (listVoucher != null) {
            listVoucher.clear();
        } else {
            listVoucher = new ArrayList<>();
        }
    }

    private void handleClickVoucher(Voucher voucher) {
        EventBus.getDefault().post(new VoucherSelectedEvent(voucher));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voucherAdapter != null) voucherAdapter.release();
        if (mValueEventListener != null) {
            MyApplication.get(this).getVoucherDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
    }
}