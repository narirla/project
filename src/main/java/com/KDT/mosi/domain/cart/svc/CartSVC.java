package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.web.api.ApiResponse;

public interface CartSVC {
  /**
   * 장바구니 조회
   */
  CartResponse getCart(Long buyerId, String memberNickname);

  /**
   * 장바구니 상품 추가
   */
  ApiResponse<Void> addToCart(Long buyerId, Long productId, String optionType, Long quantity);

  /**
   * 수량 변경
   */
  ApiResponse<Void> updateQuantity(Long buyerId, Long productId, String optionType, Long quantity);

  /**
   * 상품 삭제
   */
  ApiResponse<Void> removeFromCart(Long buyerId, Long productId, String optionType);

  /**
   * 장바구니 비우기
   */
  void clearCart(Long buyerId);

  /**
   * 상품 개수 조회
   */
  int getCartItemCount(Long buyerId);


}