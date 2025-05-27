package com.example.doantotnghiep.activity.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.adapter.admin.AdminVoucherAdapter;
import com.example.doantotnghiep.databinding.ActivityAdminVoucherBinding;
import com.example.doantotnghiep.listener.IOnAdminManagerVoucherListener;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


import java.util.ArrayList;
import java.util.List;

public class AdminVoucherActivity extends BaseActivity {
    ActivityAdminVoucherBinding binding;
    private FloatingActionButton btnAdd;
    private List<Voucher> mListVoucher;
    private AdminVoucherAdapter mAdminVoucherAdapter;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initView();
        loadListVoucher();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText(getString(R.string.manage_voucher));
    }

    private void initUi() {
        btnAdd = binding.btnAdd;
        btnAdd.setOnClickListener(v -> onClickAddVoucher());
    }

    private void initView() {
        RecyclerView rcvData = findViewById(R.id.rcv_data);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvData.setLayoutManager(linearLayoutManager);
        mListVoucher = new ArrayList<>();
        mAdminVoucherAdapter = new AdminVoucherAdapter(mListVoucher, new IOnAdminManagerVoucherListener() {
            @Override
            public void onClickUpdateVoucher(Voucher voucher) {
                onClickEditVoucher(voucher);
            }

            @Override
            public void onClickDeleteVoucher(Voucher voucher) {
                deleteVoucherItem(voucher);
            }
        });
        rcvData.setAdapter(mAdminVoucherAdapter);
        rcvData.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    btnAdd.hide();
                } else {
                    btnAdd.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void onClickAddVoucher() {
        GlobalFunction.startActivity(this, AdminAddVoucherActivity.class);
    }

    private void onClickEditVoucher(Voucher voucher) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_VOUCHER_OBJECT, voucher);
        GlobalFunction.startActivity(this, AdminAddVoucherActivity.class, bundle);
    }

    private void deleteVoucherItem(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) ->
                        MyApplication.get(this).getVoucherDatabaseReference()
                        .child(String.valueOf(voucher.getId())).removeValue((error, ref) ->
                                Toast.makeText(this,
                                        getString(R.string.msg_delete_voucher_successfully),
                                        Toast.LENGTH_SHORT).show()))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    public void loadListVoucher() {
        mChildEventListener = new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Voucher voucher = dataSnapshot.getValue(Voucher.class);
                if (voucher == null || mListVoucher == null) return;
                mListVoucher.add(0, voucher);
                if (mAdminVoucherAdapter != null) mAdminVoucherAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Voucher voucher = dataSnapshot.getValue(Voucher.class);
                if (voucher == null || mListVoucher == null || mListVoucher.isEmpty()) return;
                for (int i = 0; i < mListVoucher.size(); i++) {
                    if (voucher.getId() == mListVoucher.get(i).getId()) {
                        mListVoucher.set(i, voucher);
                        break;
                    }
                }
                if (mAdminVoucherAdapter != null) mAdminVoucherAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Voucher voucher = dataSnapshot.getValue(Voucher.class);
                if (voucher == null || mListVoucher == null || mListVoucher.isEmpty()) return;
                for (Voucher voucherObject : mListVoucher) {
                    if (voucher.getId() == voucherObject.getId()) {
                        mListVoucher.remove(voucherObject);
                        break;
                    }
                }
                if (mAdminVoucherAdapter != null) mAdminVoucherAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        MyApplication.get(this).getVoucherDatabaseReference().addChildEventListener(mChildEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChildEventListener != null) {
            MyApplication.get(this).getVoucherDatabaseReference().addChildEventListener(mChildEventListener);
        }
    }
}
