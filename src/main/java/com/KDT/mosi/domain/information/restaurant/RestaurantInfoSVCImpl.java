package com.KDT.mosi.domain.information.restaurant;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RestaurantInfoSVCImpl implements RestaurantInfoSVC {

  /**
   * 부산 맛집 API 서비스 키
   */
  @Value("${busan.api.common.service-key}")
  private String serviceKey;

  /**
   * 부산 맛집 API 기본 URL
   */
  @Value("${busan.api.food.url}")
  private String baseUrl;

  // JSON 데이터를 Java 객체로 변환
  private final ObjectMapper objectMapper;

  public RestaurantInfoSVCImpl() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * 전체 맛집 목록
   * 공공데이터 OPEN API 호출
   * @return 전체 맛집 목록
   */
  @Override
  public List<RestaurantInfoDocument> getRestaurants() {
    try {
      // API 호출
      String jsonResponse = callBusanFoodApi();
      if (jsonResponse == null) {
        return new ArrayList<>();
      }

      // JSON 파싱 (JSON 데이터를 Java 객체로 변환)
      BusanApiResponse apiResponse = objectMapper.readValue(jsonResponse, BusanApiResponse.class);

      // 응답 유효성 검증
      if (!isValidResponse(apiResponse)) {
        return new ArrayList<>();
      }

      // 데이터 추출 (getFoodKr.item 배열)
      return apiResponse.getGetFoodKr().getItem();

    } catch (Exception e) {
      log.error("부산 맛집 API 호출 중 오류", e);
      return new ArrayList<>();
    }
  }

  /**
   * 필터링된 맛집 조회
   * @param search   검색어
   * @param district 구군별
   * @param cuisineType 업종별
   * @return 필터링된 맛집 목록
   */
  @Override
  public List<RestaurantInfoDocument> getFilteredRestaurants(String search, String district, String cuisineType) {
    // 전체 데이터 조회
    List<RestaurantInfoDocument> allRestaurants = getRestaurants();

    // 조건별 필터링
    return allRestaurants.stream()
        .filter(r -> matchesSearch(r, search))     // 검색어 필터
        .filter(r -> matchesDistrict(r, district)) // 구군 필터
        .filter(r -> matchesCuisineType(r, cuisineType)) // 업종 필터
        .collect(Collectors.toList());
  }

  /**
   * 지도용 맛집 조회
   * @return 좌표가 있는 맛집 목록
   */
  @Override
  public List<RestaurantInfoDocument> getRestaurantsForMap() {
    return getRestaurants().stream() // List → Stream 변환
        .filter(r -> r.getLat() != null && r.getLng() != null) // 좌표가 있는 것만 필터링
        .collect(Collectors.toList()); // Stream → List로 변환
  }

  /**
   * 검색 자동완성
   * @param query 검색어
   * @param limit 최대 결과 수
   * @return 자동완성 목록
   */
  @Override
  public List<String> getSearchSuggestions(String query, int limit) {
    // 입력값 검증
    if (query == null || query.trim().isEmpty() || query.trim().length() < 2) {
      return new ArrayList<>();
    }

    // 검색어 정규화
    String searchQuery = query.toLowerCase().trim();
    // 결과 수 제한
    limit = Math.max(1, Math.min(10, limit));

    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getMainTitle) // 맛집명 추출
        .filter(title -> title != null)
        .filter(title -> title.toLowerCase().contains(searchQuery)) // 검색어 매칭
        .distinct() // 중복 제거
        .limit(limit) // 개수 제한
        .collect(Collectors.toList());
  }

  /**
   * 구군 목록 조회
   * @return 구군 목록
   */
  @Override
  public List<String> getDistrictList() {
    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getGugunNm) // 구군명 추출
        .filter(district -> district != null && !district.trim().isEmpty())
        .distinct() // 중복 제거
        .sorted() // 가나다순 정렬
        .collect(Collectors.toList());
  }

  /**
   * 업종 목록 조회
   * @return 업종 목록
   */
  @Override
  public List<String> getCuisineTypeList() {
    return getRestaurants().stream()
        .map(this::extractCuisineType) // 업종명 추출
        .filter(cuisineType -> cuisineType != null && !"기타".equals(cuisineType))
        .distinct() // 중복 제거
        .sorted() // 가나다순 정렬
        .collect(Collectors.toList());
  }

  /**
   * 검색어 조건 확인
   */
  private boolean matchesSearch(RestaurantInfoDocument restaurant, String search) {
    // 검색어가 없으면 모든 맛집이 매칭됨
    if (search == null || search.trim().isEmpty()) {
      return true;
    }

    String searchLower = search.toLowerCase();
    return (restaurant.getMainTitle() != null && restaurant.getMainTitle().toLowerCase().contains(searchLower)) ||  // 맛집명
        (restaurant.getAddr1() != null && restaurant.getAddr1().toLowerCase().contains(searchLower)) ||             // 주소
        (restaurant.getRprsnTvMenu() != null && restaurant.getRprsnTvMenu().toLowerCase().contains(searchLower));   // 대표메뉴
  }

  /**
   * 구군 필터링
   */
  private boolean matchesDistrict(RestaurantInfoDocument restaurant, String district) {
    if (district == null || district.trim().isEmpty()) {
      return true;
    }
    return district.equals(restaurant.getGugunNm()); // 정확히 일치해야 함
  }

  /**
   * 업종 필터링
   */
  private boolean matchesCuisineType(RestaurantInfoDocument restaurant, String cuisineType) {
    if (cuisineType == null || cuisineType.trim().isEmpty()) {
      return true;
    }

    String restaurantCuisineType = extractCuisineType(restaurant);
    return cuisineType.equals(restaurantCuisineType);
  }

  /**
   * 업종 추출
   */
  private String extractCuisineType(RestaurantInfoDocument restaurant) {
    String[] texts = {
        restaurant.getRprsnTvMenu(),
        restaurant.getMainTitle(),
        restaurant.getItemCntnts()
    };

    for (String text : texts) {
      if (text == null || text.trim().isEmpty()) continue;

      String t = text.toLowerCase();

      if (t.contains("한식") || t.contains("국") || t.contains("탕") || t.contains("찜") || t.contains("밥") || t.contains("구이") || t.contains("수육")) return "한식";
      if (t.contains("중식") || t.contains("짜장") || t.contains("짬뽕") || t.contains("탕수육")) return "중식";
      if (t.contains("일식") || t.contains("초밥") || t.contains("라멘") || t.contains("사시미")) return "일식";
      if (t.contains("양식") || t.contains("버거") || t.contains("파스타") || t.contains("스테이크")) return "양식";
      if (t.contains("치킨") || t.contains("닭") || t.contains("후라이드")) return "치킨";
      if (t.contains("카페") || t.contains("커피") || t.contains("아메리카노") || t.contains("빵")) return "카페";
      if (t.contains("분식") || t.contains("떡볶이") || t.contains("김밥") || t.contains("순대")) return "분식";
      if (t.contains("해산물") || t.contains("회") || t.contains("횟집") || t.contains("조개")) return "해산물";
    }

    return "기타";
  }

  /**
   * 부산 맛집 API 호출
   * @return API 응답 JSON
   */
  private String callBusanFoodApi() {
    try {
      // API URL 구성
      StringBuilder urlBuilder = new StringBuilder(baseUrl + "/getFoodKr");
      urlBuilder.append("?serviceKey=").append(serviceKey);
      urlBuilder.append("&numOfRows=500");        // 한번에 가져올 데이터 개수
      urlBuilder.append("&pageNo=1");             // 페이지 번호
      urlBuilder.append("&resultType=json");      // 응답 형식
      urlBuilder.append("&MobileOS=ETC");         // 모바일 OS 구분
      urlBuilder.append("&MobileApp=Mosi");       // 앱 이름

      String fullUrl = urlBuilder.toString();

      // HTTP 연결
      URL url = new URL(urlBuilder.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0");
      conn.setConnectTimeout(10000);  // 연결 타임아웃 10초
      conn.setReadTimeout(15000);     // 읽기 타임아웃 15초

      int responseCode = conn.getResponseCode();

      // 응답 처리
      BufferedReader rd;
      if (responseCode >= 200 && responseCode <= 300) {
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      } else {
        log.error("API 호출 실패: HTTP {}", responseCode);
        return null;
      }

      // 응답 읽기
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
      rd.close();
      conn.disconnect();

      String response = sb.toString();

      // JSON 형식 체크
      if (response.trim().startsWith("{")) {
        return response;
      // XML 응답 (예상과 다름)
      } else {
        log.warn("JSON이 아닌 응답 수신");
        return null;
      }
    } catch (Exception e) {
      log.error("API 호출 중 예외 발생", e);
      return null;
    }
  }

  /**
   * API 응답 유효성 검증
   */
  private boolean isValidResponse(BusanApiResponse apiResponse) {
    if (apiResponse == null || apiResponse.getGetFoodKr() == null) {
      log.warn("API 응답 구조 오류");
      return false;
    }

    // API 성공 코드 확인
    BusanHeader header = apiResponse.getGetFoodKr().getHeader();
    if (header != null && !"00".equals(header.getCode())) {
      log.error("API 오류 - 코드: {}, 메시지: {}", header.getCode(), header.getMessage());
      return false;
    }

    if (apiResponse.getGetFoodKr().getItem() == null) {
      log.warn("맛집 데이터 없음");
      return false;
    }

    return true;
  }

  // ================ DTO Classes ================

  /**
   * API 응답 최상위 DTO
   */
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BusanApiResponse {
    @JsonProperty("getFoodKr")
    private BusanFoodData getFoodKr;
  }

  /**
   * 부산 맛집 데이터 DTO
   */
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BusanFoodData {
    private BusanHeader header;
    @JsonProperty("item")
    private List<RestaurantInfoDocument> item = new ArrayList<>();
    private int numOfRows;
    private int pageNo;
    private int totalCount;
  }

  /**
   * API 응답 헤더 DTO
   */
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BusanHeader {
    private String code;
    private String message;
  }
}