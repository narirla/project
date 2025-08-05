package com.KDT.mosi.domain.publicdatamanage.restaurant.svc;

import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import com.KDT.mosi.domain.publicdatamanage.restaurant.dto.FoodItem;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDataProcessorService {

  private final RestTemplate restTemplate;
  private final FoodDataManagementSVC foodDataManagementSVC;

  @Value("${busan.api.common.serviceKey}")
  private String apiKey;

  @Value("${busan.api.food.url}")
  private String apiUrl;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  @Value("${kakao.api.url.geocode}")
  private String kakaoGeocodeUrl;

  /**
   * 부산 맛집 데이터를 API에서 가져와서 처리하고 Elasticsearch에 저장하는 전체 프로세스입니다.
   *
   * @throws Exception 데이터 처리 중 발생할 수 있는 예외
   */
  public void fetchAndProcessAllFoodData() throws Exception {
    log.info("Starting to fetch and process food data.");

    Optional<FoodDocument> latestDocument = foodDataManagementSVC.findLatestFoodDocument();
    long totalCountFromApi = fetchTotalCount();
    log.info("Total count from API: {}, Latest document found: {}", totalCountFromApi, latestDocument.isPresent());

    // 데이터 갱신 조건 확인:
    // 1. 가장 최근 문서가 있고, 갱신일이 30일 이내이면서
    // 2. 현재 저장된 문서의 총 개수가 API의 총 데이터 개수와 동일한 경우
    if (latestDocument.isPresent() &&
        ChronoUnit.DAYS.between(latestDocument.get().getTimestamp(), LocalDate.now()) < 30 &&
        foodDataManagementSVC.countAllFoodDocuments() == totalCountFromApi) {
      log.info("Data is up-to-date. Skipping full data refresh.");
      return;
    }

    // 조건에 맞지 않으면 기존 데이터 삭제 후 재로딩
    log.info("Data is outdated or incomplete. Deleting all existing documents and reloading.");
    foodDataManagementSVC.deleteAllFoodDocuments();

    int numOfRows = 100;
    long totalPages = (long) Math.ceil((double) totalCountFromApi / numOfRows);

    log.info("Fetching a total of {} pages with {} rows per page.", totalPages, numOfRows);

    for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
      List<FoodItem> items = fetchFoodData(pageNo, numOfRows);
      if (items != null && !items.isEmpty()) {
        List<FoodDocument> documents = processFoodData(items);
        foodDataManagementSVC.saveAllFoodDocuments(documents);
        log.info("Page {} processed and saved. ({} documents)", pageNo, documents.size());
      }
    }
    log.info("Finished fetching and processing all food data.");
  }


  /**
   * API에서 전체 데이터의 총 개수를 조회합니다.
   * @return API가 제공하는 총 데이터 개수
   */
  private long fetchTotalCount() {
    try {
      URI uri = UriComponentsBuilder.fromUriString(apiUrl)
          .queryParam("serviceKey", apiKey)
          .queryParam("pageNo", 1)
          .queryParam("numOfRows", 1)
          .build()
          .encode()
          .toUri();

      ResponseEntity<FoodResponse> response = restTemplate.getForEntity(uri, FoodResponse.class);

      if (response.getBody() != null && response.getBody().getBody() != null) {
        return response.getBody().getBody().getTotalCount();
      }
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error("총 데이터 개수 조회 실패: HTTP 오류 - {}", e.getMessage(), e);
    } catch (Exception e) {
      log.error("총 데이터 개수 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
    }
    return 0;
  }

  /**
   * 특정 페이지의 맛집 데이터를 API에서 가져옵니다.
   * @param pageNo 페이지 번호
   * @param numOfRows 한 페이지당 가져올 데이터 수
   * @return FoodItem 리스트
   */
  private List<FoodItem> fetchFoodData(int pageNo, int numOfRows) {
    try {
      URI uri = UriComponentsBuilder.fromUriString(apiUrl)
          .queryParam("serviceKey", apiKey)
          .queryParam("pageNo", pageNo)
          .queryParam("numOfRows", numOfRows)
          .build()
          .encode()
          .toUri();

      ResponseEntity<FoodResponse> response = restTemplate.getForEntity(uri, FoodResponse.class);

      if (response.getBody() != null && response.getBody().getBody() != null && response.getBody().getBody().getItems() != null) {
        return response.getBody().getBody().getItems().getItem();
      }
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error("데이터 로딩 중 HTTP 오류 발생: {}", e.getMessage(), e);
    } catch (Exception e) {
      log.error("데이터 로딩 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }


  /**
   * FoodItem 리스트를 FoodDocument 리스트로 변환합니다.
   * @param items 변환할 FoodItem 리스트
   * @return FoodDocument 리스트
   */
  private List<FoodDocument> processFoodData(List<FoodItem> items) {
    return items.stream()
        .map(item -> {
          if (item.getMainImgThumb() != null) {
            log.info("처리 중인 맛집의 이미지 URL: {}", item.getMainImgThumb());
          }
          // 1. 문자열 필드 클리닝
          String cleanAddr1 = cleanString(item.getAddr1());
          String cleanUsageTime = cleanString(item.getUsageDayWeekAndTime());
          String cleanItemContents = cleanString(item.getItemcntnts());

          // 2. 주메뉴 문자열을 파싱
          List<String> rprsntvMenu = processRprsntvMenuString(item.getRprsntvMenu());

          // 3. 지오코딩 (lat, lng 필드가 이미 있다면 사용하고, 없다면 주소로 지오코딩)
          GeoPoint geoPoint = (item.getLat() != 0 && item.getLng() != 0)
              ? new GeoPoint(item.getLat(), item.getLng())
              : geocodeAddress(cleanAddr1);

          return FoodDocument.builder()
              .ucSeq(item.getUcSeq())
              // .mainTitle(item.getMainTitle()) // mainTitle 필드는 title과 중복되므로 주석 처리
              .gugunNm(item.getGugunNm())
              .geoPoint(geoPoint)
              .title(item.getTitle())
              .subtitle(item.getSubtitle())
              .addr1(cleanAddr1)
              .addr2(cleanString(item.getAddr2()))
              .cntctTel(item.getCntctTel())
              .homepageUrl(item.getHomepageUrl())
              .usageDayWeekAndTime(cleanUsageTime)
              .rprsntvMenu(rprsntvMenu)
              .mainImgNormal(item.getMainImgNormal())
              .mainImgThumb(item.getMainImgThumb())
              .itemcntnts(cleanItemContents)
              .timestamp(LocalDate.now())
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 주메뉴 문자열에서 가격 정보를 제거하고 메뉴 항목별로 분리하여 리스트로 반환합니다.
   *
   * @param menuText 파싱할 원본 주메뉴 문자열
   * @return 정리된 메뉴 항목 리스트
   */
  private List<String> processRprsntvMenuString(String menuText) {
    if (menuText == null || menuText.trim().isEmpty()) {
      return Collections.emptyList();
    }

    // 1. 가격 정보를 포함하는 모든 패턴을 제거합니다.
    // - "숫자", "숫자,숫자", "숫자/숫자" 형태의 가격 (예: 9000, 15,000, 15000/16000)
    // - "숫자 원" 형태의 가격 (예: 10000원)
    // - "숫자 만원" 형태의 가격 (예: 1만원)
    String cleanedText = menuText
        .replaceAll("\\s*\\d+(?:,\\d{3})*(?:\\s*(?:원|만원))?", "") // 숫자+원/만원 제거 (예: 10000원)
        .replaceAll("\\s*[0-9]+(?:/[0-9]+)?", "") // 숫자+슬래시+숫자 제거 (예: 15000/16000)
        .replaceAll("\\s*[￦,]+", "") // '￦' 기호와 콤마 제거
        .replaceAll("\\s+", " ") // 연속된 공백을 하나의 공백으로 축소
        .trim();

    // 2. 남은 문자열을 쉼표(,), 슬래시(/), 공백을 기준으로 분리합니다.
    // 이는 "물밀면, 비빔밀면" 또는 "물밀면 / 비빔밀면" 같은 경우를 처리합니다.
    String[] itemsArray = cleanedText.split("[,/\\s]+");

    // 3. 리스트로 변환하고, 각 항목의 앞뒤 공백을 제거한 후 비어있는 항목을 걸러냅니다.
    List<String> items = Arrays.stream(itemsArray)
        .map(String::trim)
        .filter(item -> !item.isEmpty())
        .collect(Collectors.toList());

    // 4. 최종적으로 리스트에 중복되는 항목이 있으면 제거합니다.
    return items.stream().distinct().collect(Collectors.toList());
  }

  /**
   * 가격 문자열에 3자리마다 콤마를 추가합니다.
   *
   * @param priceString 포매팅할 가격 문자열 (예: "9000" 또는 "15000/16000")
   * @return 포매팅된 문자열 (예: "9,000" 또는 "15,000/16,000")
   */
  private String formatPrice(String priceString) {
    if (priceString == null || priceString.trim().isEmpty()) {
      return "";
    }

    // '/'가 포함된 경우 (예: 15000/16000)를 처리
    if (priceString.contains("/")) {
      String[] prices = priceString.split("/");
      return Arrays.stream(prices)
          .map(this::formatSinglePrice)
          .collect(Collectors.joining(" / "));
    } else {
      return formatSinglePrice(priceString);
    }
  }

  /**
   * 단일 가격 문자열에 3자리마다 콤마를 추가합니다.
   *
   * @param priceStr 포매팅할 가격 문자열
   * @return 포매팅된 문자열
   */
  private String formatSinglePrice(String priceStr) {
    try {
      long priceValue = Long.parseLong(priceStr.replaceAll("[^\\d]", ""));
      NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
      return nf.format(priceValue);
    } catch (NumberFormatException e) {
      return priceStr;
    }
  }


  /**
   * 문자열에서 줄바꿈 문자를 제거하고 앞뒤 공백을 정리합니다.
   * @param text 정리할 문자열
   * @return 정제된 문자열
   */
  private String cleanString(String text) {
    if (text == null) {
      return null;
    }
    return text.replace("\n", " ").trim();
  }

  /**
   * 카카오 Geocoding API를 사용하여 주소를 위도와 경도로 변환
   * @param address 변환할 주소
   * @return 변환된 위도, 경도 정보가 담긴 GeoPoint 객체, 실패 시 null
   */
  private GeoPoint geocodeAddress(String address) {
    if (address == null || address.trim().isEmpty()) {
      return null;
    }

    try {
      String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
      URI uri = UriComponentsBuilder.fromUriString(kakaoGeocodeUrl)
          .queryParam("query", encodedAddress)
          .build()
          .toUri();

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoApiKey);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        int latIndex = response.getBody().indexOf("\"y\":\"");
        int lngIndex = response.getBody().indexOf("\"x\":\"");
        if (latIndex != -1 && lngIndex != -1) {
          String latStr = response.getBody().substring(latIndex + 5, response.getBody().indexOf("\"", latIndex + 5));
          String lngStr = response.getBody().substring(lngIndex + 5, response.getBody().indexOf("\"", lngIndex + 5));
          double latitude = Double.parseDouble(latStr);
          double longitude = Double.parseDouble(lngStr);
          return new GeoPoint(latitude, longitude);
        }
      }
    } catch (Exception e) {
      log.error("Geocoding failed for address: {}", address, e);
    }
    return null;
  }

  // --- DTO 클래스들은 동일하게 유지 ---
  @JacksonXmlRootElement(localName = "response")
  @lombok.Data
  public static class FoodResponse {
    @JacksonXmlProperty(localName = "header")
    private FoodHeader header;

    @JacksonXmlProperty(localName = "body")
    private FoodBody body;
  }

  @lombok.Data
  private static class FoodHeader {
    @JacksonXmlProperty(localName = "resultCode")
    private String resultCode;
    @JacksonXmlProperty(localName = "resultMsg")
    private String resultMsg;
  }

  @lombok.Data
  private static class FoodBody {
    @JacksonXmlProperty(localName = "items")
    private FoodItems items;

    @JacksonXmlProperty(localName = "numOfRows")
    private int numOfRows;

    @JacksonXmlProperty(localName = "pageNo")
    private int pageNo;

    @JacksonXmlProperty(localName = "totalCount")
    private long totalCount;
  }

  @lombok.Data
  private static class FoodItems {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<FoodItem> item;
  }
}