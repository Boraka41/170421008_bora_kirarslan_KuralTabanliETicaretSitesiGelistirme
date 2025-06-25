package com.example.e_commerce.repository;

import com.example.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Modifying
    void deleteByCartId(Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartIdQuery(@Param("cartId") Long cartId);
}
