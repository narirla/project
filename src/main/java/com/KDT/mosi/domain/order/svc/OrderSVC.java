package com.KDT.mosi.domain.order.svc;

import java.util.List;
import java.util.Map;

public interface OrderSVC {

  // 주문 생성
  Map<String, Object> createOrder(Long buyerId, List<Long> cartItemIds, String specialRequest);

  // 구매자 주문 목록 (마이페이지용)
  Map<String, Object> getBuyerOrders(Long buyerId);

  // 주문 상세 조회
  Map<String, Object> getOrderDetail(Long orderId, Long buyerId);

  // 주문 취소
  Map<String, Object> cancelOrder(Long orderId, Long buyerId);

  // 주문 코드로 조회
  Map<String, Object> getOrderByCode(String orderCode, Long buyerId);

  // 주문 개수 조회 (헤더용)
  int getOrderCount(Long buyerId);
}
