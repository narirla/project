package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.entity.cart.CartItem;
import java.util.List;
import java.util.Map;

public interface CartSVC {
  // 장바구니에 상품 추가
  Map<String, Object> addToCart(Long buyerId, Long productId, String optionType, Integer quantity);

  // 장바구니 조회
  Map<String, Object> getCartSummary(Long buyerId);

  // 수량 변경
  Map<String, Object> updateQuantity(Long buyerId, Long productId, String optionType, Integer quantity);

  // 장바구니에서 상품 제거
  Map<String, Object> removeFromCart(Long buyerId, Long productId, String optionType);

  // 장바구니 전체 비우기
  void clearCart(Long buyerId);

  // 장바구니 상품 개수 조회
  int getCartItemCount(Long buyerId);

  // 선택된 장바구니 상품 조회 (주문용)
  List<CartItem> getSelectedCartItems(Long buyerId, List<Long> cartItemIds);
}