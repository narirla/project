package com.KDT.mosi.domain.publicdatamanage.facility.svc;

import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import com.KDT.mosi.domain.publicdatamanage.facility.dto.BusanFacilityApiResponse;
import com.KDT.mosi.domain.publicdatamanage.facility.dto.FacilityItem;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FacilityDataProcessorService {

  private static final Logger logger = LoggerFactory.getLogger(FacilityDataProcessorService.class);

  private final RestTemplate restTemplate;
  // 인터페이스로 의존성을 주입받습니다.
  private final FacilityDataManagementSVC dataManagementService;

  @Value("${busan.api.facility.url}")
  private String facilityApiUrl;
  @Value("${busan.api.common.serviceKey}")
  private String facilityServiceKey;
  @Value("${busan.api.common.numOfRows}")
  private int facilityNumOfRows;
  @Value("${busan.api.common.resultType}")
  private String facilityResultType;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;
  @Value("${kakao.api.url.geocode}")
  private String kakaoGeocodeUrl;

  private final AtomicLong uidCounter = new AtomicLong(0L);

  public FacilityDataProcessorService(RestTemplate restTemplate, FacilityDataManagementSVC dataManagementService) {
    this.restTemplate = restTemplate;
    this.dataManagementService = dataManagementService;
  }

  public void initializeUidCounter() {
    Optional<FacilityDocument> latestDoc = dataManagementService.findLatestFacilityDocument();
    Long latestUid = latestDoc.map(FacilityDocument::getUid).orElse(0L);

    while (true) {
      long current = uidCounter.get();
      if (current >= latestUid) {
        break;
      }
      if (uidCounter.compareAndSet(current, latestUid)) {
        logger.info("UID counter successfully initialized to {}.", latestUid);
        break;
      }
    }
  }

  public Optional<BusanFacilityApiResponse> callBusanFacilityApi(int pageNo) {
    URI uri = UriComponentsBuilder.fromUriString(facilityApiUrl)
        .queryParam("serviceKey", facilityServiceKey)
        .queryParam("pageNo", pageNo)
        .queryParam("numOfRows", facilityNumOfRows)
        .queryParam("resultType", facilityResultType)
        .build()
        .toUri();

    logger.info("Calling Busan Facility API: {}", uri);

    try {
      BusanFacilityApiResponse response = restTemplate.getForObject(uri, BusanFacilityApiResponse.class);
      if (response != null && "00".equals(response.getResponse().getHeader().getResultCode())) {
        return Optional.of(response);
      } else {
        logger.error("API call failed. Code: {}, Message: {}",
            response.getResponse().getHeader().getResultCode(),
            response.getResponse().getHeader().getResultMsg());
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Error calling API at {}: {}", uri, e.getMessage());
      return Optional.empty();
    }
  }

  public void fetchAndProcessAllFacilityData() {
    logger.info("Starting to fetch and process all facility data...");

    // 1. Elasticsearch에 저장된 현재 도큐먼트 개수를 가져옵니다.
    long currentDocumentCount = dataManagementService.countAllFacilities();

    // 2. 초기 페이지를 호출하여 전체 데이터 개수를 확인합니다.
    Optional<BusanFacilityApiResponse> initialResponseOpt = callBusanFacilityApi(1);

    if (initialResponseOpt.isPresent()) {
      BusanFacilityApiResponse initialResponse = initialResponseOpt.get();
      int totalCount = initialResponse.getResponse().getBody().getTotalCount();

      // 3. 제약조건 확인: 기존 도큐먼트 개수와 API의 총 데이터 개수가 동일한 경우
      if (currentDocumentCount >= totalCount) {
        logger.info("Data loading skipped. Current documents count ({}) is equal to or greater than the total count ({}) from API.", currentDocumentCount, totalCount);
        return; // 메서드 종료
      }
    } else {
      logger.error("Failed to fetch initial page to determine total count. Stopping data fetch.");
      return;
    }

    initializeUidCounter();

    int pageNo = 1;
    int totalCount = initialResponseOpt.get().getResponse().getBody().getTotalCount();
    int processedCount = 0;
    boolean hasMoreData = true;

    while (hasMoreData) {
      Optional<BusanFacilityApiResponse> responseOpt = callBusanFacilityApi(pageNo);

      if (responseOpt.isPresent()) {
        BusanFacilityApiResponse response = responseOpt.get();
        List<FacilityItem> items = response.getResponse().getBody().getItems().getItemList();

        // 데이터가 없는 페이지일 경우 반복문 종료
        if (items == null || items.isEmpty()) {
          logger.info("No more items on page {}. Ending data fetch.", pageNo);
          hasMoreData = false;
          continue;
        }

        List<FacilityDocument> documents = processAndConvertFacilityItems(items);
        dataManagementService.saveAllFacilityDocuments(documents);
        processedCount += documents.size();

        // 100개 단위로 진행 상황을 출력합니다.
        // (facilityNumOfRows가 100이므로 이 로그가 매 페이지마다 출력됩니다)
        logger.info("Saved {} documents from page {}. Total processed: {}", documents.size(), pageNo, processedCount);

        // 총 처리된 문서 수가 전체 데이터 수에 도달하면 반복문을 종료합니다.
        if (processedCount >= totalCount) {
          hasMoreData = false;
        } else {
          pageNo++;
        }
      } else {
        logger.error("Failed to fetch data for page {}. Stopping data fetch.", pageNo);
        hasMoreData = false;
      }

      // 혹시 모를 무한 루프를 방지하기 위한 제약 조건 (기존 코드와 동일)
      int maxExpectedPages = (totalCount > 0) ? (totalCount / facilityNumOfRows) + 5 : 10;
      if (pageNo > maxExpectedPages) {
        logger.warn("Exceeded reasonable page limit. Forcing stop data fetch at page {}", pageNo);
        hasMoreData = false;
      }
    }
    logger.info("Finished fetching and processing all facility data. Total documents processed: {}", processedCount);
  }

  public List<FacilityDocument> processAndConvertFacilityItems(List<FacilityItem> facilityItems) {
    if (facilityItems == null || facilityItems.isEmpty()) {
      return new ArrayList<>();
    }
    return facilityItems.stream()
        .map(item -> {
          String cleanedContents = removeHtmlTags(item.getContents());
          String tel = extractTel(cleanedContents);
          String addr = extractAddr(cleanedContents);
          String location = extractLocation(cleanedContents);
          String mainMenuString = extractMainMenu(cleanedContents); // 원본 mainMenu 문자열 추출
          List<String> mainMenu = splitAndCleanMainMenu(mainMenuString); // 새로운 헬퍼 메서드 호출
          String lastUpdated = extractLastUpdated(cleanedContents);
          String tableCount = extractTableCount(cleanedContents);
          List<String> setValueNmList = splitByDelimiter(item.getSetValueNm(), "|");
          List<String> gubunList = splitByDelimiter(item.getGubun(), " ");
          LocalDate timestamp = LocalDate.now();
          GeoPoint geoPoint = getGeocodeFromKakao(addr);

          return FacilityDocument.builder()
              .uid(uidCounter.incrementAndGet())
              .subject(item.getSubject())
              .tel(tel != null ? tel.trim() : null)
              .addr(addr != null ? addr.trim() : null)
              .location(location != null ? location.trim() : null)
              .mainMenu(mainMenu) // 가공된 리스트 데이터 사용
              .lastUpdated(lastUpdated != null ? lastUpdated.trim() : null)
              .imgUrl(item.getImgUrl())
              .registerDate(item.getRegisterDate())
              .setValueNm(setValueNmList)
              .gubun(gubunList)
              .tableCount(tableCount)
              .timestamp(timestamp)
              .geoPoint(geoPoint)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 메뉴 문자열을 ','를 기준으로 분리하고 각 항목을 정리하는 헬퍼 메서드
   * @param mainMenuText 원본 메뉴 문자열
   * @return 정리된 메뉴 항목 리스트
   */
  private List<String> splitAndCleanMainMenu(String mainMenuText) {
    if (mainMenuText == null || mainMenuText.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return Arrays.stream(mainMenuText.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private String removeHtmlTags(String htmlString) {
    if (htmlString == null || htmlString.trim().isEmpty()) {
      return "";
    }
    String noHtmlString = Jsoup.parse(htmlString).text();
    noHtmlString = noHtmlString.replaceAll("!R!!N!", "").replaceAll("\\s+", " ").trim();
    return noHtmlString;
  }

  private List<String> splitByDelimiter(String text, String delimiter) {
    if (text == null || text.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return Arrays.stream(text.split(Pattern.quote(delimiter)))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private String extractData(String text, String regex, int group) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(group);
    }
    return null;
  }

  private String extractTel(String contents) {
    String tel = extractData(contents, "전화번호\\s*:\\s*.*?([0-9]{2,3}[- ]?[0-9]{3,4}[- ]?[0-9]{4})", 1);
    if (tel == null) {
      tel = extractData(contents, "\\(([0-9]{2,3}[- ]?[0-9]{3,4}[- ]?[0-9]{4})\\)", 1);
    }
    if (tel == null) {
      tel = extractData(contents, "T:\\s*([0-9]{2,3}[- ]?[0-9]{3,4}[- ]?[0-9]{4})", 1);
    }
    return tel;
  }

  private String extractAddr(String contents) {
    String regex = "주소\\s*:\\s*(.+?)(?=\\s*(?:[1-5]\\.\\s*)?위\\s*치\\s*[:;]|\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*(?:[1-5]\\.\\s*)?주메뉴\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?테이블\\s*수\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?입점\\s*음식점\\s*[:;])";
    String addr = extractData(contents, regex, 1);
    if (addr == null) {
      regex = "주\\s*소\\s*:\\s*(.+?)(?=\\s*(?:[1-5]\\.\\s*)?위\\s*치\\s*[:;]|\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*(?:[1-5]\\.\\s*)?주메뉴\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?테이블\\s*수\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?입점\\s*음식점\\s*[:;])";
      addr = extractData(contents, regex, 1);
    }
    if (addr != null && addr.endsWith(".")) {
      addr = addr.substring(0, addr.length() - 1);
    }
    return addr;
  }

  private String extractLocation(String contents) {
    String regex = "(?:[1-5]\\.\\s*)?위\\s*치\\s*[:;]?\\s*(.+?)(?=\\s*(?:[1-5]\\.\\s*)?주소\\s*[:;]|\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*(?:[1-5]\\.\\s*)?주메뉴\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?테이블\\s*수\\s*(?:\\(.+?\\))?\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?입점\\s*음식점\\s*[:;])";
    String location = extractData(contents, regex, 1);
    if (location == null) {
      regex = "(?:[1-5]\\.\\s*)?위치\\s*[:;]?\\s*(.+?)(?=\\s*(?:[1-5]\\.\\s*)?주소\\s*[:;]|\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*(?:[1-5]\\.\\s*)?주메뉴\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?테이블\\s*수\\s*(?:\\(.+?\\))?\\s*[:;]|\\s*(?:[1-5]\\.\\s*)?입점\\s*음식점\\s*[:;])";
      location = extractData(contents, regex, 1);
    }

    if (location != null) {
      location = location.replaceAll("(?<=\\d{1,5})\\s*[4]\\.\\s*테이블", "");
      location = location.replaceAll("(?<=\\d{1,5}m)\\s*[4]\\.\\s*테이블", "");
      location = location.replaceAll("\\.$", "");
      return location.trim();
    }
    return null;
  }

  private String extractMainMenu(String contents) {
    String mainMenu = extractData(contents, "주메뉴\\s*:\\s*(.+?)(?=\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*※)", 1);
    if (mainMenu != null) {
      mainMenu = mainMenu.split("※")[0].trim();
    }
    if (mainMenu == null) {
      mainMenu = extractData(contents, "주메뉴\\s*:\\s*(.+)", 1);
    }
    return mainMenu;
  }

  private String extractLastUpdated(String contents) {
    String lastUpdated = extractData(contents, "<(\\d{4}년 \\d{1,2}월 \\d{1,2}일 기준)>", 1);
    if (lastUpdated == null) {
      lastUpdated = extractData(contents, "(\\d{4}년 \\d{1,2}월 \\d{1,2}일 기준)", 1);
    }
    return lastUpdated;
  }

  private String extractTableCount(String contents) {
    if (contents == null || contents.trim().isEmpty()) {
      return null;
    }

    String regex = "테이블\\s*수\\s*:\\s*(.+?)(?=\\s*<|\\s*(?:[1-9]\\.\\s*)?주메뉴\\s*[:;]|\\s*(?:[1-9]\\.\\s*)?입점\\s*음식점\\s*[:;])";
    String tableCountStr = extractData(contents, regex, 1);

    if (tableCountStr != null) {
      tableCountStr = tableCountStr.trim();
      if (tableCountStr.endsWith(".")) {
        tableCountStr = tableCountStr.substring(0, tableCountStr.length() - 1);
      }
      return tableCountStr.trim();
    }
    return null;
  }

  private GeoPoint getGeocodeFromKakao(String address) {
    if (address == null || address.trim().isEmpty()) {
      return null;
    }

    String cleanedAddress = address.replaceAll("[,()]", "").trim();

    URI uri = UriComponentsBuilder.fromHttpUrl(kakaoGeocodeUrl)
        .queryParam("query", cleanedAddress)
        .encode(StandardCharsets.UTF_8)
        .build()
        .toUri();

    try {
      HttpHeaders headers = new HttpHeaders();
      String authHeaderValue = "KakaoAK " + kakaoApiKey;
      headers.set("Authorization", authHeaderValue);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, entity, JsonNode.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        JsonNode documents = response.getBody().get("documents");
        if (documents != null && documents.isArray() && documents.size() > 0) {
          JsonNode firstDocument = documents.get(0);
          double longitude = firstDocument.get("x").asDouble();
          double latitude = firstDocument.get("y").asDouble();
          return new GeoPoint(latitude, longitude);
        }
      }
    } catch (HttpClientErrorException e) {
      logger.error("카카오맵 지오코딩 실패. HTTP 상태 코드: {}, 응답 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
      logger.error("Error for address: {}", cleanedAddress);
    } catch (Exception e) {
      logger.error("카카오맵 지오코딩 처리 중 예기치 않은 오류 발생 for address '{}': {}", cleanedAddress, e.getMessage());
    }
    return null;
  }
}