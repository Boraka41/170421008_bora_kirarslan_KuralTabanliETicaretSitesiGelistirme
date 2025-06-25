package com.example.e_commerce.repository;

import com.example.e_commerce.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    @Query("SELECT oh FROM OrderHistory oh WHERE oh.userId = :userId AND oh.orderItems LIKE %:categoryId% ORDER BY oh.orderDate DESC")
    List<OrderHistory> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT oh FROM OrderHistory oh WHERE oh.userId = :userId ORDER BY oh.orderDate DESC")
    List<OrderHistory> findByUserIdOrderByOrderDateDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(oh) FROM OrderHistory oh WHERE oh.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT oh FROM OrderHistory oh WHERE oh.customerName = :customerName ORDER BY oh.orderDate DESC")
    List<OrderHistory> findByCustomerNameOrderByOrderDateDesc(@Param("customerName") String customerName);

    @Query("SELECT COALESCE(SUM(oh.totalAmount), 0) FROM OrderHistory oh WHERE oh.userId = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);
}
