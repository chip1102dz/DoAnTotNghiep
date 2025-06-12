package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemNotificationBinding;
import com.example.doantotnghiep.model.Notification;
import com.example.doantotnghiep.utils.DateTimeUtils;
import com.example.doantotnghiep.utils.StringUtil;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> listNotifications;
    private IClickNotificationListener iClickNotificationListener;

    public interface IClickNotificationListener {
        void onClickNotificationItem(Notification notification);
        void onClickNotificationAction(Notification notification);
        void onClickMarkAsRead(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> listNotifications,
                               IClickNotificationListener listener) {
        this.context = context;
        this.listNotifications = listNotifications;
        this.iClickNotificationListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = listNotifications.get(position);
        if (notification == null) return;

        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return listNotifications != null ? listNotifications.size() : 0;
    }

    public void release() {
        if (context != null) context = null;
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        private ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Notification notification) {
            // Set icon based on notification type
            binding.imgNotificationIcon.setImageResource(notification.getIconResource());

            // Set background color for icon based on type
            int iconBackgroundColor;
            switch (notification.getType()) {
                case Notification.TYPE_ORDER:
                    iconBackgroundColor = R.color.colorPrimary;
                    break;
                case Notification.TYPE_PROMOTION:
                    iconBackgroundColor = R.color.orange;
                    break;
                case Notification.TYPE_SYSTEM:
                    iconBackgroundColor = R.color.green;
                    break;
                default:
                    iconBackgroundColor = R.color.colorPrimary;
                    break;
            }
            binding.imgNotificationIcon.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, iconBackgroundColor));

            // Set title and message
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationMessage.setText(notification.getMessage());

            // Set time
            String timeText = getTimeAgo(notification.getTimestamp());
            binding.tvNotificationTime.setText(timeText);

            // Set read/unread state
            if (notification.isRead()) {
                binding.viewUnreadIndicator.setVisibility(View.GONE);
                binding.tvNotificationTitle.setTextColor(
                        ContextCompat.getColor(context, R.color.textColorSecondary));
                binding.layoutNotificationItem.setAlpha(0.7f);
            } else {
                binding.viewUnreadIndicator.setVisibility(View.VISIBLE);
                binding.tvNotificationTitle.setTextColor(
                        ContextCompat.getColor(context, R.color.textColorHeading));
                binding.layoutNotificationItem.setAlpha(1.0f);
            }

            // Set action button visibility and text
            if (!StringUtil.isEmpty(notification.getActionText()) &&
                    notification.getType() == Notification.TYPE_ORDER) {
                binding.tvNotificationAction.setVisibility(View.VISIBLE);
                binding.tvNotificationAction.setText(notification.getActionText());

                binding.tvNotificationAction.setOnClickListener(v -> {
                    if (iClickNotificationListener != null) {
                        iClickNotificationListener.onClickNotificationAction(notification);
                    }
                });
            } else {
                binding.tvNotificationAction.setVisibility(View.GONE);
            }

            // Set item click listener
            binding.layoutNotificationItem.setOnClickListener(v -> {
                if (iClickNotificationListener != null) {
                    if (!notification.isRead()) {
                        // Mark as read when clicked
                        iClickNotificationListener.onClickMarkAsRead(notification);
                    }
                    iClickNotificationListener.onClickNotificationItem(notification);
                }
            });
        }

        private String getTimeAgo(long timestamp) {
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - timestamp;

            // Convert to minutes
            long diffMinutes = diffTime / (60 * 1000);

            if (diffMinutes < 1) {
                return "Vừa xong";
            } else if (diffMinutes < 60) {
                return diffMinutes + " phút trước";
            } else if (diffMinutes < 1440) { // Less than 24 hours
                long diffHours = diffMinutes / 60;
                return diffHours + " giờ trước";
            } else if (diffMinutes < 10080) { // Less than 7 days
                long diffDays = diffMinutes / 1440;
                return diffDays + " ngày trước";
            } else {
                // Show actual date for older notifications
                return DateTimeUtils.convertTimeStampToDate_2(timestamp);
            }
        }
    }
}