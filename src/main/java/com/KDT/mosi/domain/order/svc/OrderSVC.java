package com.KDT.mosi.domain.order.svc;

import com.KDT.mosi.domain.order.dto.OrderResponse;
import com.KDT.mosi.domain.order.request.OrderFormRequest;
import com.KDT.mosi.web.api.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderSVC {

  // 주문서 데이터 조회
  OrderResponse getOrderForm(Long buyerId, List<Long> cartItemIds);

  // 주문 생성
  OrderResponse createOrder(Long buyerId, OrderFormRequest request);

  // 주문 상세 조회
  OrderResponse getOrderDetail(Long orderId, Long buyerId);

  // 주문번호로 주문 상세 조회
  OrderResponse getOrderDetailByCode(String orderCode, Long buyerId);

  // 주문 취소
  OrderResponse cancelOrder(Long orderId, Long buyerId);

  // 주문 목록 조회
  ApiResponse<List<OrderResponse>> getOrderHistory(Long buyerId, int page, int size);

  // 주문 개수 조회
  int getOrderCount(Long buyerId);

  // 결제 완료 처리
  OrderResponse completePayment(String orderCode, Long buyerId);

  // 판매자 판매내역 조회
  ApiResponse<List<OrderResponse>> getSellerOrderHistory(Long sellerId, int page, int size);

  // 판매자 주문 개수 조회
  int getSellerOrderCount(Long sellerId);
}
