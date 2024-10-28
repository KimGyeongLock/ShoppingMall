package com.trade_ham.domain.auth.entity;

import com.trade_ham.global.common.enums.Provider;
import com.trade_ham.global.common.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    private String email; // 이메일
    private String username; // OAuth2 유일 식별자
    private String nickname; // 닉네임
    private String profile_image; // 사용자 이미지

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, USER

    @Enumerated(EnumType.STRING)
    private Provider provider; // KAKAO, NAVER

    // 추후 따로 받는다.
    private String acount; // 계좌번호
    private String realname; // 실제이름
}