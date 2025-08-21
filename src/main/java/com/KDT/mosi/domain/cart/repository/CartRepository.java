package com.KDT.mosi.domain.cart.repository;

import com.KDT.mosi.domain.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  // 구매자별 장바구니 조회
  Optional<Cart> findByBuyerId(Long buyerId);

  // 장바구니 존재 여부
  boolean existsByBuyerId(Long buyerId);
}