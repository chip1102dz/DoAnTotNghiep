package com.example.doantotnghiep.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doantotnghiep.activity.LoginActivity;
import com.example.doantotnghiep.activity.admin.AdminFeedbackActivity;
import com.example.doantotnghiep.activity.admin.AdminRevenueActivity;
import com.example.doantotnghiep.activity.admin.AdminRoleActivity;
import com.example.doantotnghiep.activity.admin.AdminTopProductActivity;
import com.example.doantotnghiep.activity.admin.AdminVoucherActivity;
import com.example.doantotnghiep.databinding.FragmentAdminSettingsBinding;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.auth.FirebaseAuth;

public class AdminSettingsFragment extends Fragment {

    FragmentAdminSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminSettingsBinding.inflate(inflater, container, false);

        setupScreen();

        return binding.getRoot();
    }

    private void setupScreen() {
        TextView tvEmail = binding.tvEmail;
        tvEmail.setText(DataStoreManager.getUser().getEmail());
        TextView tvManageRole = binding.tvManageRole;
        if (Constant.MAIN_ADMIN.equals(DataStoreManager.getUser().getEmail())) {
            tvManageRole.setVisibility(View.VISIBLE);
        } else {
            tvManageRole.setVisibility(View.GONE);
        }

        tvManageRole.setOnClickListener(view -> onClickManageRole());
        binding.tvManageFeedback.setOnClickListener(view -> onClickManageRevenue());
        binding.tvManageTopProduct.setOnClickListener(view -> onClickManageTopProduct());
        binding.tvManageVoucher.setOnClickListener(view -> onClickManageVoucher());
        binding.tvManageFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdminSettingsFragment.this.onClickManageFeedback();
            }
        });
        binding.tvSignOut.setOnClickListener(view -> onClickSignOut());
    }

    private void onClickManageRole() {
        GlobalFunction.startActivity(getActivity(), AdminRoleActivity.class);
    }

    private void onClickManageRevenue() {
        GlobalFunction.startActivity(getActivity(), AdminRevenueActivity.class);
    }

    private void onClickManageTopProduct() {
        GlobalFunction.startActivity(getActivity(), AdminTopProductActivity.class);
    }

    private void onClickManageVoucher() {
        GlobalFunction.startActivity(getActivity(), AdminVoucherActivity.class);
    }

    private void onClickManageFeedback() {
        GlobalFunction.startActivity(getActivity(), AdminFeedbackActivity.class);
    }

    private void onClickSignOut() {
        if (getActivity() == null) return;
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), LoginActivity.class);
        getActivity().finishAffinity();
    }
}
