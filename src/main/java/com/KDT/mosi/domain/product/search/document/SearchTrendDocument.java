package com.KDT.mosi.domain.product.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField; // MultiField 임포트 확인

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "search_trends")
public class SearchTrendDocument {

  @Id
  private String id;

  // ✅ MultiField를 사용하여 Text와 Keyword 타입 동시 정의
  @MultiField(
      mainField = @Field(type = FieldType.Text, name = "keyword"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String keyword;

  @Field(type = FieldType.Long, name = "searchCount")
  private Long searchCount;

  @Field(type = FieldType.Date, name = "searchDate")
  private LocalDate searchDate;

  public void incrementSearchCount() {
    if (this.searchCount == null) {
      this.searchCount = 1L;
    } else {
      this.searchCount++;
    }
  }
}