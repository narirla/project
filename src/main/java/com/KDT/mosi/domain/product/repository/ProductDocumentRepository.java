package com.KDT.mosi.domain.product.repository;

import com.KDT.mosi.domain.product.document.ProductDocument;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository; // @Repository 어노테이션 사용 시 필요

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

  // 문서 ID로 상품 도큐먼트 조회
  Optional<ProductDocument> findByProductId(String productId);

  /**
   * 제목(title) 또는 설명(description) 필드에서 특정 키워드를 포함하는 문서를 검색합니다.
   * findByTitleContainingOrDescriptionContaining 메서드는 두 개의 String 파라미터를 필요로 합니다.
   * 첫 번째 String은 title, 두 번째 String은 description 검색을 위한 것입니다.
   */
  Page<ProductDocument> findByTitleContainingOrDescriptionContaining(String titleKeyword, String descriptionKeyword, Pageable pageable);

  // ✨✨✨ 새로 추가할 자동완성 검색 메서드 ✨✨✨
  /**
   * 상품명(title) 필드에서 키워드와 일치하는 문서를 찾습니다.
   * Spring Data의 `Containing` 키워드를 사용하여 Elasticsearch의 'wildcard' 쿼리와 유사하게 작동합니다.
   * Pageable 객체를 사용하여 검색 결과를 페이지네이션(개수 제한)합니다.
   */
  Page<ProductDocument> findByTitleContaining(String title, Pageable pageable);
=======
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
>>>>>>> e506fd3749059f9445a987ad395676865572bc94
}