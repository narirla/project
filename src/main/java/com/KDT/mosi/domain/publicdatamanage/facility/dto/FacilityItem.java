package com.KDT.mosi.domain.publicdatamanage.facility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 부산 공공데이터 API 응답의 실제 데이터 항목(item)을 담는 DTO 클래스입니다.
 * @JsonProperty를 사용하여 JSON 키와 자바 변수명을 매핑합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class FacilityItem {

  /** 상호명 */
  @JsonProperty("subject")
  private String subject;

  /** 시설 상세 정보가 담긴 문자열 */
  @JsonProperty("contents")
  private String contents;

  /** 게시판 코드 */
  @JsonProperty("boardCode")
  private String boardCode;

  /** 게시판 코드명 */
  @JsonProperty("boardCodeNm")
  private String boardCodeNm;

  /** 등록일 */
  @JsonProperty("registerDate")
  private String registerDate;

  /** 시설 정보 코드값 */
  @JsonProperty("setValue")
  private String setValue;

  /** 시설 정보 코드명 (파이프(|)로 구분된 문자열) */
  @JsonProperty("setValueNm")
  private String setValueNm;

  /** 구분 (공백으로 구분된 문자열) */
  @JsonProperty("gubun")
  private String gubun;

  /** 이미지 URL */
  @JsonProperty("imgUrl")
  private String imgUrl;
}