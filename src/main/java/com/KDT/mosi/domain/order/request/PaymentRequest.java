package com.KDT.mosi.domain.order.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

  @NotNull(message = "주문 ID가 필요합니다")
  private Long orderId;

  @NotNull(message = "결제 수단을 선택해주세요")
  private String paymentMethod;

  @NotNull(message = "결제 금액이 필요합니다")
  @Positive(message = "결제 금액은 0보다 커야 합니다")


  private Long amount;
}
