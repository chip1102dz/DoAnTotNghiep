package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ChatBotAdapter;
import com.example.doantotnghiep.databinding.ActivityChatBotAiBinding;
import com.example.doantotnghiep.model.ChatMessage;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatBotAiActivity extends BaseActivity {

    private static final String TAG = "ChatBotAiActivity";
    private static final String PROJECT_ID = "doantotnghieppro-d2186"; // Thay bằng project ID của bạn
    private static final String LANGUAGE_CODE = "vi"; // Tiếng Việt

    private ActivityChatBotAiBinding binding;
    private RecyclerView rcvChat;
    private EditText edtMessage;
    private ImageView imgSend;
    private List<ChatMessage> listChatMessages;
    private ChatBotAdapter chatBotAdapter;

    // Dialogflow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private ExecutorService executorService;

    // Firebase data cache
    private List<Product> cachedProducts;
    private Map<String, Object> storeInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        setupDialogflow();
        loadDataFromFirebase();
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

        executorService = Executors.newSingleThreadExecutor();
        cachedProducts = new ArrayList<>();
        storeInfo = new HashMap<>();
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

    private void setupDialogflow() {
        try {
            // Đọc credentials từ assets
            InputStream stream = getAssets().open("dialogflow_credentials.json");
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(stream);

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(PROJECT_ID, UUID.randomUUID().toString());

            Log.d(TAG, "Dialogflow setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Dialogflow: " + e.getMessage());
            // Fallback to simple bot
            setupFallbackBot();
        }
    }

    private void loadDataFromFirebase() {
        // Load products for chatbot context
        MyApplication.get(this).getProductDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cachedProducts.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null) {
                                cachedProducts.add(product);
                            }
                        }
                        Log.d(TAG, "Loaded " + cachedProducts.size() + " products");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading products: " + error.getMessage());
                    }
                });

        // Load store location info
        MyApplication.get(this).getStoreLocationDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            storeInfo.put("address", snapshot.child("address").getValue(String.class));
                            storeInfo.put("phone", snapshot.child("phone").getValue(String.class));
                            Log.d(TAG, "Store info loaded");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading store info: " + error.getMessage());
                    }
                });
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

        // Process with Dialogflow
        if (sessionsClient != null) {
            processWithDialogflow(messageText);
        } else {
            // Fallback to simple bot
            simulateBotResponse(messageText);
        }
    }

    private void processWithDialogflow(String message) {
        showTypingIndicator();

        executorService.execute(() -> {
            try {
                // Create text input
                TextInput.Builder textInput = TextInput.newBuilder()
                        .setText(message)
                        .setLanguageCode(LANGUAGE_CODE);

                QueryInput queryInput = QueryInput.newBuilder()
                        .setText(textInput)
                        .build();

                DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
                        .setSession(sessionName.toString())
                        .setQueryInput(queryInput)
                        .build();

                // Detect intent
                DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
                QueryResult queryResult = response.getQueryResult();

                runOnUiThread(() -> {
                    hideTypingIndicator();
                    processDialogflowResponse(queryResult, message);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error in Dialogflow request: " + e.getMessage());
                runOnUiThread(() -> {
                    hideTypingIndicator();
                    // Fallback to simple response
                    String fallbackResponse = generateFallbackResponse(message);
                    addBotMessage(fallbackResponse);
                });
            }
        });
    }

    private void processDialogflowResponse(QueryResult queryResult, String originalMessage) {
        String intent = queryResult.getIntent().getDisplayName();
        String fulfillmentText = queryResult.getFulfillmentText();

        Log.d(TAG, "Intent: " + intent);
        Log.d(TAG, "Fulfillment: " + fulfillmentText);

        // Enhance response with Firebase data
        String enhancedResponse = enhanceResponseWithFirebaseData(intent, fulfillmentText, originalMessage);
        addBotMessage(enhancedResponse);
    }

    private String enhanceResponseWithFirebaseData(String intent, String fulfillmentText, String originalMessage) {
        switch (intent) {
            case "product.search":
                return searchProductsAndRespond(originalMessage, fulfillmentText);

            case "store.info":
                return getStoreInfoResponse(fulfillmentText);

            case "menu.recommendation":
                return getMenuRecommendation(fulfillmentText);

            case "price.inquiry":
                return getPriceInfoResponse(originalMessage, fulfillmentText);

            default:
                return fulfillmentText.isEmpty() ? generateFallbackResponse(originalMessage) : fulfillmentText;
        }
    }

    private String searchProductsAndRespond(String message, String baseResponse) {
        String searchTerm = extractSearchTerm(message);
        List<Product> foundProducts = new ArrayList<>();

        for (Product product : cachedProducts) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(searchTerm.toLowerCase())) {
                foundProducts.add(product);
                if (foundProducts.size() >= 5) break; // Limit to 5 products
            }
        }

        if (foundProducts.isEmpty()) {
            return baseResponse + "\n\nRất tiếc, tôi không tìm thấy sản phẩm nào phù hợp. Bạn có thể thử tìm kiếm với từ khóa khác không?";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\nTôi tìm thấy những sản phẩm sau:\n\n");
        for (Product product : foundProducts) {
            response.append("🍽️ ").append(product.getName())
                    .append(" - ").append(product.getRealPrice()).append("đ\n");
        }
        response.append("\nBạn muốn biết thêm thông tin về món nào không?");

        return response.toString();
    }

    private String getStoreInfoResponse(String baseResponse) {
        if (storeInfo.isEmpty()) {
            return baseResponse + "\n\nTôi đang cập nhật thông tin cửa hàng, vui lòng thử lại sau.";
        }

        return baseResponse + "\n\n📍 Địa chỉ: " + storeInfo.get("address") +
                "\n📞 Điện thoại: " + storeInfo.get("phone") +
                "\n🕒 Giờ mở cửa: 8:00 - 22:00 hàng ngày";
    }

    private String getMenuRecommendation(String baseResponse) {
        if (cachedProducts.isEmpty()) {
            return baseResponse + "\n\nTôi đang tải danh sách món ăn, vui lòng thử lại sau.";
        }

        // Get featured products
        List<Product> featuredProducts = new ArrayList<>();
        for (Product product : cachedProducts) {
            if (product.isFeatured()) {
                featuredProducts.add(product);
                if (featuredProducts.size() >= 3) break;
            }
        }

        if (featuredProducts.isEmpty()) {
            // Get top 3 products by rating
            cachedProducts.sort((p1, p2) -> Double.compare(p2.getRate(), p1.getRate()));
            for (int i = 0; i < Math.min(3, cachedProducts.size()); i++) {
                featuredProducts.add(cachedProducts.get(i));
            }
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\nTôi khuyên bạn nên thử:\n\n");
        for (Product product : featuredProducts) {
            response.append("⭐ ").append(product.getName())
                    .append(" - ").append(product.getRealPrice()).append("đ")
                    .append(" (⭐").append(product.getRate()).append(")\n");
        }

        return response.toString();
    }

    private String getPriceInfoResponse(String message, String baseResponse) {
        String productName = extractSearchTerm(message);

        for (Product product : cachedProducts) {
            if (product.getName().toLowerCase().contains(productName.toLowerCase())) {
                String priceInfo = "💰 " + product.getName() + ": " + product.getRealPrice() + "đ";
                if (product.getSale() > 0) {
                    priceInfo += " (Giảm " + product.getSale() + "% từ " + product.getPrice() + "đ)";
                }
                return baseResponse + "\n\n" + priceInfo;
            }
        }

        return baseResponse + "\n\nTôi không tìm thấy thông tin giá cho món này. Bạn có thể cho tôi biết tên chính xác của món ăn không?";
    }

    private String extractSearchTerm(String message) {
        // Simple extraction - in real app, you might use more sophisticated NLP
        String[] commonWords = {"tìm", "kiếm", "có", "món", "gì", "nào", "giá", "bao", "nhiêu"};
        String[] words = message.toLowerCase().split("\\s+");

        StringBuilder searchTerm = new StringBuilder();
        for (String word : words) {
            boolean isCommonWord = false;
            for (String common : commonWords) {
                if (word.equals(common)) {
                    isCommonWord = true;
                    break;
                }
            }
            if (!isCommonWord && word.length() > 1) {
                if (searchTerm.length() > 0) searchTerm.append(" ");
                searchTerm.append(word);
            }
        }

        return searchTerm.toString().trim();
    }

    private void setupFallbackBot() {
        Log.d(TAG, "Using fallback bot");
        // Original simple bot logic as fallback
    }

    private void simulateBotResponse(String userMessage) {
        showTypingIndicator();

        binding.getRoot().postDelayed(() -> {
            hideTypingIndicator();
            String botResponse = generateFallbackResponse(userMessage);
            addBotMessage(botResponse);
        }, 1500);
    }

    private String generateFallbackResponse(String userMessage) {
        // Your original response generation logic
        String message = userMessage.toLowerCase();

        if (message.contains("gà") || message.contains("chicken")) {
            return "Chúng tôi có nhiều món gà rán thơm ngon như:\n" +
                    "🍗 Gà rán giòn\n" +
                    "🍗 Gà rán cay\n" +
                    "🍗 Gà rán mật ong\n\n" +
                    "Bạn muốn xem chi tiết món nào không ạ?";
        }
        // ... rest of your original logic

        return "Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn. 😅\n\n" +
                "Bạn có thể hỏi tôi về món ăn, giá cả, hoặc thông tin cửa hàng!";
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
        if (sessionsClient != null) {
            sessionsClient.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        binding = null;
    }
}