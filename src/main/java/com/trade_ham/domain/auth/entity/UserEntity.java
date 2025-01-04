package com.trade_ham.domain.auth.entity;

import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.global.common.enums.Provider;
import com.trade_ham.global.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BatchSize(size = 10)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email; // 이메일
    private String username; // OAuth2 유일 식별자
    private String nickname; // 닉네임
    private String profile_image; // 사용자 이미지

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, USER

    @Enumerated(EnumType.STRING)
    private Provider provider; // KAKAO, NAVER
    @OneToMany(mappedBy = "seller")
    private List<ProductEntity> sellingProductEntities = new ArrayList<>();

    @OneToMany(mappedBy = "buyer")
    private List<ProductEntity> purchasedProductEntities = new ArrayList<>();

    // 추후 따로 받는다.
    private String acount; // 계좌번호
    private String realname; // 실제이름

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addSellingProduct(ProductEntity productEntity) {
        this.sellingProductEntities.add(productEntity);
    }

    public void addPurchasedProduct(ProductEntity productEntity) {
        this.purchasedProductEntities.add(productEntity);
    }

    public void deleteSellingProduct(ProductEntity productEntity) {
        this.sellingProductEntities.remove(productEntity);
    }
}
