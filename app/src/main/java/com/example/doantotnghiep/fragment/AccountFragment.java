package com.example.doantotnghiep.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.ChangePasswordActivity;
import com.example.doantotnghiep.activity.FeedbackActivity;
import com.example.doantotnghiep.activity.LoginActivity;
import com.example.doantotnghiep.activity.MainActivity;
import com.example.doantotnghiep.activity.StoreLocationActivity;
import com.example.doantotnghiep.activity.UserProfileActivity;
import com.example.doantotnghiep.activity.TopUpActivity;
import com.example.doantotnghiep.databinding.FragmentAccountBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.GlideUtils;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    // User Info Views
    private CircleImageView imgAvatar;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvBalance;
    private ImageView imgEditProfile;

    // Menu Views
    private LinearLayout layoutMyOrders;
    private LinearLayout layoutStoreLocation;
    private LinearLayout layoutFeedback;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutSignOut;
    private LinearLayout layoutTopUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        initToolbar();
        initUi();
        initListener();
        loadUserInfo();

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
        // User Info Views
        imgAvatar = binding.imgAvatar;
        tvFullName = binding.tvFullName;
        tvEmail = binding.tvEmail;
        tvPhone = binding.tvPhone;
        tvAddress = binding.tvAddress;
        tvBalance = binding.tvBalance;
        imgEditProfile = binding.imgEditProfile;

        // Menu Views
        layoutMyOrders = binding.layoutMyOrders;
        layoutStoreLocation = binding.layoutStoreLocation;
        layoutFeedback = binding.layoutFeedback;
        layoutChangePassword = binding.layoutChangePassword;
        layoutSignOut = binding.layoutSignOut;
        layoutTopUp = binding.layoutTopUp;
    }

    private void initListener() {
        // Edit Profile
        imgEditProfile.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), UserProfileActivity.class));

        // Top Up Balance
        layoutTopUp.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), TopUpActivity.class));

        // My Orders - Navigate to history tab
        layoutMyOrders.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.getViewPager2().setCurrentItem(1); // History tab
            }
        });

        // Store Location
        layoutStoreLocation.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), StoreLocationActivity.class));

        // Feedback
        layoutFeedback.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), FeedbackActivity.class));

        // Change Password
        layoutChangePassword.setOnClickListener(view ->
                GlobalFunction.startActivity(getActivity(), ChangePasswordActivity.class));

        // Sign Out
        layoutSignOut.setOnClickListener(view -> onClickSignOut());
    }

    private void loadUserInfo() {
        User user = DataStoreManager.getUser();
        if (user == null) return;

        // Load basic info
        tvEmail.setText(user.getEmail());

        // Load full name
        if (!StringUtil.isEmpty(user.getFullName())) {
            tvFullName.setText(user.getFullName());
        } else {
            tvFullName.setText(user.getDisplayName());
        }

        // Load phone number
        if (!StringUtil.isEmpty(user.getPhoneNumber())) {
            tvPhone.setText(user.getPhoneNumber());
        } else {
            tvPhone.setText("Chưa cập nhật");
            tvPhone.setTextColor(getResources().getColor(R.color.textColorAccent));
        }

        // Load address
        if (!StringUtil.isEmpty(user.getAddress())) {
            tvAddress.setText(user.getAddress());
        } else {
            tvAddress.setText("Chưa cập nhật");
            tvAddress.setTextColor(getResources().getColor(R.color.textColorAccent));
        }

        // Load balance
        tvBalance.setText(user.getFormattedBalance());

        // Load avatar
        if (!StringUtil.isEmpty(user.getProfileImageUrl())) {
            GlideUtils.loadUrl(user.getProfileImageUrl(), imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_default);
        }

        // Show completion prompt if profile is incomplete
        if (!user.hasCompleteProfile()) {
            showProfileCompletionPrompt();
        }
    }

    private void showProfileCompletionPrompt() {
        // Hiển thị thông báo nhắc nhở hoàn thiện thông tin
        if (getActivity() != null) {
            GlobalFunction.showToastMessage(getActivity(),
                    "Hãy hoàn thiện thông tin cá nhân để có trải nghiệm tốt hơn!");
        }
    }

    private void onClickSignOut() {
        if (getActivity() == null) return;

        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), LoginActivity.class);
        getActivity().finishAffinity();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload user info when returning to fragment
        loadUserInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}