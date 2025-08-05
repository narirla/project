package com.KDT.mosi.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Data;

@Data
@Document(indexName = "restaurant_info")
@JsonIgnoreProperties(ignoreUnknown = true)  // 알 수 없는 필드 무시
public class RestaurantInfoDocument {

  @Id
  @JsonProperty("UC_SEQ")
  private Long ucSeq;

  @Field(type = FieldType.Text)
  @JsonProperty("MAIN_TITLE")
  private String mainTitle;

  @Field(type = FieldType.Keyword)
  @JsonProperty("GUGUN_NM")
  private String gugunNm;

  @Field(type = FieldType.Double)
  @JsonProperty("LAT")
  private Double lat;

  @Field(type = FieldType.Double)
  @JsonProperty("LNG")
  private Double lng;

  @Field(type = FieldType.Text)
  @JsonProperty("PLACE")
  private String place;

  @Field(type = FieldType.Text)
  @JsonProperty("TITLE")
  private String title;

  @Field(type = FieldType.Text)
  @JsonProperty("SUBTITLE")
  private String subTitle;

  @Field(type = FieldType.Text)
  @JsonProperty("ADDR1")
  private String addr1;

  @Field(type = FieldType.Text)
  @JsonProperty("ADDR2")
  private String addr2;

  @Field(type = FieldType.Keyword)
  @JsonProperty("CNTCT_TEL")
  private String cntctTel;

  @Field(type = FieldType.Keyword)
  @JsonProperty("HOMEPAGE_URL")
  private String homepageUrl;

  @Field(type = FieldType.Text)
  @JsonProperty("USAGE_DAY_WEEK_AND_TIME")
  private String usageDayWeekAndTime;

  @Field(type = FieldType.Text)
  @JsonProperty("RPRSNTV_MENU")
  private String rprsnTvMenu;

  @Field(type = FieldType.Keyword)
  @JsonProperty("MAIN_IMG_NORMAL")
  private String mainImgNormal;

  @Field(type = FieldType.Keyword)
  @JsonProperty("MAIN_IMG_THUMB")
  private String mainImgThumb;

  @Field(type = FieldType.Text)
  @JsonProperty("ITEMCNTNTS")
  private String itemCntnts;
}