package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.KDT.mosi.domain.information.restaurant.RestaurantInfoSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class RestaurantController {

  private final RestaurantInfoSVC restaurantInfoSVC;

  /**
   * 카카오맵 API 서비스 키
   */
  @Value("${kakao.map.api-key}")
  private String kakaoMapApiKey;

  /**
   * 맛집 메인 페이지
   */
  @GetMapping("/information")
  public String restaurantMain(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "15") int size,
      @RequestParam(name = "search", required = false) String search,
      @RequestParam(name = "district", required = false) String district,
      @RequestParam(name = "cuisineType", required = false) String cuisineType,
      Model model) {

    try {
      List<RestaurantInfoDocument> allRestaurants = restaurantInfoSVC.getRestaurants();
      List<RestaurantInfoDocument> filteredRestaurants = restaurantInfoSVC.getFilteredRestaurants(search, district, cuisineType);

      // 페이징 계산
      long totalCount = filteredRestaurants.size();
      int currentPage = page;
      int totalPages = (int) Math.ceil((double) totalCount / size);

      // 페이지 네비게이션 범위
      int displayPageNum = 10;
      int endPage = (int) (Math.ceil(currentPage / (double) displayPageNum) * displayPageNum);
      int startPage = endPage - displayPageNum + 1;

      // 범위 보정
      if (startPage < 1) startPage = 1;
      if (endPage > totalPages) endPage = totalPages;

      // 현재 페이지 데이터 추출
      int startIndex = (currentPage - 1) * size;
      int endIndex = Math.min(startIndex + size, filteredRestaurants.size());
      List<RestaurantInfoDocument> pagedRestaurants = filteredRestaurants.subList(startIndex, endIndex);

      int currentPageCount = pagedRestaurants.size();

      // 필터 옵션 조회
      List<String> districts = restaurantInfoSVC.getDistrictList();
      List<String> categories = restaurantInfoSVC.getCuisineTypeList();

      model.addAttribute("restaurants", pagedRestaurants);
      model.addAttribute("allRestaurants", allRestaurants);
      model.addAttribute("filteredRestaurants", filteredRestaurants);
      model.addAttribute("districts", districts);
      model.addAttribute("categories", categories);

      model.addAttribute("selectedSearch", search);
      model.addAttribute("selectedDistrict", district);
      model.addAttribute("selectedCategory", cuisineType);

      model.addAttribute("totalCount", totalCount);
      model.addAttribute("currentPageCount", currentPageCount);
      model.addAttribute("currentPage", currentPage);
      model.addAttribute("totalPages", totalPages);
      model.addAttribute("startPage", startPage);
      model.addAttribute("endPage", endPage);

      model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);

      return "information/restaurants";

    } catch (Exception e) {
      log.error("맛집 메인 페이지 로드 실패", e);
      model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다.");
      return "error/500";
    }
  }

  /**
   * 맛집 상세 페이지
   */
  @GetMapping("/information/restaurants/{id}")
  public String restaurantDetail(@PathVariable(value = "id") String id, Model model) {
    try {
      List<RestaurantInfoDocument> allRestaurants = restaurantInfoSVC.getRestaurants();

      // 해당 ID 조회
      RestaurantInfoDocument restaurant = allRestaurants.stream()
          .filter(r -> id.equals(String.valueOf(r.getUcSeq())))
          .findFirst()
          .orElse(null);

      if (restaurant == null) {
        return "redirect:/information";
      }

      // 관련 구군 조회
      List<RestaurantInfoDocument> relatedRestaurants = allRestaurants.stream()
          .filter(r -> restaurant.getGugunNm() != null && restaurant.getGugunNm().equals(r.getGugunNm()))
          .filter(r -> !id.equals(String.valueOf(r.getUcSeq())))
          .limit(4)
          .collect(java.util.stream.Collectors.toList());

      model.addAttribute("restaurant", restaurant);
      model.addAttribute("relatedRestaurants", relatedRestaurants);
      model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);

      return "information/restaurant-detail";

    } catch (Exception e) {
      log.error("맛집 상세 페이지 로드 실패 - ID: {}", id, e);
      return "redirect:/information";
    }
  }

  /**
   * 전체 맛집 목록 API
   */
  @GetMapping("/api/restaurants")
  @ResponseBody
  public ResponseEntity<List<RestaurantInfoDocument>> getAllRestaurants() {
    try {
      List<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurants();
      return ResponseEntity.ok(restaurants);
    } catch (Exception e) {
      log.error("전체 맛집 조회 API 실패", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 지도용 맛집 목록 API
   */
  @GetMapping("/api/restaurants/map")
  @ResponseBody
  public ResponseEntity<List<RestaurantInfoDocument>> getRestaurantsForMap() {
    try {
      List<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurantsForMap();
      return ResponseEntity.ok(restaurants);
    } catch (Exception e) {
      log.error("지도용 맛집 조회 API 실패", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 검색 자동완성 API
   */
  @GetMapping("/api/search/autocomplete")
  @ResponseBody
  public ResponseEntity<List<String>> getAutocomplete(@RequestParam(value = "query") String query) {
    try {
      List<String> suggestions = restaurantInfoSVC.getSearchSuggestions(query, 5);
      return ResponseEntity.ok(suggestions);
    } catch (Exception e) {
      log.error("검색 자동완성 API 실패 - query: {}", query, e);
      return ResponseEntity.internalServerError().build();
    }
  }
}