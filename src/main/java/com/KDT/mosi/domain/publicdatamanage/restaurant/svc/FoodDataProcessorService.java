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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDataProcessorService {

  private final RestTemplate restTemplate;
  private final FoodDataManagementSVC dataManagementService;

  @Value("${busan.api.common.serviceKey}")
  private String apiKey;

  @Value("${busan.api.food.url}")
  private String apiUrl;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  @Value("${kakao.api.url.geocode}")
  private String kakaoGeocodeUrl;

  @Scheduled(cron = "0 0 */6 * * *")
  public void scheduledDataFetch() {
    log.info("스케줄러 시작: 부산 공공데이터 인덱스 초기화 및 갱신");
    try {
      fetchAndProcessAllFoodData();
    } catch (Exception e) {
      log.error("스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
    }
    log.info("스케줄러 완료: 부산 공공데이터 인덱스 초기화 및 갱신");
  }

  public void fetchAndProcessAllFoodData() throws Exception {
    log.info("Starting to fetch and process food data.");

    // 이 시점에서 인덱스가 존재하지 않으면 생성합니다.
    // dataManagementService.ensureIndexExists() 같은 메소드를 호출하여 인덱스를 미리 생성해주는 로직이 필요합니다.
    dataManagementService.ensureIndexExists();

    Optional<FoodDocument> latestDocument = dataManagementService.findLatestFoodDocument();
    long totalCountFromApi = fetchTotalCount();
    log.info("Total count from API: {}, Latest document found: {}", totalCountFromApi, latestDocument.isPresent());

    // 데이터가 최신 상태인지 확인하는 로직
    if (latestDocument.isPresent() &&
        ChronoUnit.DAYS.between(latestDocument.get().getTimestamp(), LocalDate.now()) < 1 &&
        dataManagementService.countAllFoodDocuments() == totalCountFromApi) {
      log.info("Data is up-to-date. Skipping full data refresh.");
      return;
    }

    log.info("Data is outdated or incomplete. Deleting all existing documents and reloading.");

    // 데이터가 오래되었거나 불완전할 경우에만 인덱스를 삭제하고 다시 생성합니다.
    dataManagementService.deleteFoodInfoIndex(); // <-- 여기로 이동
    dataManagementService.ensureIndexExists(); // 인덱스 삭제 후 다시 생성

    int numOfRows = 100;
    long totalPages = (long) Math.ceil((double) totalCountFromApi / numOfRows);
    log.info("Fetching a total of {} pages with {} rows per page.", totalPages, numOfRows);

    for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
      List<FoodItem> items = fetchFoodData(pageNo, numOfRows);
      if (items != null && !items.isEmpty()) {
        List<FoodDocument> documents = processFoodData(items);
        dataManagementService.saveAllFoodDocuments(documents);
        log.info("Page {} processed and saved. ({} documents)", pageNo, documents.size());
      }
    }
    log.info("Finished fetching and processing all food data.");
  }

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

  private List<FoodDocument> processFoodData(List<FoodItem> items) {
    return items.stream()
        .map(item -> {
          // 기존 위도/경도 값 가져오기 (변수명 수정)
          GeoPoint existingGeoPoint = null;
          try {
            if (item.getLat() != null && item.getLng() != null) {
              double lat = Double.parseDouble(item.getLat().toString());
              double lng = Double.parseDouble(item.getLng().toString());
              existingGeoPoint = new GeoPoint(lat, lng);
            }
          } catch (NumberFormatException e) {
            log.warn("기존 위도/경도 값 변환 실패: {}, {}", item.getLat(), item.getLng());
          }

          GeoPoint finalGeoPoint;

          // 기존 위도/경도 값이 유효한지 확인하는 로직
          if (isGeoPointValidForBusan(existingGeoPoint)) {
            finalGeoPoint = existingGeoPoint;
            log.info("기존 유효한 좌표 사용: 위도={}, 경도={}", finalGeoPoint.getLat(), finalGeoPoint.getLon());
          } else {
            // 기존 값이 없거나 유효하지 않으면 지오코딩 실행
            String cleanAddr1 = cleanString(item.getAddr1());
            log.warn("기존 좌표가 유효하지 않거나 없어 지오코딩을 시작합니다. 주소: {}", cleanAddr1);
            finalGeoPoint = geocodeAddress(cleanAddr1);
          }

          String cleanAddr1 = cleanString(item.getAddr1());
          String cleanUsageTime = cleanString(item.getUsageDayWeekAndTime());
          String cleanItemContents = cleanString(item.getItemcntnts());
          List<String> rprsntvMenu = processRprsntvMenuString(item.getRprsntvMenu());

          return FoodDocument.builder()
              .ucSeq(item.getUcSeq())
              .gugunNm(item.getGugunNm())
              .geoPoint(finalGeoPoint) // 최종 GeoPoint 사용
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
   * GeoPoint가 부산광역시의 대략적인 범위 내에 있는지 확인하는 헬퍼 메서드
   */
  private boolean isGeoPointValidForBusan(GeoPoint geoPoint) {
    if (geoPoint == null) {
      return false;
    }
    double lat = geoPoint.getLat();
    double lng = geoPoint.getLon();

    // 부산광역시 위도/경도 범위 설정
    double minLat = 34.88;
    double maxLat = 35.50;
    double minLng = 128.87;
    double maxLng = 129.35;

    return lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
  }

  private List<String> processRprsntvMenuString(String rprsntvMenuText) {
    if (rprsntvMenuText == null || rprsntvMenuText.trim().isEmpty()) {
      return Collections.emptyList();
    }
    String tempText = rprsntvMenuText;
    tempText = tempText.replaceAll("(?<=\\d),(?=\\d{3})", "");
    String[] itemsArray = tempText.split("[,\\n]");
    List<String> finalItems = new ArrayList<>();
    for (String item : itemsArray) {
      String processedItem = item.trim();
      if (processedItem.isEmpty()) {
        continue;
      }
      processedItem = processedItem.replaceAll("(?i)[₩￦][\\s0-9/-]*", "");
      processedItem = processedItem.replaceAll(" +", " ").trim();

      if (!processedItem.isEmpty()) {
        finalItems.add(processedItem);
      }
    }
    return finalItems.stream().distinct().collect(Collectors.toList());
  }

  private String cleanString(String text) {
    if (text == null) {
      return null;
    }
    return text.replace("\n", " ").trim();
  }

  private GeoPoint geocodeAddress(String address) {
    if (address == null || address.trim().isEmpty()) {
      log.warn("주소 데이터가 비어있어 지오코딩을 수행하지 않습니다.");
      return null;
    }

    String cleanedAddress = address;

    String roadAddressPattern = "([가-힣]+\\s*[로길]\\s*\\d+(?:-\\d+)?)\\b";

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(roadAddressPattern);
    java.util.regex.Matcher matcher = pattern.matcher(cleanedAddress);

    if (matcher.find()) {
      cleanedAddress = cleanedAddress.substring(0, matcher.end()).trim();
    }

    cleanedAddress = cleanedAddress.replaceAll("[-,\\s]*$", "").trim();

    if (cleanedAddress.startsWith("부산 ")) {
      cleanedAddress = cleanedAddress.replaceFirst("부산 ", "부산광역시 ");
    } else if (!cleanedAddress.startsWith("부산광역시")) {
      cleanedAddress = "부산광역시 " + cleanedAddress;
    }

    log.info("지오코딩을 위해 최종 정제된 주소: {}", cleanedAddress);

    try {
      URI uri = UriComponentsBuilder.fromUriString(kakaoGeocodeUrl)
          .queryParam("query", cleanedAddress)
          .build()
          .encode()
          .toUri();

      log.info("최종 API 호출 URI: {}", uri);

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoApiKey);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

      log.info("카카오 API 응답 본문: {}", response.getBody());

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        int latIndex = response.getBody().indexOf("\"y\":\"");
        int lngIndex = response.getBody().indexOf("\"x\":\"");
        if (latIndex != -1 && lngIndex != -1) {
          String latStr = response.getBody().substring(latIndex + 5, response.getBody().indexOf("\"", latIndex + 5));
          String lngStr = response.getBody().substring(lngIndex + 5, response.getBody().indexOf("\"", lngIndex + 5));
          double lat = Double.parseDouble(latStr);
          double lng = Double.parseDouble(lngStr);
          log.info("지오코딩 성공: 주소='{}' -> 위도={}, 경도={}", cleanedAddress, lat, lng);
          return new GeoPoint(lat, lng);
        } else {
          log.error("지오코딩 실패: 응답에서 위도/경도 정보를 찾을 수 없습니다. 주소='{}'", cleanedAddress);
        }
      }
    } catch (Exception e) {
      log.error("지오코딩 처리 중 오류 발생: 주소='{}', 에러: {}", cleanedAddress, e.getMessage());
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