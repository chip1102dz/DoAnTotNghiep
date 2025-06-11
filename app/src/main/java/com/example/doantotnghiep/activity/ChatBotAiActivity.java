package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ChatBotAdapter;
import com.example.doantotnghiep.databinding.ActivityChatBotAiBinding;
import com.example.doantotnghiep.model.ChatMessage;
import com.example.doantotnghiep.service.ChatbotService;
import com.example.doantotnghiep.utils.DateTimeUtils;
import com.example.doantotnghiep.utils.GlobalFunction;

import java.util.ArrayList;
import java.util.List;

public class ChatBotAiActivity extends BaseActivity {

    private static final String TAG = "ChatBotAiActivity";

    private ActivityChatBotAiBinding binding;
    private RecyclerView rcvChat;
    private EditText edtMessage;
    private ImageView imgSend;
    private List<ChatMessage> listChatMessages;
    private ChatBotAdapter chatBotAdapter;

    // Chatbot service
    private ChatbotService chatbotService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        setupChatbotService();
        setupWelcomeMessage();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.chatbot_title));
    }

    private void initUi() {
        rcvChat = binding.rcvChat;
        edtMessage = binding.edtMessage;
        imgSend = binding.imgSend;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvChat.setLayoutManager(linearLayoutManager);

        listChatMessages = new ArrayList<>();
        chatBotAdapter = new ChatBotAdapter(listChatMessages);
        rcvChat.setAdapter(chatBotAdapter);
    }

    private void initListener() {
        imgSend.setOnClickListener(v -> sendMessage());

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String message = s.toString().trim();
                if (message.isEmpty()) {
                    imgSend.setImageResource(R.drawable.ic_send_disable);
                    imgSend.setEnabled(false);
                } else {
                    imgSend.setImageResource(R.drawable.ic_send_enable);
                    imgSend.setEnabled(true);
                }
            }
        });

        edtMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void setupChatbotService() {
        try {
            chatbotService = new ChatbotService(this);

            // Load data với listener
            chatbotService.loadFirebaseData(new ChatbotService.DataLoadListener() {
                @Override
                public void onDataLoaded() {
                    Log.d(TAG, "Firebase data loaded successfully");
                    runOnUiThread(() -> {
                        // Có thể hiển thị thông báo data đã load xong
                        // hoặc cập nhật welcome message
                    });
                }

                @Override
                public void onDataError(String error) {
                    Log.e(TAG, "Error loading Firebase data: " + error);
                    runOnUiThread(() -> {
                        showToastMessage("Lỗi tải dữ liệu: " + error);
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up chatbot service: " + e.getMessage());
            e.printStackTrace();
            showToastMessage("Lỗi khởi tạo chatbot: " + e.getMessage());
        }
    }

    private void setupWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
                "Xin chào! Tôi là trợ lý AI của cửa hàng. Tôi có thể giúp bạn:\n\n" +
                        "• Tìm kiếm sản phẩm\n" +
                        "• Tư vấn món ăn\n" +
                        "• Hỗ trợ đặt hàng\n" +
                        "• Thông tin khuyến mãi\n" +
                        "• Thông tin cửa hàng\n\n" +
                        "Bạn cần hỗ trợ gì ạ?",
                ChatMessage.TYPE_BOT,
                System.currentTimeMillis()
        );
        listChatMessages.add(welcomeMessage);
        chatBotAdapter.notifyItemInserted(listChatMessages.size() - 1);
        rcvChat.scrollToPosition(listChatMessages.size() - 1);
    }

    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Add user message
        ChatMessage userMessage = new ChatMessage(
                messageText,
                ChatMessage.TYPE_USER,
                System.currentTimeMillis()
        );
        listChatMessages.add(userMessage);
        chatBotAdapter.notifyItemInserted(listChatMessages.size() - 1);
        rcvChat.scrollToPosition(listChatMessages.size() - 1);

        // Clear input
        edtMessage.setText("");
        GlobalFunction.hideSoftKeyboard(this);

        // Show typing indicator
        showTypingIndicator();

        // Process with chatbot service
        if (chatbotService != null) {
            chatbotService.sendMessage(messageText, new ChatbotService.ChatbotResponseListener() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(() -> {
                        hideTypingIndicator();
                        addBotMessage(response);
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Chatbot error: " + error);
                    runOnUiThread(() -> {
                        hideTypingIndicator();
                        addBotMessage("Xin lỗi, tôi gặp sự cố kỹ thuật. Vui lòng thử lại sau.");
                    });
                }
            });
        } else {
            // Fallback nếu service không khả dụng
            hideTypingIndicator();
            addBotMessage("Xin lỗi, hệ thống chatbot đang bảo trì. Vui lòng liên hệ hotline để được hỗ trợ.");
        }
    }

    private void showTypingIndicator() {
        ChatMessage typingMessage = new ChatMessage(
                "Đang soạn tin...",
                ChatMessage.TYPE_TYPING,
                System.currentTimeMillis()
        );
        listChatMessages.add(typingMessage);
        chatBotAdapter.notifyItemInserted(listChatMessages.size() - 1);
        rcvChat.scrollToPosition(listChatMessages.size() - 1);
    }

    private void hideTypingIndicator() {
        for (int i = listChatMessages.size() - 1; i >= 0; i--) {
            if (listChatMessages.get(i).getType() == ChatMessage.TYPE_TYPING) {
                listChatMessages.remove(i);
                chatBotAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void addBotMessage(String message) {
        ChatMessage botMessage = new ChatMessage(
                message,
                ChatMessage.TYPE_BOT,
                System.currentTimeMillis()
        );
        listChatMessages.add(botMessage);
        chatBotAdapter.notifyItemInserted(listChatMessages.size() - 1);
        rcvChat.scrollToPosition(listChatMessages.size() - 1);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup chatbot service
        if (chatbotService != null) {
            try {
                chatbotService.cleanup();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up chatbot service: " + e.getMessage());
            }
        }

        binding = null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Hide keyboard when activity is paused
        GlobalFunction.hideSoftKeyboard(this);
    }
}