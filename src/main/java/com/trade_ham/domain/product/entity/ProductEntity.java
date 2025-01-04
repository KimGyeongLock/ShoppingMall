package com.trade_ham.domain.product.entity;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.locker.entity.LockerEntity;
import com.trade_ham.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
//    @BatchSize(size = 10) // 한 번에 최대 10개의 seller를 배치로 로드
    private UserEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private UserEntity buyer;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private ProductStatus status;

    @Column(nullable = false)
    private Long price;

    @Setter
    @OneToOne
    @JoinColumn(name = "locker_id")
    private LockerEntity lockerEntity;

    @Builder.Default
    private Long views = 0L;

    @Builder.Default
    private Long likes = 0L;

    public void updateProduct(String name, String description, Long price){
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void increaseLikes(){
        this.likes++;
    }

    public void decreaseLikes(){
        if(this.likes>0){
            this.likes--;
        }
    }

    public void increaseViews(){
        this.views++;
    }
}