package com.KDT.mosi.domain.order.repository;

import com.KDT.mosi.domain.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  // 주문 ID로 주문 상세 조회
  List<OrderItem> findByOrderId(Long orderId);
}