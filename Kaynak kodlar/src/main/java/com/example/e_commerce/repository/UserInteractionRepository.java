package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserInteraction;
import com.example.e_commerce.enums.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUserUsernameAndCategoryId(String username, Long categoryId);
    UserInteraction findByUserAndProductAndInteractionType(User user, Product product, InteractionType interactionType);
    List<UserInteraction> findByProductAndInteractionType(Product product, InteractionType interactionType);
    List<UserInteraction> findByUser(User user);
    List<UserInteraction> findByUserAndInteractionType(User user, InteractionType interactionType);
}
