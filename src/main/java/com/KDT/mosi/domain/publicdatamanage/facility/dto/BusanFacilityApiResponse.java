package com.KDT.mosi.domain.publicdatamanage.facility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 부산 공공데이터 API의 JSON 응답 전체 구조를 매핑하는 DTO 클래스.
 * Lombok의 @Data 어노테이션은 getter, setter, toString 등을 자동으로 생성합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class BusanFacilityApiResponse {

  // API 응답의 최상위 객체인 "response"에 매핑
  @JsonProperty("response")
  private Response response;

  /**
   * JSON 응답의 "response" 객체에 해당하는 내부 클래스.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Response {
    @JsonProperty("header")
    private Header header;
    @JsonProperty("body")
    private Body body;
  }

  /**
   * 응답 결과 코드와 메시지를 담는 "header" 객체.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Header {
    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultMsg")
    private String resultMsg;
  }

  /**
   * 실제 데이터와 페이징 정보를 담는 "body" 객체.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Body {
    @JsonProperty("items")
    private Items items;
    @JsonProperty("numOfRows")
    private int numOfRows;
    @JsonProperty("pageNo")
    private int pageNo;
    @JsonProperty("totalCount")
    private int totalCount;
  }

  /**
   * 실제 시설 데이터 리스트를 담는 "items" 객체.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Items {
    // "item"이라는 JSON 배열을 List<FacilityItem>에 매핑합니다.
    @JsonProperty("item")
    private List<FacilityItem> itemList;
  }
}