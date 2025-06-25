package com.example.e_commerce.controller;

import com.example.e_commerce.entity.OrderHistory;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.OrderHistoryService;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class OrderHistoryController {

    @Autowired
    private OrderHistoryService orderHistoryService;

    @Autowired
    private UserService userService;

    @GetMapping("/order-history")
    public String orderHistory(Model model) {
        try {
            // Kullanıcı bilgilerini al
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (!userOptional.isPresent()) {
                return "redirect:/login";
            }

            User user = userOptional.get();

            // Kullanıcının siparişlerini getir
            List<OrderHistory> orders = orderHistoryService.getUserOrders(user.getId());

            // Her sipariş için item'ları parse et
            Map<Long, List<Map<String, Object>>> orderItemsMap = new HashMap<>();

            for (OrderHistory order : orders) {
                try {
                    List<Map<String, Object>> items = orderHistoryService.parseOrderItems(order.getOrderItems());
                    orderItemsMap.put(order.getId(), items);
                    System.out.println("Order " + order.getId() + " items: " + items); // Debug için
                } catch (Exception e) {
                    System.err.println("Error parsing items for order " + order.getId() + ": " + e.getMessage());
                    orderItemsMap.put(order.getId(), new ArrayList<>());
                }
            }

            model.addAttribute("orders", orders);
            model.addAttribute("orderItemsMap", orderItemsMap);

            return "order-history";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Sipariş geçmişi yüklenirken hata oluştu.");
            model.addAttribute("orders", new ArrayList<>());
            return "order-history";
        }
    }
}
