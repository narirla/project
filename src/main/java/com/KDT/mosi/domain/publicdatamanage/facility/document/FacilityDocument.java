package com.KDT.mosi.domain.publicdatamanage.facility.document;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "facility_info_processed")
public class FacilityDocument {

  @Id
  @Field(type = FieldType.Long)
  private Long uid;

  @Field(type = FieldType.Keyword)
  private String subject;

  // --- contents 필드에서 추출될 새로운 필드들 ---
  @Field(type = FieldType.Keyword)
  private String tel; // 전화번호

  @Field(type = FieldType.Text)
  private String addr; // 주소

  @Field(type = FieldType.Text)
  private String location; // 위치

  @Field(type = FieldType.Keyword)
  private List<String> mainMenu; // 주메뉴 (List<String>으로 변경)

  @Field(type = FieldType.Keyword)
  private String lastUpdated; // 마지막 갱신일

  @Field(type = FieldType.Keyword)
  private String imgUrl; // 이미지 URL

  // --- 기존 필드 변경 및 제거 ---
  @Field(type = FieldType.Keyword)
  private String registerDate;

  @Field(type = FieldType.Keyword)
  private List<String> setValueNm;

  @Field(type = FieldType.Keyword)
  private List<String> gubun;

  // --- 새롭게 추가될 필드들 ---
  @Field(type = FieldType.Text)
  private String tableCount; // 테이블 수 필드 추가 (TEXT 타입)

  @Field(type = FieldType.Date)
  private LocalDate timestamp; // 데이터 가공 시점의 타임스탬프 추가

  @GeoPointField
  private GeoPoint geoPoint; // 위도(lat), 경도(lon) 필드를 하나로 묶어 추가
}