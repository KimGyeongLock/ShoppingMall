package com.trade_ham.domain.mypage.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import com.trade_ham.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class FindProductsByBuyerTest {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int PRODUCTS = 10;

    @BeforeEach
    void setUp() {
        // 구매자가 구매한 상품 100개 생성
        createTestProductsForBuyer(PRODUCTS);

        // 영속성 컨텍스트 초기화
        entityManager.clear();
    }

    private void createTestProductsForBuyer(int count) {
        UserEntity buyer = UserEntity.builder()
                .username("test_buyer")
                .build();
        userRepository.save(buyer);

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
                    .buyer(buyer)
                    .build();
            productRepository.save(product);
        }
    }

    @Test
    void findProductsByBuyer_shouldNotHaveNPlusOneProblem() {
        // Hibernate 쿼리 통계 활성화
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
                .getStatistics().setStatisticsEnabled(true);

        // 구매자가 구매한 상품 조회
        List<ProductEntity> products = myPageService.findProductsByBuyer(1L);
        products.forEach(product -> System.out.println(product.getName() + " - " + product.getSeller().getUsername()));

        // 검증: 상품 개수
        assertEquals(PRODUCTS, products.size());

        // Hibernate 쿼리 수 확인
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("Number of queries executed: " + queryCount);

        // N+1 문제 검증: 예상 쿼리 수는 2개 (Product 조회 1개 + User 조회 1개)
        assertEquals(2, queryCount, "N+1 문제가 발생하지 않도록 하나의 쿼리로 데이터를 가져와야 합니다.");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }
}
