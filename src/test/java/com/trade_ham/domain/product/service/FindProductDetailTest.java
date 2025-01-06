package com.trade_ham.domain.product.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.dto.ProductDetailResponseDTO;
import com.trade_ham.domain.product.dto.ProductResponseDTO;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import com.trade_ham.domain.product.repository.ProductRepository;
import com.trade_ham.domain.product.service.SellProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application-test.yml")
public class FindProductDetailTest {

    @Autowired
    private SellProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

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
    @DisplayName("N+1 문제 발생 여부 테스트")
    public void testFindProductDetail_NPlusOne() {
        // When: 모든 제품의 상세 정보를 조회
        List<ProductResponseDTO> products = productService.findAllSellProducts(1L);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        for (ProductResponseDTO product : products) {
            ProductDetailResponseDTO response = productService.findProductDetail(product.getProductId());
            assertNotNull(response);
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Then: 실행 시 쿼리 로그를 확인하여 N+1 문제 발생 여부 판단
    }

    @Test
    @DisplayName("N+1 문제 해결된 전체 물건 정보 테스트")
    public void testFindProductDetail_NoNPlusOne() {
        // When: 모든 제품의 상세 정보를 조회
        List<ProductResponseDTO> products = productService.findAllSellProducts(1L);
        for (ProductResponseDTO product : products) {
            // 개별 정보를 가져올 필요 없이 이미 조회된 데이터를 바로 사용
            assertNotNull(product);
            System.out.println("Product ID: " + product.getProductId());
            System.out.println("Product Name: " + product.getName());
            System.out.println("Product Description: " + product.getDescription());
        }
        // Then: 실행 시 쿼리 로그에 N+1 문제가 없는지 확인
    }
}

