package com.KDT.mosi.domain.cart.repository;

import com.KDT.mosi.domain.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  // 장바구니 목록 조회
  List<CartItem> findByBuyerId(Long buyerId);

  // 중복 상품 체크
  Optional<CartItem> findByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);

  // 개별 상품 삭제
  void deleteByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);

  // 장바구니 전체 비우기
  void deleteByBuyerId(Long buyerId);

  //  선택 상품 조회 (주문용)
  List<CartItem> findByCartItemIdIn(List<Long> cartItemIds);

  // 장바구니 상품 개수
  int countByBuyerId(Long buyerId);
}