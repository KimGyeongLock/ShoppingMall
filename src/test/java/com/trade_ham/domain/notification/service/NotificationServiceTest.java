package com.trade_ham.domain.notification.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.notification.dto.NotificationResponseDTO;
import com.trade_ham.domain.notification.entity.NotificationEntity;
import com.trade_ham.domain.notification.entity.NotificationType;
import com.trade_ham.domain.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int NOTIFICATIONS = 10;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 및 알림 데이터 생성
        createTestNotifications(NOTIFICATIONS);

        // 영속성 컨텍스트 초기화
        entityManager.clear();
    }

    private void createTestNotifications(int count) {
        UserEntity user = UserEntity.builder()
                .username("test_user")
                .build();
        userRepository.save(user);

        for (int i = 1; i <= count; i++) {
            NotificationEntity notification = NotificationEntity.builder()
                    .user(user)
                    .message("Test notification " + i)
                    .type(NotificationType.PURCHASE_COMPLETE)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Test
    void getUserNotifications_shouldNotHaveNPlusOneProblem() {
        // Hibernate 쿼리 통계 활성화
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class)
                .getStatistics().setStatisticsEnabled(true);

        // 사용자 알림 조회
        List<NotificationResponseDTO> notifications = notificationService.getUserNotifications(USER_ID);
        notifications.forEach(notification -> System.out.println(notification.getMessage()));

        // 검증: 알림 개수
        assertEquals(NOTIFICATIONS, notifications.size());

        // Hibernate 쿼리 수 확인
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("Number of queries executed: " + queryCount);

        // N+1 문제 검증: 예상 쿼리 수는 2개 (Notification 조회 1개 + User 조회 1개 + Read Update 1개)
        assertEquals(3, queryCount, "N+1 문제가 발생하지 않도록 하나의 쿼리로 데이터를 가져와야 합니다.");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }
}
