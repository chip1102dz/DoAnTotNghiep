package com.example.doantotnghiep.activity.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.adapter.admin.AdminFeedbackAdapter;
import com.example.doantotnghiep.databinding.ActivityAdminFeedbackBinding;
import com.example.doantotnghiep.model.Feedback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class AdminFeedbackActivity extends BaseActivity {

    ActivityAdminFeedbackBinding binding;

    private List<Feedback> listFeedback;
    private AdminFeedbackAdapter adminFeedbackAdapter;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        loadListFeedbackFromFirebase();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        tvToolbarTitle.setText(getString(R.string.feedback));
    }

    private void initUi() {
        RecyclerView rcvData = binding.rcvData;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvData.setLayoutManager(linearLayoutManager);
        listFeedback = new ArrayList<>();
        adminFeedbackAdapter = new AdminFeedbackAdapter(listFeedback);
        rcvData.setAdapter(adminFeedbackAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadListFeedbackFromFirebase() {
        showProgressDialog(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                resetListFeedback();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Feedback feedback = dataSnapshot.getValue(Feedback.class);
                    if (feedback == null) return;
                    listFeedback.add(0, feedback);
                }
                if (adminFeedbackAdapter != null) adminFeedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).getFeedbackDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    private void resetListFeedback() {
        if (listFeedback != null) {
            listFeedback.clear();
        } else {
            listFeedback = new ArrayList<>();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getFeedbackDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
    }
}
