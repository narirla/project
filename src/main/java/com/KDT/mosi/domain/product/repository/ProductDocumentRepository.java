package com.KDT.mosi.domain.product.repository;

import com.KDT.mosi.domain.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {

  // 1) 키워드 검색
  // 상품명(title) 또는 닉네임(nickname)에 키워드가 포함된 문서를 찾습니다.
  List<ProductDocument> findByTitleContainingOrNicknameContaining(String title, String nickname);

  // 2) 자동완성 (접두사 검색)
  // 상품명(title) 또는 닉네임(nickname)이 키워드로 시작하는 문서를 찾습니다.
  List<ProductDocument> findByTitleStartingWithOrNicknameStartingWith(String title, String nickname);
}