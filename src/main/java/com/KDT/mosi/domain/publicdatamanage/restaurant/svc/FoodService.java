package com.KDT.mosi.domain.publicdatamanage.restaurant.svc;

import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FoodService {

  private final FoodDataManagementSVC foodDataManagementSVC;

  public List<FoodDocument> getAllFoodDocuments() {
    return StreamSupport.stream(foodDataManagementSVC.getAllFoodDocuments().spliterator(), false)
        .collect(Collectors.toList());
  }

  public Page<FoodDocument> getAllFoodData(Pageable pageable) {
    return foodDataManagementSVC.findAll(pageable);
  }

  public Optional<FoodDocument> getFoodDocumentById(Integer ucSeq) {
    return foodDataManagementSVC.findFoodDocumentById(ucSeq);
  }

  public Page<FoodDocument> searchFood(String searchType, String keyword, Pageable pageable) {
    return foodDataManagementSVC.searchFood(searchType, keyword, pageable);
  }
}