package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.product.svc.SearchTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AutoCompleteController {

  private final SearchTrendService searchTrendService;

  /**
   * 인기 검색어(트렌드)만을 기반으로 자동완성 추천 키워드 리스트를 반환하는 API
   * @param keyword 검색 키워드
   * @return 인기 검색어(트렌드) 리스트
   */
  @GetMapping("/api/autocomplete")
  public ResponseEntity<List<String>> autocomplete(@RequestParam("keyword") String keyword) {
    log.info("자동완성 API 호출: {}", keyword);

    // ✅ 자동완성 키워드의 최대 허용 길이 설정
    final int MAX_KEYWORD_LENGTH = 20;

    if (keyword == null || keyword.trim().isEmpty()) {
      // 키워드가 비어있을 경우 전체 인기 검색어 중 상위 5개 반환
      List<String> top5Trends = searchTrendService.getTop5Trends().stream()
          .filter(s -> s.length() <= MAX_KEYWORD_LENGTH) // ✅ 길이 필터링 강화 (20자)
          .collect(Collectors.toList());
      return ResponseEntity.ok(top5Trends);
    }

    // 키워드가 있을 경우, 해당 키워드로 시작하는 인기 검색어 중 상위 5개 반환
    List<String> trendSuggestions = searchTrendService.getTopSearchKeywords(keyword).stream()
        .filter(s -> s.length() <= MAX_KEYWORD_LENGTH) // ✅ 길이 필터링 강화 (20자)
        .collect(Collectors.toList());

    return ResponseEntity.ok(trendSuggestions);
  }
}