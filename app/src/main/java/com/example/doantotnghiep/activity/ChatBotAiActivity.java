package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ChatBotAdapter;
import com.example.doantotnghiep.databinding.ActivityChatBotAiBinding;
import com.example.doantotnghiep.model.ChatMessage;
import com.example.doantotnghiep.utils.GlobalFunction;

import java.util.ArrayList;
import java.util.List;

public class ChatBotAiActivity extends BaseActivity {

    private ActivityChatBotAiBinding binding;
    private RecyclerView rcvChat;
    private EditText edtMessage;
    private ImageView imgSend;
    private List<ChatMessage> listChatMessages;
    private ChatBotAdapter chatBotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
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

    private void setupWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
                "Xin chào! Tôi là trợ lý AI của cửa hàng. Tôi có thể giúp bạn:\n\n" +
                        "• Tìm kiếm sản phẩm\n" +
                        "• Tư vấn món ăn\n" +
                        "• Hỗ trợ đặt hàng\n" +
                        "• Thông tin khuyến mãi\n\n" +
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

        // Simulate bot response
        simulateBotResponse(messageText);
    }

    private void simulateBotResponse(String userMessage) {
        // Show typing indicator
        showTypingIndicator();

        // Simulate delay for bot response
        binding.getRoot().postDelayed(() -> {
            hideTypingIndicator();

            String botResponse = generateBotResponse(userMessage);
            ChatMessage botMessage = new ChatMessage(
                    botResponse,
                    ChatMessage.TYPE_BOT,
                    System.currentTimeMillis()
            );
            listChatMessages.add(botMessage);
            chatBotAdapter.notifyItemInserted(listChatMessages.size() - 1);
            rcvChat.scrollToPosition(listChatMessages.size() - 1);
        }, 1500);
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
        // Remove typing indicator
        for (int i = listChatMessages.size() - 1; i >= 0; i--) {
            if (listChatMessages.get(i).getType() == ChatMessage.TYPE_TYPING) {
                listChatMessages.remove(i);
                chatBotAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private String generateBotResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        if (message.contains("gà") || message.contains("chicken")) {
            return "Chúng tôi có nhiều món gà rán thơm ngon như:\n" +
                    "🍗 Gà rán giòn\n" +
                    "🍗 Gà rán cay\n" +
                    "🍗 Gà rán mật ong\n\n" +
                    "Bạn muốn xem chi tiết món nào không ạ?";
        } else if (message.contains("pizza")) {
            return "Pizza của chúng tôi rất được ưa chuộng:\n" +
                    "🍕 Pizza Margherita\n" +
                    "🍕 Pizza Pepperoni\n" +
                    "🍕 Pizza Hải sản\n\n" +
                    "Size nào bạn muốn đặt ạ?";
        } else if (message.contains("phở")) {
            return "Phở truyền thống của chúng tôi có:\n" +
                    "🍜 Phở bò tái\n" +
                    "🍜 Phở bò chín\n" +
                    "🍜 Phở gà\n\n" +
                    "Tất cả đều được nấu từ nước dùng hầm xương 12 tiếng!";
        } else if (message.contains("trà sữa") || message.contains("bubble tea")) {
            return "Trà sữa của chúng tôi rất đa dạng:\n" +
                    "🧋 Trà sữa trân châu\n" +
                    "🧋 Trà sữa matcha\n" +
                    "🧋 Trà sữa taro\n\n" +
                    "Bạn muốn size nào và độ đường bao nhiêu % ạ?";
        } else if (message.contains("giá") || message.contains("price")) {
            return "Giá cả các món ăn của chúng tôi rất hợp lý:\n" +
                    "💰 Gà rán: 45.000-65.000 VND\n" +
                    "💰 Pizza: 89.000-159.000 VND\n" +
                    "💰 Phở: 35.000-55.000 VND\n" +
                    "💰 Trà sữa: 25.000-35.000 VND\n\n" +
                    "Hiện tại có nhiều khuyến mãi hấp dẫn!";
        } else if (message.contains("khuyến mãi") || message.contains("giảm giá")) {
            return "🎉 Khuyến mãi HOT hiện tại:\n" +
                    "✨ Giảm 20% cho đơn hàng trên 200k\n" +
                    "✨ Mua 2 tặng 1 cho trà sữa\n" +
                    "✨ Free ship trong bán kính 5km\n\n" +
                    "Nhanh tay đặt hàng để nhận ưu đãi nhé!";
        } else if (message.contains("đặt hàng") || message.contains("order")) {
            return "Để đặt hàng, bạn có thể:\n" +
                    "📱 Chọn món trong app\n" +
                    "📱 Thêm vào giỏ hàng\n" +
                    "📱 Chọn địa chỉ giao hàng\n" +
                    "📱 Thanh toán\n\n" +
                    "Tôi có thể giúp bạn tìm món ăn phù hợp không?";
        } else if (message.contains("địa chỉ") || message.contains("cửa hàng")) {
            return "📍 Cửa hàng của chúng tôi:\n" +
                    "🏪 Địa chỉ: 123 Đường ABC, Quận XYZ\n" +
                    "📞 Hotline: 1900-1234\n" +
                    "🕒 Giờ mở cửa: 8:00 - 22:00\n\n" +
                    "Chúng tôi có giao hàng tận nơi trong 30 phút!";
        } else if (message.contains("cảm ơn") || message.contains("thanks")) {
            return "Cảm ơn bạn đã tin tướng và sử dụng dịch vụ! 🙏\n" +
                    "Chúng tôi luôn sẵn sàng hỗ trợ bạn 24/7.\n\n" +
                    "Chúc bạn có trải nghiệm tuyệt vời! 😊";
        } else {
            return "Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn. 😅\n\n" +
                    "Bạn có thể hỏi tôi về:\n" +
                    "• Thông tin món ăn\n" +
                    "• Giá cả\n" +
                    "• Khuyến mãi\n" +
                    "• Cách đặt hàng\n" +
                    "• Địa chỉ cửa hàng\n\n" +
                    "Hoặc gõ từ khóa như 'gà rán', 'pizza', 'phở'...";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}