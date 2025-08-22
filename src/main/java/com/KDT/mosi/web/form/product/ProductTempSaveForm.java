// ProductTempSaveForm.java

package com.KDT.mosi.domain.product.dto;

import com.KDT.mosi.domain.entity.Product;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ProductTempSaveForm {

  private Long productId;
  private Long memberId;
  private String nickname;
  private String title;
  private String category;
  private String guideYn;
  private Integer normalPrice;
  private Integer salesPrice;
  private Integer guidePrice;
  private Integer salesGuidePrice;
  private Integer totalDay;
  private Integer totalTime;
  private Integer reqMoney;
  private String description;
  private String detail;
  private String priceDetail;
  private String gpriceDetail;
  private String reqPeople;
  private String target;
  private String stucks;
  private String transportInfo;
  private String sleepInfo;
  private String foodInfo;
  private String status;
  // 기존의 fileName, fileType, fileSize, fileData 필드를 제거합니다.

  // ⭐⭐⭐ 수정 포인트 1 ⭐⭐⭐
  // 첨부 파일을 직접 받도록 필드 타입을 변경합니다.
  private MultipartFile documentFile; // 대표 이미지 파일
  private List<MultipartFile> productImages; // 추가 이미지 파일 리스트

  // ... (나머지 getter/setter는 Lombok의 @Data가 자동으로 생성합니다.)
}