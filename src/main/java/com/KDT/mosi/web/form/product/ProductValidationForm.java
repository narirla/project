package com.KDT.mosi.web.form.product;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductValidationForm {

  // 상품명
  @NotBlank(message = "상품명을 입력해주세요.")
  @Size(min = 1, max = 60, message = "상품명을 1~60자 이내로 입력해주세요.")
  private String title;

  // 카테고리
  @NotBlank(message = "카테고리를 선택해주세요.")
  private String category;

  // 가이드 동반 여부
  @NotBlank(message = "가이드 동반 여부를 선택해주세요.")
  private String guideYn;

  // 정상 가격
  @NotNull(message = "정상 가격을 입력해주세요.")
  @Min(value = 0, message = "올바른 정상 가격을 입력해주세요.")
  @Max(value = 10000000, message = "금액은 10,000,000원을 초과할 수 없습니다.")
  private Integer normalPrice;

  // 판매 가격
  @NotNull(message = "판매 가격을 입력해주세요.")
  @Min(value = 0, message = "올바른 정상 가격을 입력해주세요.")
  @Max(value = 10000000, message = "금액은 10,000,000원을 초과할 수 없습니다.") // 추가
  private Integer salesPrice;

  // 가이드 동반 가격
  @NotNull(message = "가이드동반 정상 가격을 입력해주세요.")
  @Min(value = 0, message = "올바른 정상 가격을 입력해주세요.")
  @Max(value = 10000000, message = "금액은 10,000,000원을 초과할 수 없습니다.") // 추가
  private Integer guidePrice;

  // 가이드 동반 판매 가격
  @NotNull(message = "가이드동반 판매 가격을 입력해주세요.")
  @Min(value = 0, message = "올바른 정상 가격을 입력해주세요.")
  @Max(value = 10000000, message = "금액은 10,000,000원을 초과할 수 없습니다.") // 추가
  private Integer salesGuidePrice;

  // 소요 기간
  @NotNull(message = "여행 소요일을 입력해주세요.")
  @Min(value = 0, message = "여행 소요일을 올바르게 입력해주세요.")
  private Integer totalDay;

  // 소요 시간
  @NotNull(message = "소요 시간을 입력해주세요.")
  @Min(value = 0, message = "소요 기간 또는 시간을 올바르게 입력해주세요.")
  @Max(value = 23, message = "24시간 이상 소요된다면 여행 기간에 표시해주세요.")
  private Integer totalTime;

  // 최소 여행 경비
  @NotNull(message = "최소 여행 경비를 입력해주세요.")
  @Min(value = 0, message = "올바른 최소 여행 경비를 입력해주세요.")
  @Max(value = 10000000, message = "올바른 최소 여행 경비를 입력해주세요.")
  private Integer reqMoney;

  // 상품 기본 설명
  @NotBlank(message = "상품 기본 설명을 1~500자 이내로 입력해주세요.")
  @Size(min = 1, max = 500, message = "상품 기본 설명을 1~500자 이내로 입력해주세요.")
  private String description;

  // 상품 상세 설명
  @NotBlank(message = "상품 상세 설명을 1~1000자 이내로 입력해주세요.")
  @Size(min = 1, max = 1000, message = "상품 상세 설명을 1~1000자 이내로 입력해주세요.")
  private String detail;

  // 가격 정보 상세 기입 (기본)
  @NotBlank(message = "가격 정보 상세 내용을 1~150자 이내로 입력해주세요.")
  @Size(min = 1, max = 150, message = "가격 정보 상세 내용을 1~150자 이내로 입력해주세요.")
  private String priceDetail;

  // 가격 정보 상세 기입 (가이드 동반)
  @NotBlank(message = "가이드 동반 가격 정보 상세 내용을 1~150자 이내로 입력해주세요.")
  @Size(min = 1, max = 150, message = "가이드 동반 가격 정보 상세 내용을 1~150자 이내로 입력해주세요.")
  private String gpriceDetail;

  // 추천 인원
  @NotBlank(message = "추천 인원을 입력해주세요.")
  @Size(min = 1, max = 15, message = "추천 인원을 1~15자 이내로 입력해주세요.")
  private String reqPeople;

  // 추천 대상
  @NotBlank(message = "추천 대상을 입력해주세요.")
  @Size(min = 1, max = 15, message = "추천 대상을 1~15자 이내로 입력해주세요.")
  private String target;

}