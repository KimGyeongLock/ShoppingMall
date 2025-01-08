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
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ViewLikeProductServiceTest {

    @Autowired
    private ViewLikeProductService viewLikeProductService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int PRODUCTS = 10;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock Redis에 좋아요한 상품 ID 저장
        mockLikedProductsInRedis();

        // 상품 데이터 생성
        createTestProducts(PRODUCTS);

        // 영속성 컨텍스트 초기화
        entityManager.clear();
    }

    private void createTestProducts(int count) {
        UserEntity seller = UserEntity.builder()
                .username("test_seller")
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

    private void mockLikedProductsInRedis() {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String userLikedProductsKey = "user:like:products:" + USER_ID;
        for (int i = 1; i <= PRODUCTS; i++) {
            setOperations.add(userLikedProductsKey, String.valueOf(i));
        }
    }

    @Test
    void findUserLikeProducts_shouldNotHaveNPlusOneProblem() {
        // Hibernate 쿼리 통계 활성화
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
                .getStatistics().setStatisticsEnabled(true);

        // 사용자가 좋아요한 상품 조회
        List<ProductResponseDTO> products = viewLikeProductService.findUserLikeProducts(USER_ID);
        products.forEach(product -> System.out.println(product.getName()));

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
