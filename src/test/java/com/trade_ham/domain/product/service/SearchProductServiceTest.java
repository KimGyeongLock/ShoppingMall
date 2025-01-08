package com.trade_ham.domain.product.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.dto.ProductResponseDTO;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import com.trade_ham.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SearchProductServiceTest {

    @Autowired
    private SearchProductService searchProductService;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private static final int PRODUCTS = 10;

    @BeforeEach
    void setUp() {
        // 상품 데이터 생성
        createTestProductsWithMultipleSellers(PRODUCTS);

        // 영속성 컨텍스트 초기화
        entityManager.clear();
    }

    private void createTestProductsWithMultipleSellers(int count) {
        for (int i = 1; i <= count; i++) {
            UserEntity seller = UserEntity.builder()
                    .username("test_user_" + i)
                    .build();
            userRepository.save(seller);

            ProductEntity product = ProductEntity.builder()
                    .name("Product " + i)
                    .description("Description for product " + i)
                    .price(3000L)
                    .status(ProductStatus.SELL)
                    .seller(seller)
                    .build();
            productRepository.save(product);
        }
    }

    @Test
    void testSearchProductsNPlusOneProblem() {
        // When: keyword가 포함된 제품 검색
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        List<ProductEntity> response = productRepository.searchProducts("Product");
        response.forEach(rs -> System.out.println(rs.getSeller().getUsername()));

        // Then: 반환된 제품 개수가 PRODUCTS와 동일한지 확인
        assertEquals(PRODUCTS, response.size(), "반환된 제품 개수가 예상과 다릅니다.");

        // 반환된 모든 제품 이름이 "Product"를 포함하는지 확인
        assertTrue(response.stream().allMatch(product -> product.getName().contains("Product")),
                "반환된 제품 이름에 'Product'가 포함되지 않은 항목이 있습니다.");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

    }
}