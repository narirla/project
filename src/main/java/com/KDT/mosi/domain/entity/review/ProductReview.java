package com.KDT.mosi.domain.entity.review;

import lombok.Data;

import java.time.LocalDateTime;
//제품 상세 페이지에서 리뷰를 가져올때 사용
@Data
public class ProductReview {
  private Long productId;
  private Long reviewId;
  private double score;
  private LocalDateTime rcreate;
  private String content;
  private Long buyerId;
  private String nickname;
  private int hasPic;
  private String optionType;
  private String tagIds;
  private String tagLabels;
}
