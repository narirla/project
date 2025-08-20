package com.KDT.mosi.domain.product.repository;// com.KDT.mosi.domain.search.repository.SearchTrendDocumentRepository.java

import com.KDT.mosi.domain.product.search.document.SearchTrendDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchTrendDocumentRepository extends ElasticsearchRepository<SearchTrendDocument, String> {
  // 키워드를 ID로 사용하기 때문에 findById()로 존재 여부 확인 및 조회 가능
  Optional<SearchTrendDocument> findByKeyword(String keyword);
}