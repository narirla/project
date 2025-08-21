package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.product.document.ProductDocument; // 이 임포트는 더 이상 사용되지 않지만, 기존 코드에 있었으니 유지
import com.KDT.mosi.domain.product.repository.ProductDocumentRepository; // 이 임포트도 기존 코드에 있었으니 유지
import com.KDT.mosi.domain.product.search.document.SearchTrendDocument;
import com.KDT.mosi.domain.product.repository.SearchTrendDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // 이 클래스는 더 이상 사용되지 않지만, 임포트 유지
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchTrendService {

  private final SearchTrendDocumentRepository searchTrendDocumentRepository;
  private final ProductDocumentRepository productDocumentRepository; // 이 리포지토리는 이제 이 서비스에서 직접 사용되지 않음
  private final ElasticsearchOperations elasticsearchOperations; // 이 객체도 이제 이 서비스에서 직접 사용되지 않음

  /**
   * 사용자가 입력한 검색어를 저장하거나 횟수를 업데이트합니다.
   *
   * @param keyword 검색어
   */
  public void saveSearchKeyword(String keyword) {
    String normalizedKeyword = keyword.trim().toLowerCase();
    searchTrendDocumentRepository.findByKeyword(normalizedKeyword)
        .ifPresentOrElse(
            // 이미 존재하면 count를 증가
            existingKeyword -> {
              existingKeyword.setSearchCount(existingKeyword.getSearchCount() + 1);
              searchTrendDocumentRepository.save(existingKeyword);
            },
            // 존재하지 않으면 새로 생성
            () -> {
              SearchTrendDocument newKeyword = SearchTrendDocument.builder()
                  .keyword(normalizedKeyword)
                  .searchCount(1L)
                  .build();
              searchTrendDocumentRepository.save(newKeyword);
            }
        );
  }

  /**
   * 특정 키워드(또는 키워드 없음)로 시작하는 인기 검색어를 검색합니다.
   * 이 메서드는 이제 ProductDocument가 아닌 SearchTrendDocument를 기반으로 작동합니다.
   *
   * @param keyword 사용자 입력 키워드 (선택 사항)
   * @return 인기 검색어 목록
   */
  public List<String> getTopSearchKeywords(String keyword) {
    log.info("인기 검색어/트렌드 검색 키워드: {}", keyword);

    List<SearchTrendDocument> trendResults;
    if (keyword != null && !keyword.trim().isEmpty()) {
      // 키워드가 있으면 해당 키워드로 시작하는 트렌드 검색
      trendResults = searchTrendDocumentRepository.findTop5ByKeywordStartingWithOrderBySearchCountDesc(keyword.toLowerCase());
    } else {
      // 키워드가 없으면 전체 인기 검색어 중 상위 5개
      trendResults = searchTrendDocumentRepository.findTop5ByOrderBySearchCountDesc();
    }

    log.info("Elasticsearch에서 찾은 인기 검색어/트렌드 결과 수: {}", trendResults.size());

    // 검색 결과에서 키워드(keyword)만 추출하여 List<String> 형태로 반환
    return trendResults.stream()
        .map(SearchTrendDocument::getKeyword)
        .collect(Collectors.toList());
  }
}