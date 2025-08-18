package com.KDT.mosi.domain.cart.dao;

import com.KDT.mosi.domain.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  // 기본 CRUD는 JpaRepository에서 자동 제공
  // save(), findById(), delete(), deleteById(), existsById() 등

  // 장바구니 단건 조회 (구매자 ID 기준)
  Optional<Cart> findByBuyerId(Long buyerId);

  // 장바구니 존재 여부 (구매자 ID 기준)
  boolean existsByBuyerId(Long buyerId);

  // 장바구니 총 상품 수량 합계
  @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.buyerId = :buyerId")
  int getTotalItemCount(@Param("buyerId") Long buyerId);

  // 구매자 장바구니 삭제 - 이 부분이 빠졌었음
  @Modifying
  @Query("DELETE FROM Cart c WHERE c.buyerId = :buyerId")
  void deleteByBuyerId(@Param("buyerId") Long buyerId);
}