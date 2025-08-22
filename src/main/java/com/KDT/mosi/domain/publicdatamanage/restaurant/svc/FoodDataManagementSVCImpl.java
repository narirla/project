package com.KDT.mosi.domain.publicdatamanage.restaurant.svc;


import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDataManagementSVCImpl implements FoodDataManagementSVC {

  private final FoodDocumentRepository foodDocumentRepository;
  private final ElasticsearchOperations elasticsearchOperations;
  private final FoodDocumentRepository repository;

  public void ensureIndexExists() {
    boolean isIndexExist = elasticsearchOperations.indexOps(FoodDocument.class).exists();
    if (!isIndexExist) {
      elasticsearchOperations.indexOps(FoodDocument.class).create();
      elasticsearchOperations.indexOps(FoodDocument.class).putMapping();
      log.info("인덱스 'food_data'가 존재하지 않아 새로 생성했습니다.");
    }
  }

  @Override
  public FoodDocument saveFoodDocument(FoodDocument document) {
    return foodDocumentRepository.save(document);
  }

  @Override
  public List<FoodDocument> saveAllFoodDocuments(List<FoodDocument> documents) {
    return (List<FoodDocument>) foodDocumentRepository.saveAll(documents);
  }

  @Override
  public Optional<FoodDocument> findFoodDocumentById(Integer ucSeq) {
    return foodDocumentRepository.findById(ucSeq);
  }

  @Override
  public long countAllFoodDocuments() {
    return foodDocumentRepository.count();
  }

  @Override
  public void deleteAllFoodDocuments() {
    foodDocumentRepository.deleteAll();
  }

  @Override
  public Optional<FoodDocument> findLatestFoodDocument() {
    try{
      return foodDocumentRepository.findTopByOrderByTimestampDesc();
    } catch (NoSuchIndexException e) {
      log.warn("인덱스 'food_data'가 존재하지 않아 최신 문서를 찾을 수 없습니다. 새로운 인덱스 생성 및 데이터 처리를 시작합니다.");
      return Optional.empty();
    }

  }

  @Override
  public Iterable<FoodDocument> getAllFoodDocuments() {
    return foodDocumentRepository.findAll();
  }

  @Override
  public Page<FoodDocument> findAll(Pageable pageable) {
    return foodDocumentRepository.findAll(pageable);
  }

  @Override
  public Page<FoodDocument> searchFood(String searchType, String keyword, Pageable pageable) {
    log.info("Searching food with searchType: {}, keyword: {}", searchType, keyword);
    if (keyword == null || keyword.trim().isEmpty()) {
      return foodDocumentRepository.findAll(pageable);
    }

    switch (searchType.toLowerCase()) {
      case "gugunnm":
        return foodDocumentRepository.findByGugunNm(keyword, pageable);
      case "title":
        return foodDocumentRepository.findByTitleContaining(keyword, pageable);
      case "rprsntvmenu":
        return foodDocumentRepository.findByRprsntvMenu(keyword, pageable);
      default:
        log.warn("Invalid searchType: {}. Defaulting to title search.", searchType);
        return foodDocumentRepository.findByTitleContaining(keyword, pageable);
    }
  }

  @Override
  public void deleteFoodInfoIndex() {
    // ElasticsearchOperations를 이용해 인덱스를 삭제합니다.
    if (elasticsearchOperations.indexOps(FoodDocument.class).exists()) {
      boolean isDeleted = elasticsearchOperations.indexOps(FoodDocument.class).delete();
      if (isDeleted) {
        log.info("기존 Elasticsearch 인덱스 'food_info' 삭제 성공.");
      } else {
        log.error("기존 Elasticsearch 인덱스 'food_info' 삭제 실패.");
      }
    } else {
      log.info("삭제할 Elasticsearch 인덱스 'food_info'가 존재하지 않습니다.");
    }
  }
}