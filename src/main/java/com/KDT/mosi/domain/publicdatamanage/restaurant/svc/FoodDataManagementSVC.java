package com.KDT.mosi.domain.publicdatamanage.restaurant.svc;

import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FoodDataManagementSVC {

  FoodDocument saveFoodDocument(FoodDocument document);
  List<FoodDocument> saveAllFoodDocuments(List<FoodDocument> documents);
  Optional<FoodDocument> findFoodDocumentById(Integer ucSeq);
  long countAllFoodDocuments();
  void deleteAllFoodDocuments();
  Optional<FoodDocument> findLatestFoodDocument();
  Iterable<FoodDocument> getAllFoodDocuments();

  // 모든 문서를 페이지네이션하여 조회하는 메서드
  Page<FoodDocument> findAll(Pageable pageable);

  // 여러 검색 조건을 처리하는 통합 검색 메서드
  Page<FoodDocument> searchFood(String searchType, String keyword, Pageable pageable);

  /**
   * Elasticsearch의 food_info 인덱스를 삭제합니다.
   */
  void deleteFoodInfoIndex();
}