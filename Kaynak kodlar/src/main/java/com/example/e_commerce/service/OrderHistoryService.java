package com.example.e_commerce.service;

import com.example.e_commerce.entity.OrderHistory;
import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.repository.OrderHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderHistoryService {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private UserInteractionService userInteractionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    public OrderHistory saveOrder(String customerName, String deliveryAddress, Cart cart, BigDecimal totalAmount, Long userId) {
        try {
            OrderHistory orderHistory = new OrderHistory();

            // Sipariş numarası oluştur
            String orderNumber = "ORD-" + System.currentTimeMillis();
            orderHistory.setOrderNumber(orderNumber);

            orderHistory.setCustomerName(customerName);
            orderHistory.setDeliveryAddress(deliveryAddress);
            orderHistory.setOrderDate(LocalDateTime.now());
            orderHistory.setTotalAmount(totalAmount);
            orderHistory.setUserId(userId);

            // Cart items'ları JSON formatına çevir
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> orderItems = new ArrayList<>();

            for (CartItem item : cart.getCartItems()) {
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("productName", item.getProduct().getProductName());
                orderItem.put("quantity", item.getQuantity());
                orderItem.put("unitPrice", item.getProductPrice());
                orderItem.put("totalPrice", item.getTotalPrice());
                orderItems.add(orderItem);

                // UserInteraction kaydını her ürün için ekle
                userInteractionService.recordProductPurchase(userId, item.getProduct().getId(), item.getTotalPrice());  // Kaydettik
            }

            String orderItemsJson = mapper.writeValueAsString(orderItems);
            orderHistory.setOrderItems(orderItemsJson);

            OrderHistory savedOrder = orderHistoryRepository.save(orderHistory);
            
            // Sipariş kaydedildikten sonra kullanıcının toplam satın alma tutarını güncelle
            userService.addToPurchaseAmount(userService.findById(userId).get().getUsername(), totalAmount);

            return savedOrder;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Sipariş kaydedilemedi: " + e.getMessage());
        }
    }

    // BU METODU EKLEYİN - CheckoutController'da kullanılıyor
    public List<OrderHistory> getOrdersByUsername(String username) {
        return orderHistoryRepository.findByCustomerNameOrderByOrderDateDesc(username);
    }

    public List<OrderHistory> getUserOrders(Long userId) {
        return orderHistoryRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Long getUserOrderCount(Long userId) {
        return orderHistoryRepository.countByUserId(userId);
    }

    public List<Map<String, Object>> parseOrderItems(String orderItemsJson) {
        List<Map<String, Object>> items = new ArrayList<>();

        try {
            if (orderItemsJson != null && !orderItemsJson.trim().isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();

                // JSON array olarak parse et
                List<Map<String, Object>> parsedItems = objectMapper.readValue(
                        orderItemsJson,
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                // Her item için gerekli alanları kontrol et
                for (Map<String, Object> item : parsedItems) {
                    Map<String, Object> processedItem = new HashMap<>();

                    processedItem.put("productName", item.getOrDefault("productName", "Bilinmeyen Ürün"));
                    processedItem.put("quantity", item.getOrDefault("quantity", 1));
                    processedItem.put("unitPrice", item.getOrDefault("unitPrice", 0.0));
                    processedItem.put("totalPrice", item.getOrDefault("totalPrice", 0.0));

                    items.add(processedItem);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing order items JSON: " + e.getMessage());
            System.err.println("JSON content: " + orderItemsJson);

            // Hata durumunda boş bir item ekle
            Map<String, Object> errorItem = new HashMap<>();
            errorItem.put("productName", "Ürün bilgisi alınamadı");
            errorItem.put("quantity", 0);
            errorItem.put("unitPrice", 0.0);
            errorItem.put("totalPrice", 0.0);
            items.add(errorItem);
        }

        return items;
    }
}
