package com.trade_ham.domain.product.service;

import com.trade_ham.domain.auth.entity.UserEntity;
import com.trade_ham.domain.auth.repository.UserRepository;
import com.trade_ham.domain.product.dto.ProductDetailResponseDTO;
import com.trade_ham.domain.product.entity.ProductEntity;
import com.trade_ham.domain.product.entity.ProductStatus;
import com.trade_ham.domain.product.dto.ProductDTO;
import com.trade_ham.domain.product.dto.ProductResponseDTO;
import com.trade_ham.domain.product.repository.ProductRepository;
import com.trade_ham.global.common.exception.ErrorCode;
import com.trade_ham.global.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.trade_ham.domain.product.service.ViewLikeProductService.USER_LIKED_PRODUCTS_KEY_PREFIX;


@Service
@RequiredArgsConstructor
public class SellProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisSetTemplate; // Set 타입 Redis 관리


    // 물품 올리기
    public ProductResponseDTO createProduct(ProductDTO productDTO, Long sellerId) {
        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));


        ProductEntity productEntity = ProductEntity.builder()
                .seller(seller)
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .status(ProductStatus.SELL)
                .price(productDTO.getPrice())
                .build();

        seller.addSellingProduct(productEntity);

        ProductEntity savedProductEntity = productRepository.save(productEntity);

        return new ProductResponseDTO(savedProductEntity);
    }

    // 물품 수정
    public ProductResponseDTO updateProduct(Long productId, ProductDTO productDTO) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productEntity.updateProduct(productDTO.getName(), productDTO.getDescription(), productDTO.getPrice());

        return new ProductResponseDTO(productEntity);
    }

    // 물품 삭제
    public void deleteProduct(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매자 판매 내역에서 해당 상품 제거
        UserEntity seller = productEntity.getSeller();
        if (seller != null) {
            seller.deleteSellingProduct(productEntity);
        }

        productRepository.delete(productEntity);
    }


    // 상태가 SELL인 전체 판매 물품 최신순 조회
    // N+1 문제 발생 예상 지역
    /*
    게시물들을 RDB에서 들고온다.
    레디스를 통해 해당 게시물에 사용자가 좋아요를 누른 이력이 있는지 확인하고 DTO에 담아 반환
     */
    public List<ProductResponseDTO> findAllSellProducts(Long userId) {
        String userLikedProductsKey = USER_LIKED_PRODUCTS_KEY_PREFIX + userId;

        List<ProductEntity> productEntities = productRepository.findByStatusOrderByCreatedAtDescWithFetchJoin(ProductStatus.SELL);

        Set<String> likedProductIds = redisSetTemplate.opsForSet().members(userLikedProductsKey);


        return productEntities.stream()
                .map(product -> {
                    ProductResponseDTO responseDTO = new ProductResponseDTO(product);
                    responseDTO.setIsLiked(likedProductIds != null && likedProductIds.contains(String.valueOf(product.getProductId())));
                    return responseDTO;
                })
                .toList();
    }

    public ProductDetailResponseDTO findProductDetail(Long productId) {
        ProductEntity productEntity = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 조회 수 증가
        productEntity.increaseViews();

        return new ProductDetailResponseDTO(productEntity);
    }

    // 상태가 SELL인 전체 판매 물품 최신순 조회
    // N+1 문제 발생 예상 지역
    public ProductResponseDTO findSellProduct(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ProductResponseDTO(productEntity);
    }


}