package com.KDT.mosi.domain.product.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting; // Setting 어노테이션 추가

import java.time.LocalDate; // java.sql.Date 대신 LocalDate 사용 (Elasticsearch에 적합)

@Getter
@Builder // Lombok의 Builder 패턴을 사용하여 객체 생성 용이
@Document(indexName = "products") // Elasticsearch 인덱스 이름 지정
@Setting(settingPath = "elasticsearch/product-settings.json") // 인덱스 설정을 외부 JSON 파일에서 로드
public class ProductDocument {

  @Id // Elasticsearch의 _id 필드에 매핑
  private String productId; // Long 대신 String으로 변경 (ID로 사용될 경우)

  @Field(type = FieldType.Keyword) // 정확한 값 매칭을 위해 Keyword 타입
  private String category;

  @Field(type = FieldType.Text, analyzer = "nori") // nori 분석기 적용
  private String title;

  @Field(type = FieldType.Text, analyzer = "nori") // nori 분석기 적용
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

  @Field(type = FieldType.Text, name = "transport_info") // 검색 가능하게 Text로
  private String transportInfo;

  @Field(type = FieldType.Keyword, name = "food_info")
  private String foodInfo;

  @Field(type = FieldType.Text, name = "req_people") // "남녀노소", "친구", "가족" 등 검색 가능하게 Text
  private String reqPeople;

  @Field(type = FieldType.Text) // 타겟 (예: "20대", "직장인") 검색 가능하게 Text
  private String target;

  @Field(type = FieldType.Text)
  private String stucks;

  @Field(type = FieldType.Text) // detail 필드도 필요하다면 검색 가능하게 Text
  private String detail;

  // 파일 관련 정보는 Elasticsearch에서 직접 검색하기보다 DB에서 가져오는 것이 일반적
  // 만약 파일 이름으로 검색해야 한다면 FieldType.Keyword 또는 Text로 매핑
  @Field(type = FieldType.Keyword, name = "file_name")
  private String fileName;

  @Field(type = FieldType.Keyword, name = "file_type")
  private String fileType;

  @Field(type = FieldType.Long, name = "file_size")
  private Long fileSize;

  // fileData (byte[])는 Elasticsearch에 저장하지 않습니다.
  // byte[] fileData;

  @Field(type = FieldType.Text, name = "price_detail")
  private String priceDetail;

  @Field(type = FieldType.Text, name = "gprice_detail")
  private String gpriceDetail;

  @Field(type = FieldType.Keyword)
  private String status;

  @Field(type = FieldType.Date)
  private LocalDate createDate; // Date 대신 LocalDate 사용

  @Field(type = FieldType.Date)
  private LocalDate updateDate; // Date 대신 LocalDate 사용

  // ProductImage는 Elasticsearch에 직접 저장하기보다 관련 ID를 통해 조회하거나,
  // 이미지 URL 등의 간단한 정보만 저장하는 것이 일반적입니다.
  // private List<ProductImage> productImages;
}