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
import org.springframework.scheduling.annotation.Scheduled;
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

  // 매일 6시간마다 데이터를 갱신하는 스케줄러 메서드
  @Scheduled(cron = "0 0 */6 * * *") // cron = "초 분 시 일 월 요일" 현재: 매 6시간마다 실행(0시, 6시, 12시, 18시) * 인텔리제이가 실행중이어야만 적용됨
  public void scheduledDataFetch() {
    logger.info("스케줄러 시작: 부산 공공데이터 인덱스 초기화 및 갱신");

    // 1. 기존 인덱스 삭제 (새로 추가한 메서드 호출)
    dataManagementService.deleteFacilityIndex();

    // 2. 새로운 데이터 가져와서 저장
    fetchAndProcessAllFacilityData();

    logger.info("스케줄러 완료: 부산 공공데이터 인덱스 초기화 및 갱신");
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

    long currentDocumentCount = dataManagementService.countAllFacilities();

    Optional<BusanFacilityApiResponse> initialResponseOpt = callBusanFacilityApi(1);

    if (initialResponseOpt.isPresent()) {
      BusanFacilityApiResponse initialResponse = initialResponseOpt.get();
      int totalCount = initialResponse.getResponse().getBody().getTotalCount();

      if (currentDocumentCount >= totalCount) {
        logger.info("Data loading skipped. Current documents count ({}) is equal to or greater than the total count ({}) from API.", currentDocumentCount, totalCount);
        return;
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

        if (items == null || items.isEmpty()) {
          logger.info("No more items on page {}. Ending data fetch.", pageNo);
          hasMoreData = false;
          continue;
        }

        List<FacilityDocument> documents = processAndConvertFacilityItems(items);
        dataManagementService.saveAllFacilityDocuments(documents);
        processedCount += documents.size();

        logger.info("Saved {} documents from page {}. Total processed: {}", documents.size(), pageNo, processedCount);

        if (processedCount >= totalCount) {
          hasMoreData = false;
        } else {
          pageNo++;
        }
      } else {
        logger.error("Failed to fetch data for page {}. Stopping data fetch.", pageNo);
        hasMoreData = false;
      }

      int maxExpectedPages = (totalCount > 0) ? (totalCount / facilityNumOfRows) + 5 : 10;
      if (pageNo > maxExpectedPages) {
        logger.warn("Exceeded reasonable page limit. Forcing stop data fetch at page {}", pageNo);
        hasMoreData = false;
      }
    }
    logger.info("Finished fetching and processing all facility data. Total documents processed: {}", processedCount);
  }

  // 이 메서드에 구/군 추출 로직을 추가하고, setter 호출 방식으로 변경합니다.
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
          String mainMenuString = extractMainMenu(cleanedContents);
          List<String> mainMenu = splitAndCleanMainMenu(mainMenuString);
          String lastUpdated = extractLastUpdated(cleanedContents);
          String tableCount = extractTableCount(cleanedContents);
          List<String> setValueNmList = splitByDelimiter(item.getSetValueNm(), "|");
          List<String> gubunList = splitByDelimiter(item.getGubun(), " ");
          LocalDate timestamp = LocalDate.now();
          GeoPoint geoPoint = getGeocodeFromKakao(addr);
          String gugun = extractGugun(addr);

          // AllArgsConstructor가 없으므로 setter를 이용해 객체 생성
          FacilityDocument document = new FacilityDocument();
          document.setUid(uidCounter.incrementAndGet());
          document.setSubject(item.getSubject());
          document.setTel(tel != null ? tel.trim() : null);
          document.setAddr(addr != null ? addr.trim() : null);
          document.setLocation(location != null ? location.trim() : null);
          document.setMainMenu(mainMenu);
          document.setLastUpdated(lastUpdated != null ? lastUpdated.trim() : null);
          document.setImgUrl(item.getImgUrl());
          document.setRegisterDate(item.getRegisterDate());
          document.setSetValueNm(setValueNmList);
          document.setGubun(gubunList);
          document.setTableCount(tableCount);
          document.setTimestamp(timestamp);
          document.setGeoPoint(geoPoint);
          document.setGugun(gugun); // 새로 추가된 필드

          return document;
        })
        .collect(Collectors.toList());
  }

  // 구/군 정보를 추출하는 새로운 메서드
  /**
   * 주소(addr)에서 구/군 정보만 추출합니다.
   * @param addr 원본 주소 문자열
   * @return 추출된 구/군 문자열 (예: "수영구"), 없으면 null
   */
  private String extractGugun(String addr) {
    if (addr == null || addr.trim().isEmpty()) {
      return null;
    }

    // '수영구', '해운대군' 등 '구' 또는 '군'으로 끝나는 한글 단어를 찾습니다.
    Pattern pattern = Pattern.compile("[가-힣]+[구군]");
    Matcher matcher = pattern.matcher(addr);

    if (matcher.find()) {
      return matcher.group();
    }

    return null;
  }

  // ... 나머지 헬퍼 메서드들은 그대로 유지 ...
  private List<String> splitAndCleanMainMenu(String mainMenuText) {
    if (mainMenuText == null || mainMenuText.trim().isEmpty()) {
      return new ArrayList<>();
    }

    String tempText = mainMenuText;

    tempText = tempText.replaceAll("\\(.*", "");
    tempText = tempText.replaceAll("(?<=\\d),(?=\\d{3})", "");
    tempText = tempText.replaceAll("\\s*[0-9]+(?:\\s*[-/]?\\s*[0-9]+)*\\s*(?:원|만원|Won)\\s*", "");
    tempText = tempText.replaceAll("(?i)[₩￦][\\s0-9/-]*", "");
    tempText = tempText.replaceAll(" +", " ").trim();

    return Arrays.stream(tempText.split(","))
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
    if (contents == null) {
      return null;
    }

    String regex = "주메뉴\\s*:\\s*(.+?)(?=\\s*(?:[1-9]\\.\\s*)?테이블\\s*수|\\s*(?:[1-9]\\.\\s*)?입점\\s*음식점|\\s*(?:[1-9]\\.\\s*)?이용시간|\\s*<|\\s*\\d{4}년|\\s*출처|\\s*홈페이지|\\s*※|$)";

    String mainMenu = extractData(contents, regex, 1);

    if (mainMenu != null) {
      mainMenu = mainMenu.replaceAll("\\s*\\d+\\.\\s*.*", "");
      mainMenu = mainMenu.split("※")[0].trim();
      mainMenu = mainMenu.split("\\*")[0].trim();
      return mainMenu;
    }

    String fallbackRegex = "주메뉴\\s*:\\s*(.+)";
    mainMenu = extractData(contents, fallbackRegex, 1);

    if (mainMenu != null) {
      mainMenu = mainMenu.split("※")[0].trim();
      mainMenu = mainMenu.split("\\*")[0].trim();
      return mainMenu;
    }

    return null;
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