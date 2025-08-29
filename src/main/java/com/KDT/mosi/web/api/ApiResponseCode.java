package com.KDT.mosi.web.api;

import java.util.Arrays;

/**
 *  REST API 응답메세지 헤더에 사용되는 응답코드, 응답메세지 상수화
 */
public enum ApiResponseCode {
  // 성공 응답
  SUCCESS("S00", "성공"),
  SERVER_ERROR("S01", "서버 오류가 발생했습니다."),

  // 인증 관련
  LOGIN_REQUIRED("A01", "로그인이 필요합니다."),

  // 공통 예외
  INVALID_PARAMETER("C01", "잘못된 요청 파라미터입니다."),
  FILE_TOO_LARGE("C02", "업로드 가능한 최대 파일 용량을 초과했습니다."),
  VALIDATION_ERROR("E01", "유효성 검증 오류가 발생했습니다."),
  BUSINESS_ERROR("E02", "비즈니스 오류가 발생했습니다."),
  ENTITY_NOT_FOUND("E03", "엔티티를 찾을 수 없습니다."),
  BAD_REQUEST("R01", "잘못된 요청입니다."),

  // 데이터 관련
  NO_DATA("N04", "데이터를 찾을 수 없습니다."),

  // 상품 관련
  PRICE_CHANGED("P01", "상품 가격이 변경되었습니다."),
  PRODUCT_UNAVAILABLE("P02", "상품을 사용할 수 없습니다."),
  PRODUCT_DISCONTINUED("P03", "현재 판매가 중단된 상품입니다."),

  // 장바구니 관련
  CART_ITEM_ALREADY_EXISTS("C11", "이미 장바구니에 동일한 상품이 존재합니다."),
  CART_ITEM_NOT_FOUND("C12", "장바구니에서 해당 상품을 찾을 수 없습니다."),

  // 결제 관련
  PAYMENT_FAILED("M01", "결제에 실패했습니다."),

  // 사용자 관리
  USER_NOT_FOUND("U01", "사용자를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS("U02", "사용자가 이미 존재합니다."),
  INVALID_PASSWORD("U03", "잘못된 비밀번호입니다."),

  // 시스템 예외
  INTERNAL_SERVER_ERROR("999","내부 서버 오류");


  private final String rtcd;
  private final String rtmsg;

  ApiResponseCode(String rtcd, String rtmsg) {
    this.rtcd = rtcd;
    this.rtmsg = rtmsg;
  }

  public String getRtcd() {
    return rtcd;
  }

  public String getRtmsg() {
    return rtmsg;
  }

  // 코드로 enum 조회
  public static ApiResponseCode of(String code) {
    return Arrays.stream(values())
        .filter(rc -> rc.getRtcd().equals(code))
        .findFirst()
        .orElse(INTERNAL_SERVER_ERROR);
  }

}
