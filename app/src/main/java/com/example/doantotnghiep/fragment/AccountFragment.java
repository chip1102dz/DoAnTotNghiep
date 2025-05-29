package com.example.doantotnghiep.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.ChangePasswordActivity;
import com.example.doantotnghiep.activity.FeedbackActivity;
import com.example.doantotnghiep.activity.LoginActivity;
import com.example.doantotnghiep.activity.MainActivity;
import com.example.doantotnghiep.activity.StoreLocationActivity;
import com.example.doantotnghiep.databinding.FragmentAccountBinding;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.auth.FirebaseAuth;

public class AccountFragment extends Fragment {
    FragmentAccountBinding binding;
    private LinearLayout layoutStoreLocation;
    private LinearLayout layoutFeedback;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutSignOut;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        initToolbar();
        initUi();
        initListener();
        return binding.getRoot();
    }
    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> backToHomeScreen());
        tvToolbarTitle.setText(getString(R.string.nav_account));
    }

    private void backToHomeScreen() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;
        mainActivity.getViewPager2().setCurrentItem(0);
    }

    private void initUi() {
        TextView tvUsername = binding.tvUsername;
        tvUsername.setText(DataStoreManager.getUser().getEmail());
        layoutStoreLocation = binding.layoutStoreLocation;
        layoutFeedback = binding.layoutFeedback;
        layoutChangePassword = binding.layoutChangePassword;
        layoutSignOut = binding.layoutSignOut;
    }

    private void initListener() {
        layoutStoreLocation.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), StoreLocationActivity.class));
        layoutFeedback.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), FeedbackActivity.class));
        layoutChangePassword.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), ChangePasswordActivity.class));
        layoutSignOut.setOnClickListener(view -> onClickSignOut());
    }

    private void onClickSignOut() {
        if (getActivity() == null) return;

        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), LoginActivity.class);
        getActivity().finishAffinity();
    }
}