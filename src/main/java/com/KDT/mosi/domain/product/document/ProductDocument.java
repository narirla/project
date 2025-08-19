package com.KDT.mosi.domain.product.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(indexName = "products") // Elasticsearch 인덱스 이름
@Setting(settingPath = "/elasticsearch/settings.json") // 한글 분석기(nori) 설정을 위한 파일 경로
public class ProductDocument {

  @Id
  private Long productId; // 1. 상품 고유 ID

  @Field(type = FieldType.Keyword)
  private Long memberId; // 2. 회원(판매자) 고유 ID

  @Field(type = FieldType.Keyword)
  private String category; // 3. 상품 카테고리

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String title; // 4. 상품명

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String nickname; // 5. 별명

  @Field(type = FieldType.Integer)
  private Integer salesPrice; // 8. 판매가

  @Field(type = FieldType.Integer)
  private Integer salesGuidePrice; // 9. 판매가(가이드 포함)

  @Field(type = FieldType.Integer)
  private Integer totalDay; // 10. 여행 소요일

  @Field(type = FieldType.Integer)
  private Integer totalTime; // 11. 여행 소요시간

  @Field(type = FieldType.Integer)
  private Integer reqMoney; // 12. 인당 최소 경비

  @Field(type = FieldType.Keyword)
  private String sleepInfo; // 13. 숙박 포함유무

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String transportInfo; // 14. 교통시설 정보

  @Field(type = FieldType.Keyword)
  private String foodInfo; // 15. 식사 포함유무

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String reqPeople; // 16. 추천인원

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String target; // 17. 추천연령대

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String stucks; // 18. 추천준비물

  @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
  private String description; // 19. 기본설명

  @Field(type = FieldType.Keyword)
  private String status; // 24. 판매상태

  @Field(type = FieldType.Date)
  private Date createDate; // 25. 등록일

  @Field(type = FieldType.Date)
  private Date updateDate; // 26. 수정일
}