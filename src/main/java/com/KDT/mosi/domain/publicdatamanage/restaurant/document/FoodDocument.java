package com.KDT.mosi.domain.publicdatamanage.restaurant.document;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDate; // LocalDateTime 대신 LocalDate 임포트
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Document(indexName = "food_data")
public class FoodDocument {

  @Id
  @Field(name = "uc_seq", type = FieldType.Integer)
  private int ucSeq;

  @Field(name = "gugun_nm", type = FieldType.Keyword)
  private String gugunNm;

  @GeoPointField
  private GeoPoint geoPoint;

  @Field(name = "title", type = FieldType.Text)
  private String title;

  @Field(name = "subtitle", type = FieldType.Text)
  private String subtitle;

  @Field(name = "addr1", type = FieldType.Text)
  private String addr1;

  @Field(name = "addr2", type = FieldType.Text)
  private String addr2;

  @Field(name = "cntct_tel", type = FieldType.Keyword)
  private String cntctTel;

  @Field(name = "homepage_url", type = FieldType.Keyword)
  private String homepageUrl;

  @Field(name = "usage_day_week_and_time", type = FieldType.Text)
  private String usageDayWeekAndTime;

  @Field(name = "rprsntv_menu", type = FieldType.Keyword)
  private List<String> rprsntvMenu;

  @Field(name = "main_img_normal", type = FieldType.Keyword)
  private String mainImgNormal;

  @Field(name = "main_img_thumb", type = FieldType.Keyword)
  private String mainImgThumb;

  @Field(name = "itemcntnts", type = FieldType.Text)
  private String itemcntnts;

  // LocalDate 타입으로 변경하고, 패턴은 yyyy-MM-dd로 지정
  @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
  private LocalDate timestamp;

}