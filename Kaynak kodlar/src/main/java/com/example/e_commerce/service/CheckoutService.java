package com.example.e_commerce.service;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CheckoutService {

    public BigDecimal calculateSubtotalWithDiscounts(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            BigDecimal itemTotal = BigDecimal.valueOf(item.getTotalPrice());
            subtotal = subtotal.add(itemTotal);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateShippingCost(BigDecimal subtotal) {
        if (subtotal.compareTo(new BigDecimal("100.00")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("15.00");
    }

    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
