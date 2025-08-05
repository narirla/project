package com.KDT.mosi.domain.publicdatamanage.restaurant.svc;

import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDataManagementSVCImpl implements FoodDataManagementSVC {

  private final FoodDocumentRepository foodDocumentRepository;

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
    return foodDocumentRepository.findTopByOrderByTimestampDesc();
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
}