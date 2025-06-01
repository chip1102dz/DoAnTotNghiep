package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemChatBotBinding;
import com.example.doantotnghiep.databinding.ItemChatTypingBinding;
import com.example.doantotnghiep.databinding.ItemChatUserBinding;
import com.example.doantotnghiep.model.ChatMessage;
import com.example.doantotnghiep.utils.DateTimeUtils;

import java.util.List;

public class ChatBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> listChatMessages;

    public ChatBotAdapter(List<ChatMessage> listChatMessages) {
        this.listChatMessages = listChatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        return listChatMessages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ChatMessage.TYPE_USER:
                ItemChatUserBinding userBinding = ItemChatUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new UserMessageViewHolder(userBinding);

            case ChatMessage.TYPE_TYPING:
                ItemChatTypingBinding typingBinding = ItemChatTypingBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new TypingViewHolder(typingBinding);

            default: // ChatMessage.TYPE_BOT
                ItemChatBotBinding botBinding = ItemChatBotBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new BotMessageViewHolder(botBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = listChatMessages.get(position);

        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_USER:
                ((UserMessageViewHolder) holder).bind(chatMessage);
                break;
            case ChatMessage.TYPE_TYPING:
                ((TypingViewHolder) holder).bind(chatMessage);
                break;
            default: // ChatMessage.TYPE_BOT
                ((BotMessageViewHolder) holder).bind(chatMessage);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listChatMessages != null ? listChatMessages.size() : 0;
    }

    // ViewHolder for Bot messages
    public static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBotBinding binding;

        public BotMessageViewHolder(@NonNull ItemChatBotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage chatMessage) {
            binding.tvBotMessage.setText(chatMessage.getMessage());
            binding.tvBotTime.setText(DateTimeUtils.convertTimeStampToDate(chatMessage.getTimestamp()));
        }
    }

    // ViewHolder for User messages
    public static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatUserBinding binding;

        public UserMessageViewHolder(@NonNull ItemChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage chatMessage) {
            binding.tvUserMessage.setText(chatMessage.getMessage());
            binding.tvUserTime.setText(DateTimeUtils.convertTimeStampToDate(chatMessage.getTimestamp()));
        }
    }

    // ViewHolder for Typing indicator
    public static class TypingViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatTypingBinding binding;

        public TypingViewHolder(@NonNull ItemChatTypingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage chatMessage) {
            binding.tvTypingMessage.setText(chatMessage.getMessage());
            // Có thể thêm animation cho typing indicator ở đây
        }
    }
}