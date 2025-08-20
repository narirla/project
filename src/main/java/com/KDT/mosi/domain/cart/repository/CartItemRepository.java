package com.KDT.mosi.domain.cart.repository;

import com.KDT.mosi.domain.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  /**
   * 상품 조회
   */
  List<CartItem> findByBuyerId(Long buyerId);

  /**
   * 중복 상품 확인
   */
  Optional<CartItem> findByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);

  /**
   * 상품 삭제
   */
  void deleteByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);
  void deleteByBuyerId(Long buyerId);

  /**
   * 상품 개수 조회
   */
  int countByBuyerId(Long buyerId);
}