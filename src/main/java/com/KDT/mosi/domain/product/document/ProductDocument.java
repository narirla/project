package com.KDT.mosi.domain.product.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(shards = 1, replicas = 0, settingPath = "elasticsearch/product-settings.json")
public class ProductDocument {

  @Id
  private String productId;

  @Field(type = FieldType.Keyword)
  private String category;

  // @MultiField를 사용하여 text와 keyword 타입 동시 정의
  @MultiField(
      mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String title;

  @MultiField(
      mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String description;

  @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer")
  private String autocomplete_title;

  @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer")
  private String autocomplete_description;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer normalPrice;

  @Field(type = FieldType.Keyword)
  private String guideYn;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer guidePrice;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer salesPrice;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer salesGuidePrice;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer totalDay;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer totalTime;

  // ✅ Long 타입을 Integer로 수정하여 DB와 일치시킴
  @Field(type = FieldType.Integer)
  private Integer reqMoney;

  @Field(type = FieldType.Text)
  private String sleepInfo;

  @Field(type = FieldType.Text)
  private String transportInfo;

  @Field(type = FieldType.Text)
  private String foodInfo;

  @Field(type = FieldType.Text)
  private String reqPeople;

  @Field(type = FieldType.Text)
  private String target;

  @Field(type = FieldType.Text)
  private String stucks;

  @Field(type = FieldType.Text)
  private String detail;

  @Field(type = FieldType.Text)
  private String fileName;

  @Field(type = FieldType.Text)
  private String fileType;

  @Field(type = FieldType.Long)
  private Long fileSize;

  @Field(type = FieldType.Text)
  private String priceDetail;

  @Field(type = FieldType.Text)
  private String gpriceDetail;

  @Field(type = FieldType.Keyword)
  private String status;

  @Field(type = FieldType.Date)
  private LocalDate createDate;

  @Field(type = FieldType.Date)
  private LocalDate updateDate;
}