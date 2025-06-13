package com.example.doantotnghiep.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.MainActivity;
import com.example.doantotnghiep.activity.TrackingOrderActivity;
import com.example.doantotnghiep.adapter.NotificationAdapter;
import com.example.doantotnghiep.databinding.FragmentNotificationBinding;
import com.example.doantotnghiep.model.Notification;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;

    // UI Components
    private RecyclerView rcvNotifications;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressLoading;
    private TextView tvAllNotifications, tvOrderNotifications, tvPromoNotifications;
    private TextView tvMarkAllRead;

    // Data
    private List<Notification> listAllNotifications;
    private List<Notification> listDisplayNotifications;
    private NotificationAdapter notificationAdapter;
    private ValueEventListener mValueEventListener;

    // Filter state
    private int currentFilter = FILTER_ALL;
    private static final int FILTER_ALL = 0;
    private static final int FILTER_ORDER = 1;
    private static final int FILTER_PROMO = 2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        initToolbar();
        initUi();
        initListener();
        loadNotificationsFromFirebase();

        return binding.getRoot();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> backToHomeScreen());
        tvToolbarTitle.setText(getString(R.string.notification_title));
    }

    private void backToHomeScreen() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;
        mainActivity.getViewPager2().setCurrentItem(0);
    }

    private void initUi() {
        rcvNotifications = binding.rcvNotifications;
        layoutEmptyState = binding.layoutEmptyState;
        progressLoading = binding.progressLoading;
        tvAllNotifications = binding.tvAllNotifications;
        tvOrderNotifications = binding.tvOrderNotifications;
        tvPromoNotifications = binding.tvPromoNotifications;
        tvMarkAllRead = binding.tvMarkAllRead;

        // Setup RecyclerView
        listAllNotifications = new ArrayList<>();
        listDisplayNotifications = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvNotifications.setLayoutManager(linearLayoutManager);

        notificationAdapter = new NotificationAdapter(getContext(), listDisplayNotifications,
                new NotificationAdapter.IClickNotificationListener() {
                    @Override
                    public void onClickNotificationItem(Notification notification) {
                        handleNotificationClick(notification);
                    }

                    @Override
                    public void onClickNotificationAction(Notification notification) {
                        handleNotificationAction(notification);
                    }

                    @Override
                    public void onClickMarkAsRead(Notification notification) {
                        markNotificationAsRead(notification);
                    }
                });

        rcvNotifications.setAdapter(notificationAdapter);

        // Set initial filter state
        updateFilterButtonState(FILTER_ALL);
    }

    private void initListener() {
        // Filter button listeners
        tvAllNotifications.setOnClickListener(v -> {
            currentFilter = FILTER_ALL;
            updateFilterButtonState(FILTER_ALL);
            filterNotifications();
        });

        tvOrderNotifications.setOnClickListener(v -> {
            currentFilter = FILTER_ORDER;
            updateFilterButtonState(FILTER_ORDER);
            filterNotifications();
        });

        tvPromoNotifications.setOnClickListener(v -> {
            currentFilter = FILTER_PROMO;
            updateFilterButtonState(FILTER_PROMO);
            filterNotifications();
        });

        // Mark all read listener
        tvMarkAllRead.setOnClickListener(v -> markAllNotificationsAsRead());
    }

    private void updateFilterButtonState(int selectedFilter) {
        // Reset all buttons
        tvAllNotifications.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
        tvAllNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorHeading));

        tvOrderNotifications.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
        tvOrderNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorHeading));

        tvPromoNotifications.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
        tvPromoNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorHeading));

        // Set selected button
        switch (selectedFilter) {
            case FILTER_ALL:
                tvAllNotifications.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                tvAllNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                break;
            case FILTER_ORDER:
                tvOrderNotifications.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                tvOrderNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                break;
            case FILTER_PROMO:
                tvPromoNotifications.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                tvPromoNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                break;
        }
    }

    private void loadNotificationsFromFirebase() {
        if (getActivity() == null) return;

        showLoading(true);

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                listAllNotifications.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        listAllNotifications.add(notification);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(listAllNotifications, (n1, n2) ->
                        Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                filterNotifications();
                updateMarkAllReadVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                showEmptyState();
                if (getActivity() != null) {
                    GlobalFunction.showToastMessage(getActivity(),
                            getString(R.string.msg_get_date_error));
                }
            }
        };

        // Query notifications for current user
        String userEmail = DataStoreManager.getUser().getEmail();
        MyApplication.get(getActivity()).getNotificationDatabaseReference()
                .orderByChild("userEmail")
                .equalTo(userEmail)
                .addValueEventListener(mValueEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNotifications() {
        listDisplayNotifications.clear();

        switch (currentFilter) {
            case FILTER_ALL:
                listDisplayNotifications.addAll(listAllNotifications);
                break;
            case FILTER_ORDER:
                for (Notification notification : listAllNotifications) {
                    if (notification.getType() == Notification.TYPE_ORDER) {
                        listDisplayNotifications.add(notification);
                    }
                }
                break;
            case FILTER_PROMO:
                for (Notification notification : listAllNotifications) {
                    if (notification.getType() == Notification.TYPE_PROMOTION) {
                        listDisplayNotifications.add(notification);
                    }
                }
                break;
        }

        if (listDisplayNotifications.isEmpty()) {
            showEmptyState();
        } else {
            showNotificationList();
        }

        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
    }

    private void handleNotificationClick(Notification notification) {
        // Handle different types of notifications
        switch (notification.getType()) {
            case Notification.TYPE_ORDER:
                if (notification.getOrderId() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(Constant.ORDER_ID, notification.getOrderId());
                    GlobalFunction.startActivity(getActivity(), TrackingOrderActivity.class, bundle);
                }
                break;
            case Notification.TYPE_PROMOTION:
                // Navigate to promotions or specific product
                // You can implement this based on your needs
                break;
            case Notification.TYPE_SYSTEM:
                // Handle system notifications
                break;
        }
    }

    private void handleNotificationAction(Notification notification) {
        // Handle action button clicks
        if (notification.getType() == Notification.TYPE_ORDER && notification.getOrderId() != null) {
            Bundle bundle = new Bundle();
            bundle.putLong(Constant.ORDER_ID, notification.getOrderId());
            GlobalFunction.startActivity(getActivity(), TrackingOrderActivity.class, bundle);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void markNotificationAsRead(Notification notification) {
        if (notification.isRead()) return;

        notification.setRead(true);

        // Update in Firebase
        if (getActivity() != null) {
            String userEmail = DataStoreManager.getUser().getEmail();
            MyApplication.get(getActivity()).getNotificationDatabaseReference()
                    .child(String.valueOf(notification.getId()))
                    .child("read")
                    .setValue(true);
        }

        // Update UI
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }

        updateMarkAllReadVisibility();
    }

    private void markAllNotificationsAsRead() {
        if (getActivity() == null) return;

        Map<String, Object> updates = new HashMap<>();
        boolean hasUnreadNotifications = false;

        for (Notification notification : listDisplayNotifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                updates.put(String.valueOf(notification.getId()) + "/read", true);
                hasUnreadNotifications = true;
            }
        }

        if (hasUnreadNotifications) {
            // Update Firebase
            MyApplication.get(getActivity()).getDatabase().getReference("notifications")
                    .updateChildren(updates);

            // Update UI
            if (notificationAdapter != null) {
                notificationAdapter.notifyDataSetChanged();
            }

            updateMarkAllReadVisibility();
            GlobalFunction.showToastMessage(getActivity(), "Đã đánh dấu tất cả là đã đọc");
        }
    }

    private void updateMarkAllReadVisibility() {
        boolean hasUnreadNotifications = false;
        for (Notification notification : listDisplayNotifications) {
            if (!notification.isRead()) {
                hasUnreadNotifications = true;
                break;
            }
        }

        tvMarkAllRead.setVisibility(hasUnreadNotifications ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressLoading.setVisibility(View.VISIBLE);
            rcvNotifications.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        } else {
            progressLoading.setVisibility(View.GONE);
        }
    }

    private void showNotificationList() {
        rcvNotifications.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rcvNotifications.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    // Method to create sample notifications for testing
    private void createSampleNotifications() {
        if (getActivity() == null) return;

        String userEmail = DataStoreManager.getUser().getEmail();

        // Sample order notification
        Notification orderNotification = Notification.createOrderNotification(
                userEmail, 123456L, Notification.ORDER_STATUS_CONFIRMED);
        orderNotification.setId(System.currentTimeMillis());

        // Sample promotion notification
        Notification promoNotification = Notification.createPromotionNotification(
                userEmail, "🎉 Giảm giá 50%", "Giảm giá 50% cho tất cả sản phẩm trong tuần này!", "SALE50");
        promoNotification.setId(System.currentTimeMillis() + 1);

        // Sample system notification
        Notification systemNotification = Notification.createSystemNotification(
                userEmail, "Cập nhật ứng dụng", "Phiên bản mới đã có sẵn với nhiều tính năng thú vị!");
        systemNotification.setId(System.currentTimeMillis() + 2);

        // Save to Firebase
        MyApplication.get(getActivity()).getNotificationDatabaseReference()
                .child(String.valueOf(orderNotification.getId()))
                .setValue(orderNotification);

        MyApplication.get(getActivity()).getNotificationDatabaseReference()
                .child(String.valueOf(promoNotification.getId()))
                .setValue(promoNotification);

        MyApplication.get(getActivity()).getNotificationDatabaseReference()
                .child(String.valueOf(systemNotification.getId()))
                .setValue(systemNotification);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (notificationAdapter != null) {
            notificationAdapter.release();
        }

        if (getActivity() != null && mValueEventListener != null) {
            MyApplication.get(getActivity()).getNotificationDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }

        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh notifications when fragment is resumed
        if (listAllNotifications != null && !listAllNotifications.isEmpty()) {
            filterNotifications();
            updateMarkAllReadVisibility();
        }
    }
}