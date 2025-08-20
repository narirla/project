package com.KDT.mosi.domain.product.search.document;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Builder
@NoArgsConstructor // NoArgsConstructor는 Builder와 함께 사용할 때 필요
@Document(indexName = "search_trends")
public class SearchTrendDocument {

  @Id // 키워드를 문서의 ID로 사용
  private String keyword;

  @Field(type = FieldType.Integer)
  private Integer searchCount;

  // Builder와 함께 사용하기 위한 생성자
  public SearchTrendDocument(String keyword, Integer searchCount) {
    this.keyword = keyword;
    this.searchCount = searchCount;
  }
}