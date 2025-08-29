package com.KDT.mosi.domain.order.repository;

import com.KDT.mosi.domain.entity.order.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  /**
   *  주문 상세 조회
   */
  List<OrderItem> findByOrderId(Long orderId);

  /**
   * 판매자별 주문 아이템 조회 (페이징)
   */
  Page<OrderItem> findBySellerId(Long sellerId, Pageable pageable);

  /**
   * 판매자별 주문 아이템 조회 (전체)
   */
  List<OrderItem> findBySellerId(Long sellerId);

  /**
   * 판매자별 주문 아이템 개수 조회
   */
  int countBySellerId(Long sellerId);

  /**
   * 특정 주문의 특정 판매자 상품만 조회
   */
  List<OrderItem> findByOrderIdAndSellerId(Long orderId, Long sellerId);
}