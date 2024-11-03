package com.trade_ham.domain.product.controller;

import com.trade_ham.domain.auth.dto.CustomOAuth2User;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.domain.Product;
import com.trade_ham.domain.product.domain.ProductStatus;
import com.trade_ham.domain.product.service.PurchaseProductService;
import com.trade_ham.global.common.exception.AccessDeniedException;
import com.trade_ham.global.common.exception.ErrorCode;
import com.trade_ham.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PurchaseProductController {
    private final PurchaseProductService productService;
    private final UserRepository userRepository;

    @GetMapping("/product/purchase-page/{productId}")
    public ApiResponse<String> accessPurchasePage(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);

        // 상태가 SELL이 아니라면 예외 발생
        if (!product.getStatus().equals(ProductStatus.SELL)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        productService.purchaseProduct(productId);

        // 상태가 SELL이면 구매 페이지에 접근 가능
        return ApiResponse.success("구매 페이지에 접근 가능합니다.");
    }


}