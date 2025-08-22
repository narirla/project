package com.KDT.mosi.domain.publicdatamanage.facility.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.annotations.MultiField; // MultiField 임포트
import org.springframework.data.elasticsearch.annotations.InnerField; // InnerField 임포트

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Document(indexName = "facility_info_processed")
public class FacilityDocument {

  @Id
  @Field(type = FieldType.Long)
  private Long uid;

  @MultiField(
      mainField = @Field(type = FieldType.Text),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String subject;

  @MultiField(
      mainField = @Field(type = FieldType.Text),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String tel;

  // --- contents 필드에서 추출될 새로운 필드들 ---
  @Field(type = FieldType.Text)
  private String addr;

  @Field(type = FieldType.Text)
  private String location;

  @MultiField(
      mainField = @Field(type = FieldType.Keyword),
      otherFields = {
          @InnerField(suffix = "text", type = FieldType.Text)
      }
  )
  private List<String> mainMenu;

  // --- 나머지 필드는 동일 ---
  @Field(type = FieldType.Keyword)
  private String lastUpdated;

  @Field(type = FieldType.Keyword)
  private String imgUrl;

  @Field(type = FieldType.Keyword)
  private String registerDate;

  @Field(type = FieldType.Keyword)
  private List<String> setValueNm;

  @Field(type = FieldType.Keyword)
  private List<String> gubun;

  @Field(type = FieldType.Text)
  private String tableCount;

  @Field(type = FieldType.Date)
  private LocalDate timestamp;

  @GeoPointField
  private GeoPoint geoPoint;

  @Field(type = FieldType.Keyword)
  private String gugun;
}