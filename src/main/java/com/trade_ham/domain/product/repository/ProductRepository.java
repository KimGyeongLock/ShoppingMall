package com.trade_ham.domain.product.repository;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductId(Long productId);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    List<ProductEntity> findBySeller(UserEntity seller);
    @Query("SELECT p FROM ProductEntity p " +
            "JOIN FETCH p.seller " +
            "WHERE p.buyer.id = :buyerId")
    List<ProductEntity> findByBuyerWithFetchJoin(@Param("buyerId") Long buyerId);
    List<ProductEntity> findByStatusOrderByCreatedAtDesc(ProductStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :productId")
    Optional<ProductEntity> findByIdWithPessimisticLock(@Param("productId") Long productId);

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = 'SELL' " +
            "ORDER BY p.createdAt DESC")
    List<ProductEntity> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM ProductEntity p " +
            "LEFT JOIN FETCH p.seller " +
            "LEFT JOIN FETCH p.buyer " +
            "LEFT JOIN FETCH p.lockerEntity " +
            "WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<ProductEntity> findByStatusOrderByCreatedAtDescWithFetchJoin(@Param("status") ProductStatus status);

//    @EntityGraph(attributePaths = {"seller"})
//    List<ProductEntity> findByStatusOrderByCreatedAtDesc(ProductStatus status);

//    @EntityGraph(attributePaths = {"seller"})
//    Page<ProductEntity> findByStatus(ProductStatus status, Pageable pageable);
//
//    @Query(value = "SELECT p FROM ProductEntity p JOIN FETCH p.seller WHERE p.status = :status",
//            countQuery = "SELECT COUNT(p) FROM ProductEntity p WHERE p.status = :status")
//    Page<ProductEntity> findByStatusWithFetchJoin(@Param("status") ProductStatus status, Pageable pageable);


}

