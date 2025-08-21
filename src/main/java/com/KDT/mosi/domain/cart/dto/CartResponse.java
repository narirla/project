package com.KDT.mosi.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장바구니 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

  private boolean success;
  private boolean empty;
  private String message;
  private String memberNickname;
  private Long memberId;

  private List<CartItemResponse> cartItems;
  private Long totalCount;
  private Long totalQuantity;
  private Long totalPrice;

  /**
   * 빈 장바구니 응답
   */
  public static CartResponse createEmptyCart(String memberNickname, Long memberId) {
    return CartResponse.builder()
        .success(true)
        .empty(true)
        .message("장바구니가 비어있습니다")
        .memberNickname(memberNickname)
        .memberId(memberId)
        .cartItems(List.of())
        .totalCount(0L)
        .totalQuantity(0L)
        .totalPrice(0L)
        .build();
  }

  /**
   * 장바구니 성공 응답
   */
  public static CartResponse createSuccess(String memberNickname, Long memberId,
                                           List<CartItemResponse> cartItems,
                                           Long totalCount,
                                           Long totalQuantity,
                                           Long totalPrice) {
    return CartResponse.builder()
        .success(true)
        .empty(false)
        .memberNickname(memberNickname)
        .memberId(memberId)
        .cartItems(cartItems)
        .totalCount(totalCount)
        .totalQuantity(totalQuantity)
        .totalPrice(totalPrice)
        .build();
  }

  /**
   * 오류 응답 생성
   */
  public static CartResponse createError(String memberNickname, Long memberId, String errorMessage) {
    return CartResponse.builder()
        .success(false)
        .empty(true)
        .message(errorMessage)
        .memberNickname(memberNickname)
        .memberId(memberId)
        .cartItems(List.of())
        .totalCount(0L)
        .totalQuantity(0L)
        .totalPrice(0L)
        .build();
  }
}