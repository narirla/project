package com.KDT.mosi.domain.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartFormRequest {
  @NotNull(message = "상품 ID는 필수입니다")
  private Long productId;

  @NotBlank(message = "옵션 선택은 필수입니다")
  private String optionType;

  @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
  private Long quantity;
}