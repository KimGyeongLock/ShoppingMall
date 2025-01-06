package com.trade_ham.domain.product.controller;

import com.trade_ham.domain.auth.dto.CustomOAuth2User;
import com.trade_ham.domain.product.dto.ProductDTO;
import com.trade_ham.domain.product.dto.ProductDetailResponseDTO;
import com.trade_ham.domain.product.dto.ProductResponseDTO;
import com.trade_ham.domain.product.service.SellProductService;
import com.trade_ham.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class SellProductController {
    private final SellProductService sellProductService;

    // 물품 올리기
    @PostMapping("/sell")
    public ApiResponse<ProductResponseDTO> createProduct(@RequestBody ProductDTO productDTO, @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long sellerId = oAuth2User.getId();
        ProductResponseDTO product = sellProductService.createProduct(productDTO, sellerId);

        return ApiResponse.success(product);
    }

    // 물품 수정
    @PutMapping("/{productId}")
    public ApiResponse<ProductResponseDTO> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        ProductResponseDTO updatedProduct = sellProductService.updateProduct(productId, productDTO);

        return ApiResponse.success(updatedProduct);
    }

    // 물품 삭제
    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable Long productId) {
        sellProductService.deleteProduct(productId);

        return ApiResponse.success("삭제 완료");
    }
  
    // 상태가 SELL인 전체 판매 물품 최신순으로 조회
    // 필요한 데이터만 전달
    @GetMapping("/all")
    public ApiResponse<List<ProductResponseDTO>> findAllSellProducts(@AuthenticationPrincipal Long userId) { //userId가 왜 전달되는지 모르겠음 :: roki
        List<ProductResponseDTO> products = sellProductService.findAllSellProducts(userId);

        return ApiResponse.success(products);
    }

    // 물품 상세 페이지 제공
    // 해당 물품 조회 수 증가
    @GetMapping("/{productId}/detail")
    public ApiResponse<ProductDetailResponseDTO> findProductDetail(@AuthenticationPrincipal Long userId, @PathVariable Long productId) {
        ProductDetailResponseDTO product = sellProductService.findProductDetail(productId);

        return ApiResponse.success(product);
    }
}