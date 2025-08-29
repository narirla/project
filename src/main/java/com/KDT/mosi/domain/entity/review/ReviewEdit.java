package com.KDT.mosi.domain.entity.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEdit {
  private Long reviewId;
  private Long productId;
  private Long orderItemId;
  private String content;
  private double score;
  private String ids;
}
