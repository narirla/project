package com.KDT.mosi.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CartRequest {
  @NotNull(message = "상품 ID는 필수입니다")
  private Long productId;

  @NotBlank(message = "옵션 타입은 필수입니다")
  private String optionType;

  @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
  private Long quantity;
}