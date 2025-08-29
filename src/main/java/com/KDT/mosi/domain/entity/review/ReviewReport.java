package com.KDT.mosi.domain.entity.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewReport {
  private Long reviewId;
  private Long memberId;
  private String reason;
  private LocalDateTime reportDate;
}