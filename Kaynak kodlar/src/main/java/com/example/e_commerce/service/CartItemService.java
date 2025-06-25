package com.example.e_commerce.service;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.CartRepository;
import com.example.e_commerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserInteractionService userInteractionService;

    public CartItemService(CartItemRepository cartItemRepository, CartRepository cartRepository, ProductRepository productRepository, UserService userService, UserInteractionService userInteractionService) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.userInteractionService = userInteractionService;
    }

    public CartItem addProductToCartByUsername(String username, Long productId, int quantity) {
        // Kullanıcıyı bul
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Sepeti bul ya da oluştur
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalAmount(0.0);
                    return cartRepository.save(newCart);
                });

        // Ürünü bul
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // İndirim hesaplama
        double discountRate = userInteractionService.calculateCategoryDiscount(username, product.getCategory().getId());
        double discountedPrice = product.getPrice() * (1 - discountRate);  // İndirimli fiyat

        // Sepette mevcut ürün var mı kontrol et
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setTotalPrice(cartItem.getQuantity() * discountedPrice);  // İndirimli toplam fiyat
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(discountedPrice);  // İndirimli fiyat
            cartItem.setTotalPrice(quantity * discountedPrice);  // İndirimli toplam fiyat

            cart.getCartItems().add(cartItem);
        }

        // Sepet item'ını kaydet
        cartItemRepository.save(cartItem);

        // Sepetin toplamını güncelle
        double newTotal = cart.getTotalAmount() + quantity * discountedPrice;  // İndirimli toplam
        cart.setTotalAmount(newTotal);

        cartRepository.save(cart);

        return cartItem;
    }


    public CartItem deleteOneCartItem(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem bulunamadı: " + cartItemId));
        Cart cart = item.getCart();
        double newTotal = cart.getTotalAmount() - item.getTotalPrice();
        cart.setTotalAmount(Math.max(newTotal, 0.0));
        cartRepository.save(cart);
        cartItemRepository.deleteById(cartItemId);
        return item;
    }


}
