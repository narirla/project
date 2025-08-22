package com.KDT.mosi.domain.entity.review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewInfo {
  private Long buyerId;
  private Long productId;
  private String optionType;
  private String reviewed;
}
