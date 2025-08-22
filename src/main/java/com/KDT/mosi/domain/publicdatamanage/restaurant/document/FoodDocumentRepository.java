package com.KDT.mosi.domain.publicdatamanage.restaurant.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface FoodDocumentRepository extends ElasticsearchRepository<FoodDocument, Integer> {

  Optional<FoodDocument> findTopByOrderByTimestampDesc();
  Page<FoodDocument> findByGugunNm(String gugun, Pageable pageable);
  Page<FoodDocument> findByTitleContaining(String keyword, Pageable pageable);
  Page<FoodDocument> findByRprsntvMenu(String keyword, Pageable pageable);
}