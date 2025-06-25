package com.example.e_commerce.service;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.UserInteraction;
import com.example.e_commerce.enums.InteractionType;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.UserInteractionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;
    private UserInteractionRepository userInteractionRepository;

    public ProductService(ProductRepository productRepository, UserInteractionRepository userInteractionRepository) {
        this.productRepository = productRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    public List<Product> getAllProducts() {
         return productRepository.findAll();
    }

    public Optional<Product> getOneProduct(Long productId) {
        return productRepository.findById(productId);
    }

    public Product createOneProduct(Product newProduct) {
        return productRepository.save(newProduct);
    }

    public Product updateOneProduct(Long productId, Product updatedProduct) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            Product foundProduct = product.get();
            foundProduct.setProductName(updatedProduct.getProductName());
            foundProduct.setCategory(updatedProduct.getCategory());
            foundProduct.setImage(updatedProduct.getImage());
            foundProduct.setPrice(updatedProduct.getPrice());
            productRepository.save(foundProduct);
            return foundProduct;
        }
        else
            return null;
    }

    public void deleteOneProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<UserInteraction> ratings = userInteractionRepository.findByProductAndInteractionType(product, InteractionType.RATING);
        
        if (ratings.isEmpty()) {
            product.setAverageRating(0.0);
            product.setTotalRatings(0);
        } else {
            double totalRating = ratings.stream()
                    .mapToDouble(UserInteraction::getValue)
                    .sum();
            double averageRating = totalRating / ratings.size();
            
            product.setAverageRating(Math.round(averageRating * 100.0) / 100.0); // 2 decimal places
            product.setTotalRatings(ratings.size());
        }
        
        productRepository.save(product);
    }

    public Double getUserRatingForProduct(String username, Long productId) {
        // Bu metod daha sonra UserInteractionService'den çağrılacak
        return null;
    }
}
