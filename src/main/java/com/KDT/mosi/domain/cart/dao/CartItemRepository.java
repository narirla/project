package com.KDT.mosi.domain.cart.dao;

import com.KDT.mosi.domain.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  // 기본 CRUD는 JpaRepository에서 자동 제공
  // save(), findById(), delete(), deleteById() 등

  // 장바구니 상품 목록 조회 (장바구니 ID 기준)
  List<CartItem> findByCartId(Long cartId);

  // 장바구니 상품 목록 조회 (구매자 ID 기준)
  List<CartItem> findByBuyerId(Long buyerId);

  // 동일 상품 체크용 (메소드명 수정)
  Optional<CartItem> findByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);

  // 동일 상품 존재 여부 (메소드명 수정)
  boolean existsByBuyerIdAndProductIdAndOptionType(Long buyerId, Long productId, String optionType);

  // 구매자 장바구니 상품 수량
  int countByBuyerId(Long buyerId);

  // 선택 상품 조회 (주문 시 사용) - 이 부분이 빠졌었음
  @Query("SELECT ci FROM CartItem ci WHERE ci.cartItemId IN :cartItemIds AND ci.buyerId = :buyerId")
  List<CartItem> findByItemIds(@Param("cartItemIds") List<Long> cartItemIds, @Param("buyerId") Long buyerId);

  // 선택 상품 삭제 - 이 부분이 빠졌었음
  @Modifying
  @Query("DELETE FROM CartItem ci WHERE ci.buyerId = :buyerId AND ci.productId = :productId AND ci.optionType = :optionType")
  void deleteByBuyerIdAndProductIdAndOptionType(@Param("buyerId") Long buyerId,
                                                @Param("productId") Long productId,
                                                @Param("optionType") String optionType);

  // 구매자 장바구니 전체 비우기 - 이 부분이 빠졌었음
  @Modifying
  @Query("DELETE FROM CartItem ci WHERE ci.buyerId = :buyerId")
  void deleteByBuyerId(@Param("buyerId") Long buyerId);
}