package com.KDT.mosi.domain.entity.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewList {
  private Long reviewId;
  private String content;
  private double score;
  private String sellerRecoYn;
  private LocalDateTime rcreate;
  private LocalDateTime rupdate;
  private String title;
  private LocalDateTime pcreate;
  private LocalDateTime pupdate;
  private String optionType;
  private String tagIds;
  private String tagLabels;
  private Long productImageId;
  private String productImageMime;
  private String nickname;

}
