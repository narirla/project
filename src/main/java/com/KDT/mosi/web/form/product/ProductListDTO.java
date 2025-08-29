package com.KDT.mosi.web.form.product;

import lombok.Data;
import java.util.List;

@Data
public class ProductListDTO {
  private Long productId;
  private String title;
  private String category;
  private Integer salesPrice;   // ⚠️ Integer 로 변경
  private Integer normalPrice;  // ⚠️ Integer 로 변경

  private List<ProductImageForm> images;
  private List<ProductCoursePointForm> coursePoints;
  private long countProduct;
}
