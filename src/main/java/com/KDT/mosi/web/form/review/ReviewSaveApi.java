package com.KDT.mosi.web.form.review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
@Data

public class ReviewSaveApi {
  @NotNull
  private Long orderItemId;

  @NotNull
  private Double score;

  @Size(max = 2000)
  private String content;

  // 태그 id 리스트
  private List<Long> tagIds;

}
