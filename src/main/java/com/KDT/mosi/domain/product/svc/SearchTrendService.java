package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.product.repository.SearchTrendDocumentRepository;
import com.KDT.mosi.domain.product.search.document.SearchTrendDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchTrendService {

  private final SearchTrendDocumentRepository searchTrendDocumentRepository;

  // 1. 키워드 저장/업데이트 (Create/Update)
  @Transactional
  public void updateSearchTrend(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return; // 유효하지 않은 키워드는 처리하지 않음
    }
    String normalizedKeyword = keyword.toLowerCase().trim(); // 검색 키워드 정규화 (소문자, 공백 제거)

    Optional<SearchTrendDocument> existingTrend = searchTrendDocumentRepository.findById(normalizedKeyword);

    if (existingTrend.isPresent()) {
      // 이미 존재하는 키워드일 경우, 검색 횟수 증가
      SearchTrendDocument trend = existingTrend.get();
      trend.setSearchCount(trend.getSearchCount() + 1);
      searchTrendDocumentRepository.save(trend); // 업데이트
      System.out.println("검색 키워드 '" + normalizedKeyword + "' 검색 횟수 증가: " + trend.getSearchCount());
    } else {
      // 새로운 키워드일 경우, 문서 생성
      SearchTrendDocument newTrend = SearchTrendDocument.builder()
          .keyword(normalizedKeyword)
          .searchCount(1)
          .build();
      searchTrendDocumentRepository.save(newTrend);
      System.out.println("새로운 검색 키워드 '" + normalizedKeyword + "' 추가");
    }
  }

  // 2. 인기 키워드 조회 (Read)
  public List<String> getPopularKeywords() {
    // 검색 횟수 (searchCount) 기준으로 내림차순 정렬, 상위 10개만 가져오기
    // PageRequest.of(페이지번호, 페이지크기, 정렬방향, 정렬기준필드)
    PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "searchCount");

    List<SearchTrendDocument> trends = searchTrendDocumentRepository.findAll(pageRequest).getContent();

    return trends.stream()
        .map(SearchTrendDocument::getKeyword)
        .collect(Collectors.toList());
  }
}