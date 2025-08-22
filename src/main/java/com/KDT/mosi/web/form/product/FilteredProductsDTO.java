package com.KDT.mosi.web.form.product;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FilteredProductsDTO {
  private List<ProductManagingForm> content; // 필터링된 상품 목록
  private long totalCount;                 // 전체 상품 수
  private PaginationInfo pagination;       // 페이징 정보
}