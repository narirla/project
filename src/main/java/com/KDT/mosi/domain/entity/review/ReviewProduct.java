package com.KDT.mosi.domain.entity.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProduct {
  private Long productId;
  private String nickname;
  private String category;
  private String title;
  private LocalDateTime createDate;
  private String mimeType;
  private byte[] imageData;
  private String optionType;
}
