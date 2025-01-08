package com.trade_ham.domain.product.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import com.trade_ham.domain.product.repository.ProductRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FindAllSellProductsTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int PRODUCTS = 100;

    @BeforeEach
    void setUp() {
        // 상품 데이터 생성
        createTestProductsWithMultipleSellers(PRODUCTS);

        // 영속성 컨텍스트 초기화
        entityManager.clear(); // 테스트 데이터는 이후 영속성 컨텍스트에서 관리되지 않도록 초기화
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

    // EAGER Fetch, LAZY Fetch를 사용한 기본 조회 성능 테스트
    @Test
    void findAllSellProducts() {
        // Hibernate 쿼리 로깅 활성화
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
                .getStatistics().setStatisticsEnabled(true);

        // 판매 상태인 상품 조회
        List<ProductEntity> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.SELL);
        products.forEach(rs -> System.out.println(rs.getSeller().getUsername()));

        // 검증
        assertEquals(PRODUCTS, products.size());

        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();

        // Hibernate 쿼리 수 확인
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("Number of queries executed: " + queryCount);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // N+1 문제 검증: 예상 쿼리 수와 실제 쿼리 수 비교
        assertEquals(1, queryCount, "N+1 문제가 발생하지 않도록 하나의 쿼리로 데이터를 가져와야 합니다.");

    }

    // Fetch Join을 사용한 조회 성능 테스트
//    @Test
//    void fetchJoinPerformanceTest() {
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics().setStatisticsEnabled(true);
//        Statistics statistics = entityManager.getEntityManagerFactory()
//                .unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics();
//        statistics.clear();
//
//        long startTime = System.nanoTime();
//
//        // Fetch Join 방식으로 데이터 조회
//        List<ProductEntity> products = productRepository.findByStatusOrderByCreatedAtDescWithFetchJoin(ProductStatus.SELL);
//
//        for (ProductEntity product : products) {
//            String name = product.getSeller().getUsername();
//        }
//
//        long elapsedTime = System.nanoTime() - startTime;
//
//        System.out.println("Products Count: " + products.size());
//        System.out.println("Executed Queries: " + statistics.getPrepareStatementCount());
//        System.out.println("Elapsed Time (ms): " + elapsedTime / 1_000_000);
//
//        assertEquals(PRODUCTS, products.size());
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//    }

    // EntityGraph를 사용한 조회 성능 테스트
//    @Test
//    void entityGraphPerformanceTest() {
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics().setStatisticsEnabled(true);
//        Statistics statistics = entityManager.getEntityManagerFactory()
//                .unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics();
//        statistics.clear();
//
//        long startTime = System.nanoTime();
//
//        // EntityGraph 방식으로 데이터 조회
//        List<ProductEntity> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.SELL);
//
//        for (ProductEntity product : products) {
//            String name = product.getSeller().getUsername();
//        }
//
//        long elapsedTime = System.nanoTime() - startTime;
//
//        System.out.println("Products Count: " + products.size());
//        System.out.println("Executed Queries: " + statistics.getPrepareStatementCount());
//        System.out.println("Elapsed Time (ms): " + elapsedTime / 1_000_000);
//
//        assertEquals(PRODUCTS, products.size());
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//    }

    // EntityGraph와 페이징 처리 성능 테스트
//    @Test
//    void entityGraphPagingTest() {
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics().setStatisticsEnabled(true);
//        Statistics statistics = entityManager.getEntityManagerFactory()
//                .unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics();
//        statistics.clear();
//
//        // 페이징 설정 (2페이지, 페이지 당 10개)
//        Pageable pageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
//
//        long startTime = System.nanoTime();
//
//        // EntityGraph와 페이징 처리로 데이터 조회
//        Page<ProductEntity> page = productRepository.findByStatus(ProductStatus.SELL, pageable);
//
//        List<ProductEntity> products = page.getContent();
//        for (ProductEntity product : products) {
//            String name = product.getSeller().getUsername(); // Lazy Loading 확인
//        }
//
//        long elapsedTime = System.nanoTime() - startTime;
//
//        System.out.println("Products Count: " + products.size());
//        System.out.println("Total Pages: " + page.getTotalPages());
//        System.out.println("Total Elements: " + page.getTotalElements());
//        System.out.println("Executed Queries: " + statistics.getPrepareStatementCount());
//        System.out.println("Elapsed Time (ms): " + elapsedTime / 1_000_000);
//
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        // 검증
//        assertEquals(10, products.size());
//    }

    // Fetch Join과 페이징 처리 성능 테스트
//    @Test
//    void fetchJoinPagingTest() {
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics().setStatisticsEnabled(true);
//        Statistics statistics = entityManager.getEntityManagerFactory()
//                .unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics();
//        statistics.clear();
//
//        // 페이징 설정 (2페이지, 페이지 당 10개)
//        Pageable pageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
//
//        long startTime = System.nanoTime();
//
//        // Fetch Join과 페이징 처리로 데이터 조회
//        Page<ProductEntity> page = productRepository.findByStatusWithFetchJoin(ProductStatus.SELL, pageable);
//
//        List<ProductEntity> products = page.getContent();
//        for (ProductEntity product : products) {
//            String name = product.getSeller().getUsername(); // 연관 데이터 확인
//        }
//
//        long elapsedTime = System.nanoTime() - startTime;
//
//        System.out.println("Products Count: " + products.size());
//        System.out.println("Total Pages: " + page.getTotalPages());
//        System.out.println("Total Elements: " + page.getTotalElements());
//        System.out.println("Executed Queries: " + statistics.getPrepareStatementCount());
//        System.out.println("Elapsed Time (ms): " + elapsedTime / 1_000_000);
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        // 검증
//        assertEquals(10, products.size());
//    }

    // Batch Size 설정을 통한 LAZY 로딩 성능 테스트
//    @Test
//    void batchSizePerformanceTest() {
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics().setStatisticsEnabled(true);
//        Statistics statistics = entityManager.getEntityManagerFactory()
//                .unwrap(org.hibernate.SessionFactory.class)
//                .getStatistics();
//        statistics.clear();
//
//        long startTime = System.nanoTime();
//
//        // 페이징 없이 모든 ProductEntity 조회
//        List<ProductEntity> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.SELL);
//
//        // 각 ProductEntity의 seller 데이터 접근 (LAZY 로딩 트리거)
//        for (ProductEntity product : products) {
//            String name = product.getSeller().getUsername(); // BatchSize로 로드
//        }
//
//        long elapsedTime = System.nanoTime() - startTime;
//
//        System.out.println("Products Count: " + products.size());
//        System.out.println("Executed Queries: " + statistics.getPrepareStatementCount());
//        System.out.println("Elapsed Time (ms): " + elapsedTime / 1_000_000);
//
//        assertEquals(PRODUCTS, products.size());
//        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
//    }
}
