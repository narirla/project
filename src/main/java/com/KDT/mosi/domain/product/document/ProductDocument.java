package com.KDT.mosi.domain.product.document;

<<<<<<< HEAD
import lombok.AllArgsConstructor; // AllArgsConstructor 추가
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // NoArgsConstructor 추가
=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> e506fd3749059f9445a987ad395676865572bc94
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

<<<<<<< HEAD
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
=======
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
>>>>>>> e506fd3749059f9445a987ad395676865572bc94
}