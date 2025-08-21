package com.KDT.mosi.domain.product.document;

import lombok.AllArgsConstructor; // AllArgsConstructor 추가
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // NoArgsConstructor 추가
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor // ✨✨✨ Lombok을 사용해 기본 생성자 추가
@AllArgsConstructor // ✨✨✨ Lombok을 사용해 모든 필드를 인자로 받는 생성자 추가
@Document(indexName = "products")
@Setting(shards = 1, replicas = 0)
public class ProductDocument {

  @Id
  @Field(type = FieldType.Keyword)
  private String productId;

  @Field(type = FieldType.Keyword)
  private String category;

  @Field(type = FieldType.Text, analyzer = "nori") // nori 분석기 유지
  private String title;

  @Field(type = FieldType.Text, analyzer = "nori") // nori 분석기 유지
  private String description;

  @Field(type = FieldType.Integer)
  private Integer normalPrice;

  @Field(type = FieldType.Keyword, name = "guide_yn")
  private String guideYn;

  @Field(type = FieldType.Integer, name = "guide_price")
  private Integer guidePrice;

  @Field(type = FieldType.Integer, name = "sales_price")
  private Integer salesPrice;

  @Field(type = FieldType.Integer, name = "sales_guide_price")
  private Integer salesGuidePrice;

  @Field(type = FieldType.Integer, name = "total_day")
  private Integer totalDay;

  @Field(type = FieldType.Integer, name = "total_time")
  private Integer totalTime;

  @Field(type = FieldType.Integer, name = "req_money")
  private Integer reqMoney;

  @Field(type = FieldType.Keyword, name = "sleep_info")
  private String sleepInfo;

  @Field(type = FieldType.Text, name = "transport_info")
  private String transportInfo;

  @Field(type = FieldType.Keyword, name = "food_info")
  private String foodInfo;

  @Field(type = FieldType.Text, name = "req_people")
  private String reqPeople;

  @Field(type = FieldType.Text)
  private String target;

  @Field(type = FieldType.Text)
  private String stucks;

  @Field(type = FieldType.Text)
  private String detail;

  @Field(type = FieldType.Keyword, name = "file_name")
  private String fileName;

  @Field(type = FieldType.Keyword, name = "file_type")
  private String fileType;

  @Field(type = FieldType.Long, name = "file_size")
  private Long fileSize;

  @Field(type = FieldType.Text, name = "price_detail")
  private String priceDetail;

  @Field(type = FieldType.Text, name = "gprice_detail")
  private String gpriceDetail;

  @Field(type = FieldType.Keyword)
  private String status;

  @Field(type = FieldType.Date)
  private LocalDate createDate;

  @Field(type = FieldType.Date)
  private LocalDate updateDate;
}