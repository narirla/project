package com.KDT.mosi.search.controller;

import com.KDT.mosi.domain.product.svc.ProductSearchService;
import com.KDT.mosi.domain.product.svc.SearchTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AutoCompleteController {

  // ✅ ProductSearchService와 SearchTrendService를 주입받습니다.
  private final ProductSearchService productSearchService;
  private final SearchTrendService searchTrendService;

  /**
   * 상품명/설명 기반 자동완성 결과와 인기 검색어(트렌드)를 결합하여 반환하는 API
   * @param keyword 검색 키워드
   * @return 결합된 자동완성 추천 키워드 리스트
   */
  @GetMapping("/api/autocomplete")
  public ResponseEntity<List<String>> autocomplete(@RequestParam("keyword") String keyword) {
    log.info("자동완성 API 호출: {}", keyword);

    List<String> combinedSuggestions = new ArrayList<>();
    Set<String> uniqueSuggestions = new HashSet<>(); // 중복 제거를 위한 Set

    // 1. 키워드 기반 상품 자동완성 (주 우선순위)
    //    키워드 길이가 2글자 이상일 때만 상품 검색 실행
    if (keyword != null && keyword.length() >= 2) {
      List<String> productSuggestions = productSearchService.searchAutocomplete(keyword);
      for (String suggestion : productSuggestions) {
        if (uniqueSuggestions.add(suggestion)) { // Set에 추가하며 중복 체크
          combinedSuggestions.add(suggestion);
        }
      }
    }

    // 2. 인기 검색어(트렌드) 기반 자동완성 (보조)
    //    상품 검색 결과가 부족하거나 키워드 길이가 2글자 미만일 때 트렌드를 추가
    if (combinedSuggestions.size() < 5) {
      List<String> trendSuggestions = searchTrendService.getTopSearchKeywords(keyword);
      for (String suggestion : trendSuggestions) {
        if (uniqueSuggestions.add(suggestion)) {
          combinedSuggestions.add(suggestion);
        }
      }
    }

    // 최종적으로 반환할 결과의 최대 개수 제한 (예: 10개)
    if (combinedSuggestions.size() > 10) {
      combinedSuggestions = combinedSuggestions.subList(0, 10);
    }

    return ResponseEntity.ok(combinedSuggestions);
  }
}