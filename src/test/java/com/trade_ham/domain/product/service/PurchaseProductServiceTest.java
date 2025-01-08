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

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PurchaseProductServiceTest {

    @Autowired
    private PurchaseProductService purchaseProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int PRODUCTS = 10;

    @BeforeEach
    void setUp() {
        // 상품 데이터 생성
        createTestProductsWithMultipleSellers(PRODUCTS);

        // 영속성 컨텍스트 초기화
        entityManager.clear(); // 테스트 데이터는 이후 영속성 컨텍스트에서 관리되지 않도록 초기화
    }

    private void createTestProductsWithMultipleSellers(int count) {
        UserEntity seller = UserEntity.builder()
                .username("test_user_" + 1)
                .build();
        userRepository.save(seller);
        for (int i = 1; i <= count; i++) {
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
    void testPurchaseProductNPlusOneProblem() {

        // When: 여러 제품에 대해 purchaseProduct 호출
        List<ProductResponseDTO> products = productService.findAllSellProducts(1L);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        products.forEach(product -> purchaseProductService.purchaseProduct(product.getProductId()));
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Then: 쿼리 로그를 확인하여 N+1 문제가 발생하는지 확인
    }
}
