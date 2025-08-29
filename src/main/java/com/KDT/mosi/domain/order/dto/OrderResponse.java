package com.KDT.mosi.domain.order.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

  // 회원 정보 (MEMBER 테이블)
  private String buyerName;
  private String buyerPhone;
  private String buyerEmail;

  private Long orderId;
  private String orderCode;
  private LocalDateTime orderDate;
  private String orderStatus;
  private String specialRequest;  // 요청사항 추가
  private List<OrderItemResponse> orderItems;
  private Long totalPrice;
  private int totalItemCount;
  
  // getTotalAmount 메서드 추가 (호환성)
  public Long getTotalAmount() {
    return this.totalPrice;
  }

  /**
   * 주문서 성공 응답
   */
  public static OrderResponse createOrderFormSuccess(String buyerName, String buyerPhone, String buyerEmail,
                                                     List<OrderItemResponse> orderItems,
                                                     Long totalPrice, int totalItemCount) {
    OrderResponse response = new OrderResponse();
    response.setBuyerName(buyerName);
    response.setBuyerPhone(buyerPhone);
    response.setBuyerEmail(buyerEmail);
    response.setOrderItems(orderItems);
    response.setTotalPrice(totalPrice);
    response.setTotalItemCount(totalItemCount);
    return response;
  }

  /**
   * 주문 완료 응답
   */
  public static OrderResponse createOrderCompleteSuccess(String orderCode, Long orderId,
                                                         Long totalPrice, LocalDateTime orderDate) {
    OrderResponse response = new OrderResponse();
    response.setOrderId(orderId);
    response.setOrderCode(orderCode);
    response.setOrderDate(orderDate);
    response.setOrderStatus("결제완료");
    response.setTotalPrice(totalPrice);
    response.setOrderItems(List.of());
    return response;
  }

  /**
   * 주문 상세조회 성공 응답
   */
  public static OrderResponse createOrderDetailSuccess(String buyerName, String buyerPhone, String buyerEmail,
                                                       Long orderId, String orderCode, LocalDateTime orderDate,
                                                       String orderStatus, String specialRequest, List<OrderItemResponse> orderItems,
                                                       Long totalPrice, int totalItemCount) {
    OrderResponse response = new OrderResponse();
    response.setBuyerName(buyerName);
    response.setBuyerPhone(buyerPhone);
    response.setBuyerEmail(buyerEmail);
    response.setOrderId(orderId);
    response.setOrderCode(orderCode);
    response.setOrderDate(orderDate);
    response.setOrderStatus(orderStatus);
    response.setSpecialRequest(specialRequest);
    response.setOrderItems(orderItems);
    response.setTotalPrice(totalPrice);
    response.setTotalItemCount(totalItemCount);
    return response;
  }
}
