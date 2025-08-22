package com.KDT.mosi.web.form.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagInfo {
  private Long tagId;
  private String label;
  private String slug;
}