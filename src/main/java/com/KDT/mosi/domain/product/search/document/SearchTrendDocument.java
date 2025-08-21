package com.KDT.mosi.domain.product.search.document;

import lombok.AllArgsConstructor; // ✨✨✨ 새로 추가
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor // ✨✨✨ 이 어노테이션을 추가합니다.
@Document(indexName = "search_trends")
public class SearchTrendDocument {

  @Id
  private String id; // 일반적으로 검색어는 고유 ID를 가집니다.

  @Field(type = FieldType.Text, name = "keyword")
  private String keyword;

  @Field(type = FieldType.Long, name = "searchCount")
  private Long searchCount;

  @Field(type = FieldType.Date, name = "searchDate")
  private LocalDate searchDate;
}