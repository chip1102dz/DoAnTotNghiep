package com.example.doantotnghiep.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.doantotnghiep.MyApplication;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

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

    // Firebase listener
    private ValueEventListener balanceListener;

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
        if (user == null || StringUtil.isEmpty(user.getEmail())) return;

        // Load thông tin cơ bản từ local trước
        displayUserInfo(user);

        // Sau đó đồng bộ từ Firebase
        syncUserDataFromFirebase();

        // Setup realtime sync cho số dư
        setupRealtimeBalanceSync();
    }

    private void syncUserDataFromFirebase() {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        MyApplication.get(getActivity()).getUserDatabaseReference(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && getActivity() != null) {
                            Log.d(TAG, "Syncing user data from Firebase");

                            // Lấy dữ liệu từ Firebase
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            String dateOfBirth = snapshot.child("dateOfBirth").getValue(String.class);
                            String gender = snapshot.child("gender").getValue(String.class);
                            Double balance = snapshot.child("balance").getValue(Double.class);

                            // Cập nhật User object
                            User currentUser = DataStoreManager.getUser();
                            boolean isUpdated = false;

                            if (fullName != null && !fullName.equals(currentUser.getFullName())) {
                                currentUser.setFullName(fullName);
                                isUpdated = true;
                            }
                            if (phoneNumber != null && !phoneNumber.equals(currentUser.getPhoneNumber())) {
                                currentUser.setPhoneNumber(phoneNumber);
                                isUpdated = true;
                            }
                            if (address != null && !address.equals(currentUser.getAddress())) {
                                currentUser.setAddress(address);
                                isUpdated = true;
                            }
                            if (profileImageUrl != null && !profileImageUrl.equals(currentUser.getProfileImageUrl())) {
                                currentUser.setProfileImageUrl(profileImageUrl);
                                isUpdated = true;
                            }
                            if (dateOfBirth != null && !dateOfBirth.equals(currentUser.getDateOfBirth())) {
                                currentUser.setDateOfBirth(dateOfBirth);
                                isUpdated = true;
                            }
                            if (gender != null && !gender.equals(currentUser.getGender())) {
                                currentUser.setGender(gender);
                                isUpdated = true;
                            }
                            if (balance != null && balance != currentUser.getBalance()) {
                                currentUser.setBalance(balance);
                                isUpdated = true;
                                Log.d(TAG, "Updated balance from Firebase: " + balance);
                            }

                            // Lưu lại vào local nếu có thay đổi
                            if (isUpdated) {
                                DataStoreManager.setUser(currentUser);
                                // Cập nhật UI
                                displayUserInfo(currentUser);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to sync user data: " + error.getMessage());
                    }
                });
    }

    private void setupRealtimeBalanceSync() {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        balanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getActivity() != null) {
                    Double balance = snapshot.child("balance").getValue(Double.class);
                    if (balance != null) {
                        User currentUser = DataStoreManager.getUser();
                        if (currentUser != null && balance != currentUser.getBalance()) {
                            currentUser.setBalance(balance);
                            DataStoreManager.setUser(currentUser);

                            // Cập nhật UI
                            tvBalance.setText(currentUser.getFormattedBalance());
                            Log.d(TAG, "Real-time balance updated: " + balance);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Balance sync error: " + error.getMessage());
            }
        };

        MyApplication.get(getActivity()).getUserDatabaseReference(userKey)
                .addValueEventListener(balanceListener);
    }

    private void displayUserInfo(User user) {
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

        // Remove listener để tránh memory leak
        if (balanceListener != null && getActivity() != null) {
            String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
            MyApplication.get(getActivity()).getUserDatabaseReference(userKey)
                    .removeEventListener(balanceListener);
        }

        binding = null;
    }
}