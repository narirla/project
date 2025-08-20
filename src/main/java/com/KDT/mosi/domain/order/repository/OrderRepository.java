package com.KDT.mosi.domain.order.repository;

import com.KDT.mosi.domain.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // 구매자 주문 목록
  List<Order> findByBuyerId(Long buyerId);

  // 주문 코드로 조회
  Optional<Order> findByOrderCodeAndBuyerId(String orderCode, Long buyerId);

  // 구매자 주문 개수
  int countByBuyerId(Long buyerId);
}