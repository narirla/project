package com.KDT.mosi.domain.product.dto.response;

import com.KDT.mosi.domain.product.document.ProductDocument;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductSearchResponse {

  // Elasticsearch에서 검색된 상품 목록을 담는 필드
  private List<ProductDocument> products;

  // 검색된 상품의 전체 개수를 담는 필드
  private long totalCount;
}