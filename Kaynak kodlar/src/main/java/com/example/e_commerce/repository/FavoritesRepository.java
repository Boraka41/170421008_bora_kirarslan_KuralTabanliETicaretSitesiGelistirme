package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Favorites;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoritesRepository extends JpaRepository<Favorites,Long> {
    Optional<Favorites> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
    @Query("select f.product.id from Favorites f where f.user.username = :username")
    List<Long> findProductIdsByUsername(@Param("username") String username);
}
