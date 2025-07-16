package com.KDT.mosi.web.form.product;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class ProductManagingForm {
  private Long productId;
  private Long memberId;

  private String nickname;
  private byte[] sellerImage;

  private String title;         // 상품명
  private String guideYn;       // 가이드 포함 여부 확인으로 가격정보 바뀜

  private int normalPrice;      // 정가
  private int guidePrice;       // 가이드 포함 정가
  private int salesPrice;       // 판매가
  private int salesGuidePrice;  // 가이드 포함 판매가

  private String description;   // 상품 설명

  private String status;        // 판매상태

  private Date createDate;      // 작성일
  private Date updateDate;      // 수정일(작성일과 다르면 화면에 수정일 표기)

  // 상품 이미지 리스트 (조회용 이미지 메타데이터)
  private List<ProductImageForm> productImages;
}
