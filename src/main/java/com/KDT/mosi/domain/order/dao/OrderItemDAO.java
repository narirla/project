package com.KDT.mosi.domain.order.dao;

import com.KDT.mosi.domain.entity.order.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemDAO {

  // 주문 아이템 생성
  OrderItem insert(OrderItem orderItem);

  // 주문 아이템 수정
  OrderItem update(OrderItem orderItem);

  // 주문 아이템 삭제
  void delete(Long orderItemId);

  // 주문 아이템 ID로 조회
  Optional<OrderItem> findById(Long orderItemId);

  // 주문 ID로 모든 주문 아이템 조회
  List<OrderItem> findByOrderId(Long orderId);

  // 특정 주문의 특정 상품 조회
  Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}