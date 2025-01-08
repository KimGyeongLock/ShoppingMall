package com.trade_ham.domain.mypage.service;


import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.dto.ProductResponseDTO;
import com.trade_ham.domain.product.repository.ProductRepository;
import com.trade_ham.global.common.exception.ErrorCode;
import com.trade_ham.global.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 구매자 구매 내역 관리
    public List<ProductEntity> findProductsByBuyer(Long buyerId) {
        UserEntity buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return productRepository.findByBuyerWithFetchJoin(buyer.getId()); // findProductsBySeller 에만 ProductResponseDTO로 type casting?
    }

    // 판매자 판매 내역 관리
    public List<ProductResponseDTO> findProductsBySeller(Long sellerId) {
        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        List<ProductEntity> productEntities = productRepository.findBySeller(seller);

        return productEntities.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }
}
