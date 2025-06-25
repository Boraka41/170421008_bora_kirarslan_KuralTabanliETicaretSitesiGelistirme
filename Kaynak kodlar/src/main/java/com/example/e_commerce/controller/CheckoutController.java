package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.OrderHistory;
import com.example.e_commerce.service.CartService;
import com.example.e_commerce.service.CheckoutService;
import com.example.e_commerce.service.UserService;
import com.example.e_commerce.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderHistoryService orderHistoryService;

    @Autowired
    private CheckoutService checkoutService;

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (!userOptional.isPresent()) {
                return "redirect:/login";
            }

            User user = userOptional.get();
            Cart cart = cartService.getCartByUserId(user.getId());

            if (cart == null || cart.getCartItems().isEmpty()) {
                model.addAttribute("cart", null);
                model.addAttribute("subtotal", BigDecimal.ZERO);
                model.addAttribute("shippingCost", BigDecimal.ZERO);
                model.addAttribute("taxAmount", BigDecimal.ZERO);
                model.addAttribute("totalAmount", BigDecimal.ZERO);
                model.addAttribute("user", user);
                return "checkout";
            }

            BigDecimal subtotal = checkoutService.calculateSubtotalWithDiscounts(cart);
            BigDecimal shippingCost = checkoutService.calculateShippingCost(subtotal);
            BigDecimal taxAmount = checkoutService.calculateTax(subtotal);
            BigDecimal totalAmount = subtotal.add(shippingCost).add(taxAmount);

            model.addAttribute("cart", cart);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingCost", shippingCost);
            model.addAttribute("taxAmount", taxAmount);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("user", user);

            return "checkout";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cart";
        }
    }

    @PostMapping("/confirm-order")
    public String confirmOrder(
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String zip,
            @RequestParam String cardNumber,
            @RequestParam String cardHolder,
            @RequestParam String expiry,
            @RequestParam String cvv,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (!userOptional.isPresent()) {
                return "redirect:/login";
            }

            User user = userOptional.get();
            Cart cart = cartService.getCartByUserId(user.getId());

            if (cart == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Sepetiniz boş!");
                return "redirect:/cart";
            }

            BigDecimal subtotal = checkoutService.calculateSubtotalWithDiscounts(cart);
            BigDecimal shippingCost = checkoutService.calculateShippingCost(subtotal);
            BigDecimal taxAmount = checkoutService.calculateTax(subtotal);
            BigDecimal totalAmount = subtotal.add(shippingCost).add(taxAmount);

            String deliveryAddress = address + ", " + city + " " + zip;
            OrderHistory savedOrder = orderHistoryService.saveOrder(fullName, deliveryAddress, cart, totalAmount, user.getId());

            model.addAttribute("orderNumber", savedOrder.getOrderNumber());
            model.addAttribute("orderDate", savedOrder.getOrderDate());
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            model.addAttribute("city", city);
            model.addAttribute("zip", zip);
            model.addAttribute("cardLastFour", cardNumber.replaceAll("\\s", "").substring(cardNumber.replaceAll("\\s", "").length() - 4));
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingCost", shippingCost);
            model.addAttribute("taxAmount", taxAmount);
            model.addAttribute("totalAmount", totalAmount);

            List<Map<String, Object>> orderItems = orderHistoryService.parseOrderItems(savedOrder.getOrderItems());
            model.addAttribute("orderItems", orderItems);

            cartService.clearCart(user.getId());

            return "order-confirmation";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Sipariş işlemi sırasında bir hata oluştu!");
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(Model model) {
        return "order-success";
    }
}
