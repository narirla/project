package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.product.svc.SearchTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete") // API 엔드포인트는 보통 /api로 시작합니다.
@RequiredArgsConstructor
public class AutoCompleteController {

  private final SearchTrendService searchTrendService;

  // 인기 검색어와 유사한 키워드를 반환하는 API
  @GetMapping("/keywords")
  public List<String> getPopularKeywordSuggestions(@RequestParam String keyword) {
    // SearchTrendService를 사용하여 검색어와 유사한 인기 키워드를 조회하는 로직을 구현합니다.
    // 현재는 인기 키워드 전체를 반환하지만, 실제로는
    // `searchTrendRepository`를 이용해 `keyword`를 포함하는 키워드를 조회해야 합니다.
    // 이 부분은 나중에 Elasticsearch의 'completion suggester' 기능을 추가하면 더욱 정확해집니다.

    // 현재는 기능 구현을 위해 임시로 인기 키워드 목록을 그대로 반환하는 예시로 대체합니다.
    // 실제로는 검색어 필터링 로직이 추가되어야 합니다.
    List<String> popularKeywords = searchTrendService.getPopularKeywords();
    return popularKeywords;
  }
}