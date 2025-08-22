package com.KDT.mosi.domain.product.repository;

import com.KDT.mosi.domain.product.search.document.SearchTrendDocument; // SearchTrendDocument 임포트
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchTrendDocumentRepository extends ElasticsearchRepository<SearchTrendDocument, String> {

  Optional<SearchTrendDocument> findByKeyword(String keyword);

  /**
   * 특정 키워드로 시작하는 인기 검색어를 검색합니다.
   * 검색 횟수(searchCount) 기준으로 내림차순 정렬하여 상위 N개를 가져옵니다.
   * @param keyword 검색 키워드 (소문자 처리된 값)
   * @return 인기 검색어 목록
   */
  List<SearchTrendDocument> findTop5ByKeywordStartingWithOrderBySearchCountDesc(String keyword);

  /**
   * 전체 인기 검색어 중 상위 N개를 검색합니다.
   * 검색 횟수(searchCount) 기준으로 내림차순 정렬합니다.
   * @return 전체 인기 검색어 목록
   */
  List<SearchTrendDocument> findTop5ByOrderBySearchCountDesc();
}