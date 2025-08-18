package com.KDT.mosi.domain.order.svc;

import java.util.List;
import java.util.Map;

public interface OrderSVC {
  // 주문 생성
  Map<String, Object> createOrder(Long buyerId, List<Long> cartItemIds, String specialRequest);

  // 주문 상세 조회
  Map<String, Object> getOrderDetail(String orderCode, Long buyerId);

  // 내 주문 목록 조회
  Map<String, Object> getMyOrders(Long buyerId);

  // 주문 취소
  Map<String, Object> cancelOrder(String orderCode, Long buyerId);
}
