package com.KDT.mosi.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 상품 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

  private Long cartItemId; // 추가: 장바구니 아이템 ID
  private Long productId;
  private String productName;
  private String description;
  private Long price;
  private Long originalPrice;
  private Long quantity;
  private String optionType;
  private String productImage;
  private String sellerNickname;
  private boolean available;

  /**
   * 사용 가능한 상품 생성
   */
  public static CartItemResponse createAvailable(Long cartItemId, Long productId, String productTitle, String description,
                                                 Long price, Long originalPrice, Long quantity,
                                                 String optionType, String imageData, String sellerNickname) {
    return CartItemResponse.builder()
        .cartItemId(cartItemId)
        .productId(productId)
        .productName(productTitle)
        .description(description)
        .price(price)
        .originalPrice(originalPrice)
        .quantity(quantity)
        .optionType(optionType)
        .productImage(imageData)
        .sellerNickname(sellerNickname)
        .available(true)
        .build();
  }

  /**
   * 사용 불가능한 상품 생성
   */
  public static CartItemResponse createUnavailable(Long cartItemId, Long productId, String productTitle, String description,
                                                   Long price, Long originalPrice, Long quantity,
                                                   String optionType, String imageData, String sellerNickname) {
    return CartItemResponse.builder()
        .cartItemId(cartItemId)
        .productId(productId)
        .productName(productTitle)
        .description(description)
        .price(price)
        .originalPrice(originalPrice)
        .quantity(quantity)
        .optionType(optionType)
        .productImage(imageData)
        .sellerNickname(sellerNickname)
        .available(false)
        .build();
  }
}