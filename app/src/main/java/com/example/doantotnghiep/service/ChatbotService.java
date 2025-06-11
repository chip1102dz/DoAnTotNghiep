package com.example.doantotnghiep.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.StoreLocation;
import com.example.doantotnghiep.model.Category;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ChatbotService {

    private static final String TAG = "ChatbotService";
    // Sửa Project ID đúng theo file credentials
    private static final String PROJECT_ID = "doantotnghieppro-d2186";
    private static final String LANGUAGE_CODE = "vi";
    private static final long DIALOGFLOW_TIMEOUT = 10000; // 10 seconds

    private Context context;
    private SessionsClient sessionsClient;
    private SessionName sessionName;

    // Data cache
    private List<Product> cachedProducts;
    private List<Category> cachedCategories;
    private StoreLocation storeLocation;
    private Map<String, Object> conversationContext;
    private boolean isDataLoaded = false;
    private boolean isDialogflowEnabled = false;

    // Listeners
    public interface ChatbotResponseListener {
        void onResponse(String response);
        void onError(String error);
    }

    public interface DataLoadListener {
        void onDataLoaded();
        void onDataError(String error);
    }

    public ChatbotService(Context context) {
        this.context = context;
        this.cachedProducts = new ArrayList<>();
        this.cachedCategories = new ArrayList<>();
        this.conversationContext = new HashMap<>();

        initializeDialogflow();
        loadFirebaseData(null);
    }


    private void initializeDialogflow() {
        try {
            // Thử đọc file credentials (có thể là .json hoặc .json.json)
            InputStream stream = null;
            try {
                stream = context.getAssets().open("dialogflow_credentials.json");
            } catch (Exception e) {
                try {
                    stream = context.getAssets().open("dialogflow_credentials.json.json");
                } catch (Exception e2) {
                    Log.e(TAG, "Cannot find credentials file: " + e2.getMessage());
                    return;
                }
            }

            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(stream);

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(PROJECT_ID, UUID.randomUUID().toString());

            isDialogflowEnabled = true;
            Log.d(TAG, "Dialogflow initialized successfully with project: " + PROJECT_ID);

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Dialogflow: " + e.getMessage());
            e.printStackTrace();
            isDialogflowEnabled = false;
        }
    }

    public void loadFirebaseData(DataLoadListener listener) {
        final int[] loadedCount = {0};
        final int totalLoads = 3; // products, categories, store location

        // Load products
        MyApplication.get(context).getProductDatabaseReference()
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
                        checkDataLoadComplete(++loadedCount[0], totalLoads, listener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load products: " + error.getMessage());
                        if (listener != null) {
                            listener.onDataError("Failed to load products: " + error.getMessage());
                        }
                    }
                });

        // Load categories
        MyApplication.get(context).getCategoryDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cachedCategories.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category != null) {
                                cachedCategories.add(category);
                            }
                        }
                        Log.d(TAG, "Loaded " + cachedCategories.size() + " categories");
                        checkDataLoadComplete(++loadedCount[0], totalLoads, listener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load categories: " + error.getMessage());
                        if (listener != null) {
                            listener.onDataError("Failed to load categories: " + error.getMessage());
                        }
                    }
                });

        // Load store location
        MyApplication.get(context).getStoreLocationDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        storeLocation = snapshot.getValue(StoreLocation.class);
                        Log.d(TAG, "Store location loaded");
                        checkDataLoadComplete(++loadedCount[0], totalLoads, listener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load store location: " + error.getMessage());
                        if (listener != null) {
                            listener.onDataError("Failed to load store location: " + error.getMessage());
                        }
                    }
                });
    }

    private void checkDataLoadComplete(int loadedCount, int totalLoads, DataLoadListener listener) {
        if (loadedCount >= totalLoads) {
            isDataLoaded = true;
            Log.d(TAG, "All Firebase data loaded successfully");
            if (listener != null) {
                listener.onDataLoaded();
            }
        }
    }

    public void sendMessage(String message, ChatbotResponseListener listener) {
        Log.d(TAG, "Sending message: " + message);
        Log.d(TAG, "Dialogflow enabled: " + isDialogflowEnabled);
        Log.d(TAG, "Data loaded: " + isDataLoaded);

        if (isDialogflowEnabled && sessionsClient != null) {
            sendToDialogflow(message, listener);
        } else {
            // Fallback to simple bot
            Log.d(TAG, "Using fallback simple bot");
            String response = generateSimpleResponse(message);
            listener.onResponse(response);
        }
    }

    private void sendToDialogflow(String message, ChatbotResponseListener listener) {
        try {
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(message)
                    .setLanguageCode(LANGUAGE_CODE);

            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(textInput)
                    .build();

            DetectIntentRequest.Builder requestBuilder = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput);

            // Thực hiện request trong background thread
            new Thread(() -> {
                try {
                    DetectIntentResponse response = sessionsClient.detectIntent(requestBuilder.build());
                    QueryResult queryResult = response.getQueryResult();

                    String enhancedResponse = processDialogflowResponse(queryResult, message);

                    // Trả về main thread
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> listener.onResponse(enhancedResponse));

                } catch (Exception e) {
                    Log.e(TAG, "Dialogflow error: " + e.getMessage());
                    e.printStackTrace();

                    // Fallback to simple response
                    String fallbackResponse = generateSimpleResponse(message);
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> listener.onResponse("⚠️ Kết nối không ổn định, tôi sẽ cố gắng trả lời:\n\n" + fallbackResponse));
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Error creating Dialogflow request: " + e.getMessage());
            String fallbackResponse = generateSimpleResponse(message);
            listener.onResponse(fallbackResponse);
        }
    }

    private String processDialogflowResponse(QueryResult queryResult, String originalMessage) {
        String intent = queryResult.getIntent().getDisplayName();
        String fulfillmentText = queryResult.getFulfillmentText();

        Log.d(TAG, "Intent: " + intent);
        Log.d(TAG, "Fulfillment: " + fulfillmentText);

        // Update conversation context
        updateConversationContext(intent, originalMessage);

        // Enhance response with Firebase data
        return enhanceResponse(intent, fulfillmentText, originalMessage);
    }

    private void updateConversationContext(String intent, String message) {
        conversationContext.put("last_intent", intent);
        conversationContext.put("last_message", message);
        conversationContext.put("timestamp", System.currentTimeMillis());

        // Save specific context based on intent
        switch (intent) {
            case "product.search":
                conversationContext.put("searching_products", true);
                break;
            case "product.details":
                String productName = extractProductName(message);
                if (productName != null && !productName.isEmpty()) {
                    conversationContext.put("current_product", productName);
                }
                break;
        }
    }

    private String enhanceResponse(String intent, String fulfillmentText, String originalMessage) {
        if (!isDataLoaded) {
            return fulfillmentText + "\n\n⏳ Đang tải dữ liệu, vui lòng thử lại sau ít phút.";
        }

        switch (intent) {
            case "product.search":
            case "product.list":
                return handleProductSearch(fulfillmentText, originalMessage);

            case "product.details":
                return handleProductDetails(fulfillmentText, originalMessage);

            case "store.info":
            case "store.location":
                return handleStoreInfo(fulfillmentText);

            case "menu.recommendation":
                return handleMenuRecommendation(fulfillmentText);

            case "price.inquiry":
                return handlePriceInquiry(fulfillmentText, originalMessage);

            case "category.list":
                return handleCategoryList(fulfillmentText);

            case "promotion.info":
                return handlePromotionInfo(fulfillmentText);

            case "order.help":
                return handleOrderHelp(fulfillmentText);

            default:
                return fulfillmentText.isEmpty() ? generateSimpleResponse(originalMessage) : fulfillmentText;
        }
    }

    // Các method handle khác giữ nguyên như code cũ...
    private String handleProductSearch(String baseResponse, String message) {
        String searchTerm = extractSearchTerm(message);
        List<Product> foundProducts = searchProducts(searchTerm);

        if (foundProducts.isEmpty()) {
            return baseResponse + "\n\n❌ Không tìm thấy sản phẩm phù hợp với \"" + searchTerm + "\".\n" +
                    "Bạn có thể thử:\n• Tìm theo danh mục: " + getCategoryNames() + "\n" +
                    "• Hoặc hỏi \"menu có gì?\" để xem tất cả món";
        }

        StringBuilder response = new StringBuilder();
        if (!baseResponse.isEmpty()) {
            response.append(baseResponse).append("\n\n");
        }
        response.append("🔍 Tìm thấy ").append(foundProducts.size()).append(" sản phẩm:\n\n");

        for (int i = 0; i < Math.min(5, foundProducts.size()); i++) {
            Product product = foundProducts.get(i);
            response.append("🍽️ **").append(product.getName()).append("**\n");
            response.append("   💰 ").append(formatPrice(product)).append("\n");
            response.append("   ⭐ ").append(product.getRate()).append("/5\n");
            if (i < Math.min(4, foundProducts.size() - 1)) {
                response.append("\n");
            }
        }

        if (foundProducts.size() > 5) {
            response.append("\n... và ").append(foundProducts.size() - 5).append(" món khác");
        }

        response.append("\n\n💬 Bạn muốn biết thêm về món nào không?");
        return response.toString();
    }

    // Thêm các helper methods
    private List<Product> searchProducts(String searchTerm) {
        List<Product> results = new ArrayList<>();
        for (Product product : cachedProducts) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (product.getCategory_name() != null &&
                            product.getCategory_name().toLowerCase().contains(searchTerm.toLowerCase()))) {
                results.add(product);
            }
        }

        // Sort by rating
        results.sort((p1, p2) -> Double.compare(p2.getRate(), p1.getRate()));
        return results;
    }

    private String extractSearchTerm(String message) {
        String[] commonWords = {
                "tìm", "kiếm", "có", "món", "gì", "nào", "giá", "bao", "nhiêu",
                "cho", "tôi", "mình", "xem", "của", "và", "hoặc", "với", "không",
                "được", "là", "thì", "sao", "như", "thế", "nào", "ở", "đây"
        };

        String[] words = message.toLowerCase()
                .replaceAll("[^a-zA-ZÀ-ỹ\\s]", "")
                .split("\\s+");

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

    private String extractProductName(String message) {
        String cleanMessage = message.toLowerCase();

        for (Product product : cachedProducts) {
            String productName = product.getName().toLowerCase();
            if (cleanMessage.contains(productName)) {
                return product.getName();
            }

            String[] productWords = productName.split("\\s+");
            for (String word : productWords) {
                if (word.length() > 2 && cleanMessage.contains(word)) {
                    return product.getName();
                }
            }
        }

        return extractSearchTerm(message);
    }

    private String getCategoryNames() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < Math.min(5, cachedCategories.size()); i++) {
            if (i > 0) names.append(", ");
            names.append(cachedCategories.get(i).getName());
        }
        return names.length() > 0 ? names.toString() : "Gà rán, Pizza, Phở, Trà sữa";
    }

    private String formatPrice(Product product) {
        return formatPrice(product.getRealPrice());
    }

    private String formatPrice(int price) {
        return String.format("%,d", price) + "đ";
    }

    // Các method handle khác (handleProductDetails, handleStoreInfo, etc.)
    // giữ nguyên như code gốc nhưng cần implement đầy đủ...

    private String handleProductDetails(String baseResponse, String message) {
        // Implementation tương tự code gốc
        return baseResponse + "\n\n🍽️ Chi tiết sản phẩm đang được cập nhật...";
    }

    private String handleStoreInfo(String baseResponse) {
        if (storeLocation == null) {
            return baseResponse + "\n\n⏳ Đang cập nhật thông tin cửa hàng...";
        }

        return baseResponse + "\n\n" +
                "📍 **Địa chỉ:** " + storeLocation.getAddress() + "\n" +
                "📞 **Điện thoại:** " + storeLocation.getPhone() + "\n" +
                "🕒 **Giờ mở cửa:** 8:00 - 22:00 hàng ngày\n" +
                "🚚 **Giao hàng:** Miễn phí trong bán kính 5km";
    }

    private String handleMenuRecommendation(String baseResponse) {
        // Implementation
        return baseResponse + "\n\n⭐ Đang tải menu đặc biệt...";
    }

    private String handlePriceInquiry(String baseResponse, String originalMessage) {
        // Implementation
        return baseResponse + "\n\n💰 Đang cập nhật bảng giá...";
    }

    private String handleCategoryList(String baseResponse) {
        // Implementation
        return baseResponse + "\n\n📂 Đang tải danh mục...";
    }

    private String handlePromotionInfo(String baseResponse) {
        // Implementation
        return baseResponse + "\n\n🔥 Đang cập nhật khuyến mãi...";
    }

    private String handleOrderHelp(String baseResponse) {
        return baseResponse + "\n\n📱 **Cách đặt hàng dễ dàng:**\n\n" +
                "1️⃣ Chọn món trong menu\n" +
                "2️⃣ Thêm vào giỏ hàng\n" +
                "3️⃣ Điền thông tin giao hàng\n" +
                "4️⃣ Chọn phương thức thanh toán\n" +
                "5️⃣ Xác nhận đặt hàng\n\n" +
                "🚚 **Thời gian giao hàng:** 15-30 phút";
    }

    private String generateSimpleResponse(String message) {
        message = message.toLowerCase();

        // Product search patterns
        if (message.contains("gà") || message.contains("chicken")) {
            return "🍗 **Món gà rán của chúng tôi:**\n\n" +
                    "• Gà rán giòn - 65.000đ\n" +
                    "• Gà rán cay - 70.000đ\n" +
                    "• Gà rán mật ong - 75.000đ\n\n" +
                    "💬 Bạn muốn biết thêm về món nào không?";
        }

        if (message.contains("pizza")) {
            return "🍕 **Pizza đặc biệt:**\n\n" +
                    "• Pizza Margherita - 89.000đ\n" +
                    "• Pizza Pepperoni - 129.000đ\n" +
                    "• Pizza Hải sản - 159.000đ\n\n" +
                    "📐 Có size S, M, L để bạn lựa chọn!";
        }

        if (message.contains("phở")) {
            return "🍜 **Phở truyền thống:**\n\n" +
                    "• Phở bò tái - 45.000đ\n" +
                    "• Phở bò chín - 45.000đ\n" +
                    "• Phở gà - 40.000đ\n\n" +
                    "🔥 Nước dùng hầm xương 12 tiếng!";
        }

        if (message.contains("trà sữa") || message.contains("bubble tea")) {
            return "🧋 **Trà sữa đa dạng:**\n\n" +
                    "• Trà sữa trân châu - 25.000đ\n" +
                    "• Trà sữa matcha - 30.000đ\n" +
                    "• Trà sữa taro - 28.000đ\n\n" +
                    "🧊 Có thể chọn độ đường và đá theo ý thích!";
        }

        // Price inquiries
        if (message.contains("giá") || message.contains("price") || message.contains("bao nhiêu")) {
            return "💰 **Bảng giá tham khảo:**\n\n" +
                    "🍗 Gà rán: 65.000 - 75.000đ\n" +
                    "🍕 Pizza: 89.000 - 159.000đ\n" +
                    "🍜 Phở: 40.000 - 45.000đ\n" +
                    "🧋 Trà sữa: 25.000 - 35.000đ\n\n" +
                    "🎯 Có nhiều ưu đãi hấp dẫn đang chờ bạn!";
        }

        // Store info
        if (message.contains("địa chỉ") || message.contains("cửa hàng") || message.contains("ở đâu")) {
            return "📍 **Thông tin cửa hàng:**\n\n" +
                    "🏪 Địa chỉ: 123 Đường ABC, Quận XYZ\n" +
                    "📞 Hotline: 1900-1234\n" +
                    "🕒 Giờ mở cửa: 8:00 - 22:00\n" +
                    "🚚 Giao hàng: Miễn phí trong 5km";
        }

        // Menu/catalog requests
        if (message.contains("menu") || message.contains("món ăn") || message.contains("có gì")) {
            return "📖 **Menu đa dạng:**\n\n" +
                    "🍗 **Gà rán** - Giòn tan, thơm ngon\n" +
                    "🍕 **Pizza** - Bánh mỏng, nhân đầy đặn\n" +
                    "🍜 **Phở** - Truyền thống Việt Nam\n" +
                    "🧋 **Trà sữa** - Tươi mát, đa vị\n\n" +
                    "💬 Bạn muốn xem chi tiết danh mục nào?";
        }

        // Greetings
        if (message.contains("hello") || message.contains("hi") || message.contains("chào")) {
            return "👋 **Xin chào!** Chào mừng bạn đến với cửa hàng!\n\n" +
                    "Tôi có thể giúp bạn:\n" +
                    "🔍 Tìm kiếm món ăn\n" +
                    "💰 Xem giá cả\n" +
                    "📍 Thông tin cửa hàng\n" +
                    "🛒 Hướng dẫn đặt hàng\n\n" +
                    "💬 Bạn cần hỗ trợ gì ạ?";
        }

        // Default response
        return "💭 **Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn.**\n\n" +
                "Bạn có thể hỏi tôi về:\n" +
                "🍽️ Menu và món ăn\n" +
                "💰 Giá cả\n" +
                "📍 Thông tin cửa hàng\n" +
                "🛒 Cách đặt hàng\n\n" +
                "💡 **Ví dụ:** \"Menu có món gì?\", \"Giá pizza bao nhiêu?\", \"Cửa hàng ở đâu?\"";
    }

    // Cleanup methods
    public void cleanup() {
        if (sessionsClient != null) {
            try {
                sessionsClient.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing Dialogflow client: " + e.getMessage());
            }
        }
        cachedProducts.clear();
        cachedCategories.clear();
        conversationContext.clear();
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public boolean isDialogflowEnabled() {
        return isDialogflowEnabled;
    }
}