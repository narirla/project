package com.KDT.mosi.domain.product.svc;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.KDT.mosi.domain.product.document.ProductDocument;
import com.KDT.mosi.domain.product.search.document.SearchTrendDocument;
import com.KDT.mosi.domain.product.repository.SearchTrendDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchTrendService {

  private final SearchTrendDocumentRepository searchTrendDocumentRepository;
  private final ElasticsearchOperations elasticsearchOperations;
  // ✅ ProductSearchService에 대한 의존성 제거

  @Transactional
  public void saveSearchKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return;
    }

    String normalizedKeyword = keyword.toLowerCase().trim();
    LocalDate today = LocalDate.now();

    NativeQuery nativeQuery = NativeQuery.builder()
        .withQuery(q -> q.bool(b -> b
            .must(m -> m.term(t -> t.field("keyword.keyword").value(normalizedKeyword)))
            .must(m -> m.term(t -> t.field("searchDate").value(today.toString())))
        ))
        .build();

    SearchHits<SearchTrendDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchTrendDocument.class);

    if (!searchHits.isEmpty()) {
      SearchTrendDocument existingTrend = searchHits.getSearchHit(0).getContent();
      existingTrend.incrementSearchCount();
      searchTrendDocumentRepository.save(existingTrend);
      log.info("인기 검색어/트렌드 검색 키워드: " + existingTrend.getKeyword() + ", 횟수 증가: " + existingTrend.getSearchCount());
    } else {
      SearchTrendDocument newTrend = SearchTrendDocument.builder()
          .keyword(normalizedKeyword)
          .searchDate(today)
          .searchCount(1L)
          .build();
      searchTrendDocumentRepository.save(newTrend);
      log.info("인기 검색어/트렌드 검색 키워드: " + newTrend.getKeyword() + ", 신규 추가");
    }
  }

  public List<String> getTopSearchKeywords(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return searchTrendDocumentRepository.findTop5ByOrderBySearchCountDesc()
          .stream()
          .map(SearchTrendDocument::getKeyword)
          .collect(Collectors.toList());
    }

    return searchTrendDocumentRepository.findTop5ByKeywordStartingWithOrderBySearchCountDesc(keyword)
        .stream()
        .map(SearchTrendDocument::getKeyword)
        .collect(Collectors.toList());
  }

  /**
   * ✅ 키워드가 없는 경우, 전체 인기 검색어 중 상위 5개를 반환하는 메서드를 추가합니다.
   * @return 전체 인기 검색어 목록
   */
  public List<String> getTop5Trends() {
    return searchTrendDocumentRepository.findTop5ByOrderBySearchCountDesc()
        .stream()
        .map(SearchTrendDocument::getKeyword)
        .collect(Collectors.toList());
  }

}