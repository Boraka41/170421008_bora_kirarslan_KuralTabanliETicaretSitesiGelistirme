package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Category;
import com.example.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByCategoryId(Long categoryId);
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<Category> findDistinctCategories();
}
