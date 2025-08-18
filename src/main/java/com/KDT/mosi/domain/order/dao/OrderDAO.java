package com.KDT.mosi.domain.order.dao;

import com.KDT.mosi.domain.entity.order.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {

  // 주문 생성
  Order insert(Order order);

  // 주문 수정
  Order update(Order order);

  // 주문 삭제
  void delete(Long orderId);

  // 주문 ID로 조회
  Optional<Order> findById(Long orderId);

  // 주문번호와 구매자 ID로 조회
  Optional<Order> findByOrderCodeAndBuyerId(String orderCode, Long buyerId);

  // 구매자의 모든 주문 조회 (주문일 내림차순)
  List<Order> findByBuyerIdOrderByOrderDateDesc(Long buyerId);

  // 주문번호로 조회
  Optional<Order> findByOrderCode(String orderCode);
}