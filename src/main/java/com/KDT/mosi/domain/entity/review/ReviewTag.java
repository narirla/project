package com.KDT.mosi.domain.entity.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTag {
  private Long reviewId;
  private Long tagId;
  private Long sortOrder;

}
