package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.publicdatamanage.restaurant.document.FoodDocument;
import com.KDT.mosi.domain.publicdatamanage.restaurant.svc.FoodDataManagementSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
@Slf4j
public class FoodRestController {

  private final FoodDataManagementSVC foodDataManagementSVC;

  /**
   * 모든 맛집 문서 목록을 페이지네이션하여 JSON 형식으로 반환하는 API입니다.
   * @param pageable 페이지 정보 (page, size, sort)
   * @return FoodDocument 리스트와 페이지 정보가 담긴 Page 객체
   */
  @GetMapping
  public Page<FoodDocument> getAllRestaurants(Pageable pageable) {
    log.info("API 호출: 모든 맛집 목록을 페이지네이션하여 조회합니다. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
    // FoodDataManagementSVC의 findAll 메서드를 호출합니다.
    return foodDataManagementSVC.findAll(pageable);
  }

  /**
   * 다양한 조건으로 맛집 문서를 검색하고 페이지네이션하여 JSON 형식으로 반환하는 API입니다.
   * @param searchType 검색할 필드 타입 (예: gugunNm, title, rprsntvMenu)
   * @param keyword 검색어
   * @param pageable 페이지 정보 (page, size, sort)
   * @return 검색 결과와 페이지 정보가 담긴 Page 객체
   */
  @GetMapping("/search")
  public Page<FoodDocument> searchRestaurants(@RequestParam("searchType") String searchType, @RequestParam("keyword") String keyword, Pageable pageable) {
    log.info("API 호출: 검색 타입 '{}', 키워드 '{}'로 맛집 검색 결과를 페이지네이션하여 조회합니다. Page: {}, Size: {}", searchType, keyword, pageable.getPageNumber(), pageable.getPageSize());
    // FoodDataManagementSVC의 searchFood 메서드를 호출하여 검색 로직을 위임합니다.
    return foodDataManagementSVC.searchFood(searchType, keyword, pageable);
  }
}