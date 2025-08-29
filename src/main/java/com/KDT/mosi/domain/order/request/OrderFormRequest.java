package com.KDT.mosi.domain.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderFormRequest {

  @NotEmpty(message = "주문할 상품을 선택해주세요")
  private List<Long> cartItemIds;

  @NotBlank(message = "주문자명은 필수입니다")
  private String ordererName;

  @NotBlank(message = "연락처는 필수입니다")
  private String phone;

  @NotBlank(message = "이메일은 필수입니다")
  private String email;

  @Size(max = 200, message = "요청사항은 200자 이내로 입력해주세요")
  private String requirements;

  @NotNull(message = "결제 방법을 선택해주세요")
  private String paymentMethod;

  @NotNull(message = "결제 금액은 필수입니다")
  @Positive(message = "결제 금액은 0보다 커야 합니다")
  private Long totalAmount;
}
