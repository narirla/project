package com.KDT.mosi.web.form.product;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class ProductDetailForm {
  private Long productId;
  private Long memberId;

  private String category;
  private String title;
  private String guideYn;

  private int normalPrice;
  private int guidePrice;
  private int salesPrice;
  private int salesGuidePrice;

  private int totalDay;
  private int totalTime;
  private int reqMoney;

  private String sleepInfo;
  private String transportInfo;
  private String foodInfo;

  private String reqPeople;
  private String target;
  private String stucks;

  private String description;
  private String detail;

  private String fileName;
  private String fileType;
  private Long fileSize;
  private byte[] fileData;

  private String priceDetail;
  private String gpriceDetail;

  private String status;

  private Date createDate;
  private Date updateDate;

  // 상품 이미지 리스트 (조회용 이미지 메타데이터)
  private List<ProductImageForm> productImages;

  // 상품 코스포인트 리스트
  private List<ProductCoursePointForm> coursePoints;
}