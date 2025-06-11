package com.example.doantotnghiep.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.StoreLocation;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.Order;
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
    private static final String PROJECT_ID = "your-dialogflow-project-id";
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
            InputStream stream = context.getAssets().open("dialogflow_credentials.json");
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(stream);

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(PROJECT_ID, UUID.randomUUID().toString());

            Log.d(TAG, "Dialogflow initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Dialogflow: " + e.getMessage());
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
            if (listener != null) {
                listener.onDataLoaded();
            }
        }
    }

    public void sendMessage(String message, ChatbotResponseListener listener) {
        if (sessionsClient != null) {
            sendToDialogflow(message, listener);
        } else {
            // Fallback to simple bot
            String response = generateSimpleResponse(message);
            listener.onResponse(response);
        }
    }

    private void sendToDialogflow(String message, ChatbotResponseListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CompletableFuture.supplyAsync(() -> {
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

                            // Add contexts if any
                            if (!conversationContext.isEmpty()) {
                                // Add contexts to request
                            }

                            DetectIntentResponse response = sessionsClient.detectIntent(requestBuilder.build());
                            return response.getQueryResult();

                        } catch (Exception e) {
                            Log.e(TAG, "Dialogflow error: " + e.getMessage());
                            return null;
                        }
                    }).thenAccept(queryResult -> {
                        if (queryResult != null) {
                            String enhancedResponse = processDialogflowResponse(queryResult, message);
                            listener.onResponse(enhancedResponse);
                        } else {
                            String fallbackResponse = generateSimpleResponse(message);
                            listener.onResponse("⚠️ Kết nối không ổn định, tôi sẽ cố gắng trả lời:\n\n" + fallbackResponse);
                        }
                    }).orTimeout(DIALOGFLOW_TIMEOUT, TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Dialogflow timeout or error: " + throwable.getMessage());
                        String fallbackResponse = generateSimpleResponse(message);
                        listener.onResponse("⚠️ Phản hồi chậm, đây là câu trả lời nhanh:\n\n" + fallbackResponse);
                        return null;
                    });
        }
    }

    private String processDialogflowResponse(QueryResult queryResult, String originalMessage) {
        String intent = queryResult.getIntent().getDisplayName();
        String fulfillmentText = queryResult.getFulfillmentText();
        Map<String, com.google.protobuf.Value> parameters = queryResult.getParameters().getFieldsMap();

        Log.d(TAG, "Intent: " + intent);
        Log.d(TAG, "Fulfillment: " + fulfillmentText);

        // Update conversation context
        updateConversationContext(intent, parameters, originalMessage);

        // Enhance response with Firebase data
        return enhanceResponse(intent, fulfillmentText, originalMessage, parameters);
    }

    private void updateConversationContext(String intent, Map<String, com.google.protobuf.Value> parameters, String message) {
        conversationContext.put("last_intent", intent);
        conversationContext.put("last_message", message);
        conversationContext.put("timestamp", System.currentTimeMillis());

        // Save specific context based on intent
        switch (intent) {
            case "product.search":
                conversationContext.put("searching_products", true);
                break;
            case "product.details":
                // Extract product name from parameters or message
                String productName = extractProductName(message);
                if (productName != null) {
                    conversationContext.put("current_product", productName);
                }
                break;
        }
    }

    private String enhanceResponse(String intent, String fulfillmentText, String originalMessage,
                                   Map<String, com.google.protobuf.Value> parameters) {
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

    private String handleProductSearch(String baseResponse, String message) {
        String searchTerm = extractSearchTerm(message);
        List<Product> foundProducts = searchProducts(searchTerm);

        if (foundProducts.isEmpty()) {
            return baseResponse + "\n\n❌ Không tìm thấy sản phẩm phù hợp với \"" + searchTerm + "\".\n" +
                    "Bạn có thể thử:\n• Tìm theo danh mục: " + getCategoryNames() + "\n" +
                    "• Hoặc hỏi \"menu có gì?\" để xem tất cả món";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\n🔍 Tìm thấy " + foundProducts.size() + " sản phẩm:\n\n");

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

    private String handleProductDetails(String baseResponse, String message) {
        String productName = extractProductName(message);
        Product product = findProductByName(productName);

        if (product == null) {
            return baseResponse + "\n\n❌ Không tìm thấy thông tin chi tiết cho món này. " +
                    "Bạn có thể hỏi \"menu có gì?\" để xem danh sách đầy đủ.";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\n");
        response.append("🍽️ **").append(product.getName()).append("**\n\n");
        response.append("📝 ").append(product.getDescription()).append("\n\n");
        response.append("💰 **Giá:** ").append(formatPrice(product)).append("\n");
        response.append("⭐ **Đánh giá:** ").append(product.getRate()).append("/5 (").append(product.getCountReviews()).append(" reviews)\n");

        if (product.isFeatured()) {
            response.append("🌟 **Món đặc biệt**\n");
        }

        if (product.getInfo() != null && !product.getInfo().isEmpty()) {
            response.append("ℹ️ **Thông tin thêm:** ").append(product.getInfo()).append("\n");
        }

        response.append("\n🛒 Bạn có muốn thêm vào giỏ hàng không?");

        // Save to context for potential follow-up
        conversationContext.put("current_product_detail", product.getName());

        return response.toString();
    }

    private String handleStoreInfo(String baseResponse) {
        if (storeLocation == null) {
            return baseResponse + "\n\n⏳ Đang cập nhật thông tin cửa hàng...";
        }

        return baseResponse + "\n\n" +
                "📍 **Địa chỉ:** " + storeLocation.getAddress() + "\n" +
                "📞 **Điện thoại:** " + storeLocation.getPhone() + "\n" +
                "🕒 **Giờ mở cửa:** 8:00 - 22:00 hàng ngày\n" +
                "🚚 **Giao hàng:** Miễn phí trong bán kính 5km\n\n" +
                "🗺️ Bạn có thể xem vị trí chính xác trong mục \"Vị trí cửa hàng\" của app!";
    }

    private String handleMenuRecommendation(String baseResponse) {
        List<Product> recommendations = getRecommendations();

        if (recommendations.isEmpty()) {
            return baseResponse + "\n\n⏳ Đang cập nhật menu...";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\n⭐ **Top món được yêu thích:**\n\n");

        for (int i = 0; i < recommendations.size(); i++) {
            Product product = recommendations.get(i);
            response.append(i + 1).append(". **").append(product.getName()).append("**\n");
            response.append("   💰 ").append(formatPrice(product));
            response.append(" | ⭐ ").append(product.getRate()).append("/5\n");
            if (product.isFeatured()) {
                response.append("   🌟 Món đặc biệt\n");
            }
            response.append("\n");
        }

        response.append("💬 Bạn muốn biết thêm về món nào không?");
        return response.toString();
    }

    private String handlePriceInquiry(String baseResponse, String message) {
        String productName = extractProductName(message);

        if (productName.isEmpty()) {
            // Show price range
            if (cachedProducts.isEmpty()) {
                return baseResponse + "\n\n⏳ Đang tải bảng giá...";
            }

            int minPrice = cachedProducts.stream().mapToInt(Product::getRealPrice).min().orElse(0);
            int maxPrice = cachedProducts.stream().mapToInt(Product::getRealPrice).max().orElse(0);

            return baseResponse + "\n\n💰 **Khoảng giá menu:**\n" +
                    "Từ " + formatPrice(minPrice) + " đến " + formatPrice(maxPrice) + "\n\n" +
                    "💬 Bạn muốn biết giá món cụ thể nào không?";
        }

        Product product = findProductByName(productName);
        if (product == null) {
            return baseResponse + "\n\n❌ Không tìm thấy giá cho \"" + productName + "\".\n" +
                    "💬 Bạn có thể hỏi \"menu có gì?\" để xem danh sách đầy đủ.";
        }

        String priceInfo = "💰 **" + product.getName() + ":** " + formatPrice(product);
        if (product.getSale() > 0) {
            priceInfo += " 🔥 (Giảm " + product.getSale() + "% từ " + formatPrice(product.getPrice()) + ")";
        }

        return baseResponse + "\n\n" + priceInfo;
    }

    private String handleCategoryList(String baseResponse) {
        if (cachedCategories.isEmpty()) {
            return baseResponse + "\n\n⏳ Đang tải danh mục...";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\n📂 **Danh mục món ăn:**\n\n");
        for (Category category : cachedCategories) {
            long productCount = cachedProducts.stream()
                    .filter(p -> p.getCategory_id() == category.getId())
                    .count();
            response.append("• ").append(category.getName())
                    .append(" (").append(productCount).append(" món)\n");
        }

        response.append("\n💬 Bạn muốn xem món trong danh mục nào?");
        return response.toString();
    }

    private String handlePromotionInfo(String baseResponse) {
        List<Product> promotionalProducts = cachedProducts.stream()
                .filter(p -> p.getSale() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getSale(), p1.getSale()))
                .limit(5)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (promotionalProducts.isEmpty()) {
            return baseResponse + "\n\n📢 Hiện tại chưa có khuyến mãi đặc biệt nào.\n" +
                    "Hãy theo dõi app để cập nhật ưu đãi mới nhất!";
        }

        StringBuilder response = new StringBuilder(baseResponse + "\n\n🔥 **Khuyến mãi HOT:**\n\n");
        for (Product product : promotionalProducts) {
            response.append("🎯 **").append(product.getName()).append("**\n");
            response.append("   💸 ").append(formatPrice(product.getRealPrice()))
                    .append(" (Giảm ").append(product.getSale()).append("% từ ")
                    .append(formatPrice(product.getPrice())).append(")\n\n");
        }

        response.append("⏰ Nhanh tay đặt hàng để nhận ưu đãi!");
        return response.toString();
    }

    private String handleOrderHelp(String baseResponse) {
        return baseResponse + "\n\n📱 **Cách đặt hàng dễ dàng:**\n\n" +
                "1️⃣ Chọn món trong menu\n" +
                "2️⃣ Thêm vào giỏ hàng\n" +
                "3️⃣ Điền thông tin giao hàng\n" +
                "4️⃣ Chọn phương thức thanh toán\n" +
                "5️⃣ Xác nhận đặt hàng\n\n" +
                "🚚 **Thời gian giao hàng:** 15-30 phút\n" +
                "💳 **Thanh toán:** Tiền mặt, ZaloPay\n" +
                "📞 **Hỗ trợ:** " + (storeLocation != null ? storeLocation.getPhone() : "Hotline") + "\n\n" +
                "💬 Tôi có thể giúp bạn tìm món ăn phù hợp không?";
    }

    // Helper methods
    private List<Product> searchProducts(String searchTerm) {
        return cachedProducts.stream()
                .filter(product ->
                        product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                (product.getCategory_name() != null &&
                                        product.getCategory_name().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .sorted((p1, p2) -> {
                    // Prioritize exact name matches
                    boolean p1NameMatch = p1.getName().toLowerCase().contains(searchTerm.toLowerCase());
                    boolean p2NameMatch = p2.getName().toLowerCase().contains(searchTerm.toLowerCase());
                    if (p1NameMatch && !p2NameMatch) return -1;
                    if (p2NameMatch && !p1NameMatch) return 1;
                    // Then by rating
                    return Double.compare(p2.getRate(), p1.getRate());
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private Product findProductByName(String productName) {
        return cachedProducts.stream()
                .filter(product ->
                        product.getName().toLowerCase().contains(productName.toLowerCase())
                )
                .findFirst()
                .orElse(null);
    }

    private List<Product> getRecommendations() {
        // Get featured products first
        List<Product> featured = cachedProducts.stream()
                .filter(Product::isFeatured)
                .sorted((p1, p2) -> Double.compare(p2.getRate(), p1.getRate()))
                .limit(3)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (featured.size() >= 3) {
            return featured;
        }

        // If not enough featured products, get top rated
        return cachedProducts.stream()
                .sorted((p1, p2) -> Double.compare(p2.getRate(), p1.getRate()))
                .limit(5)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private String extractSearchTerm(String message) {
        // Remove common Vietnamese words
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
        // Try to find product name in the message
        String cleanMessage = message.toLowerCase();

        // Look for products by name
        for (Product product : cachedProducts) {
            String productName = product.getName().toLowerCase();
            if (cleanMessage.contains(productName)) {
                return product.getName();
            }

            // Check individual words
            String[] productWords = productName.split("\\s+");
            for (String word : productWords) {
                if (word.length() > 2 && cleanMessage.contains(word)) {
                    return product.getName();
                }
            }
        }

        // Fallback to search term extraction
        return extractSearchTerm(message);
    }

    private String getCategoryNames() {
        return cachedCategories.stream()
                .map(Category::getName)
                .limit(5)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Gà rán, Pizza, Phở, Trà sữa");
    }

    private String formatPrice(Product product) {
        return formatPrice(product.getRealPrice());
    }

    private String formatPrice(int price) {
        return String.format("%,d", price) + "đ";
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
                    "🚚 Giao hàng: Miễn phí trong 5km\n\n" +
                    "⏰ Thời gian giao hàng: 15-30 phút";
        }

        // Promotions
        if (message.contains("khuyến mãi") || message.contains("giảm giá") || message.contains("ưu đãi")) {
            return "🔥 **Khuyến mãi HOT:**\n\n" +
                    "🎯 Giảm 20% cho đơn hàng trên 200k\n" +
                    "🎁 Mua 2 tặng 1 cho trà sữa\n" +
                    "🚚 Miễn phí ship trong bán kính 5km\n" +
                    "⏰ Giảm 15% cho đơn hàng sau 20h\n\n" +
                    "💨 Nhanh tay đặt hàng để nhận ưu đãi!";
        }

        // Menu/catalog requests
        if (message.contains("menu") || message.contains("món ăn") || message.contains("có gì")) {
            return "📖 **Menu đa dạng:**\n\n" +
                    "🍗 **Gà rán** - Giòn tan, thơm ngon\n" +
                    "🍕 **Pizza** - Bánh mỏng, nhân đầy đặn\n" +
                    "🍜 **Phở** - Truyền thống Việt Nam\n" +
                    "🧋 **Trà sữa** - Tươi mát, đa vị\n" +
                    "🍚 **Cơm** - Cơm rang, cơm chiên\n" +
                    "🍰 **Tráng miệng** - Kem, bánh ngọt\n\n" +
                    "💬 Bạn muốn xem chi tiết danh mục nào?";
        }

        // Order help
        if (message.contains("đặt hàng") || message.contains("order") || message.contains("mua")) {
            return "📱 **Hướng dẫn đặt hàng:**\n\n" +
                    "1️⃣ Chọn món yêu thích\n" +
                    "2️⃣ Thêm vào giỏ hàng\n" +
                    "3️⃣ Điền địa chỉ giao hàng\n" +
                    "4️⃣ Chọn thanh toán\n" +
                    "5️⃣ Xác nhận đặt hàng\n\n" +
                    "💳 Thanh toán: Tiền mặt, ZaloPay\n" +
                    "🚚 Giao hàng: 15-30 phút";
        }

        // Greetings
        if (message.contains("hello") || message.contains("hi") || message.contains("chào")) {
            return "👋 **Xin chào!** Chào mừng bạn đến với cửa hàng!\n\n" +
                    "Tôi có thể giúp bạn:\n" +
                    "🔍 Tìm kiếm món ăn\n" +
                    "💰 Xem giá cả\n" +
                    "📍 Thông tin cửa hàng\n" +
                    "🛒 Hướng dẫn đặt hàng\n" +
                    "🎯 Khuyến mãi hiện tại\n\n" +
                    "💬 Bạn cần hỗ trợ gì ạ?";
        }

        // Thanks
        if (message.contains("cảm ơn") || message.contains("thanks") || message.contains("thank")) {
            return "🙏 **Cảm ơn bạn rất nhiều!**\n\n" +
                    "Rất vui được hỗ trợ bạn! Nếu cần thêm thông tin gì, đừng ngại hỏi nhé.\n\n" +
                    "🌟 Chúc bạn có trải nghiệm tuyệt vời với món ăn của chúng tôi! 😊";
        }

        // Default response
        return "💭 **Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn.**\n\n" +
                "Bạn có thể hỏi tôi về:\n" +
                "🍽️ Menu và món ăn\n" +
                "💰 Giá cả\n" +
                "🎯 Khuyến mãi\n" +
                "📍 Thông tin cửa hàng\n" +
                "🛒 Cách đặt hàng\n\n" +
                "💡 **Ví dụ:** \"Menu có món gì?\", \"Giá pizza bao nhiêu?\", \"Cửa hàng ở đâu?\"";
    }

    // Conversation context methods
    public void setContext(String key, Object value) {
        conversationContext.put(key, value);
    }

    public Object getContext(String key) {
        return conversationContext.get(key);
    }

    public void clearContext() {
        conversationContext.clear();
    }

    // Data getters
    public List<Product> getCachedProducts() {
        return new ArrayList<>(cachedProducts);
    }

    public List<Category> getCachedCategories() {
        return new ArrayList<>(cachedCategories);
    }

    public StoreLocation getStoreLocation() {
        return storeLocation;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    // Cleanup
    public void cleanup() {
        if (sessionsClient != null) {
            sessionsClient.close();
        }
        cachedProducts.clear();
        cachedCategories.clear();
        conversationContext.clear();
    }

    // Advanced features
    public void enableSmartRecommendations(String userId) {
        // TODO: Implement user-based recommendations using order history
        // This could analyze user's past orders from Firebase to suggest personalized recommendations
    }

    public void logConversation(String userMessage, String botResponse, String intent) {
        // TODO: Log conversation for analytics
        // This could help improve the chatbot by analyzing common queries and response quality
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", System.currentTimeMillis());
        logData.put("user_message", userMessage);
        logData.put("bot_response", botResponse);
        logData.put("intent", intent);
        logData.put("session_id", sessionName != null ? sessionName.toString() : "fallback");

        // Log to Firebase Analytics or custom logging system
        Log.d(TAG, "Conversation logged: " + logData.toString());
    }
}