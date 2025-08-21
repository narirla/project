package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import com.KDT.mosi.domain.publicdatamanage.facility.svc.FacilityDataManagementSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
@Slf4j
public class FacilityRestController {

  private final FacilityDataManagementSVC facilityDataManagementSVC;

  @GetMapping
  public Page<FacilityDocument> getAllFacilities(Pageable pageable) {
    log.info("API 호출: 모든 시설 목록을 페이지네이션하여 조회합니다. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
    return facilityDataManagementSVC.findAll(pageable);
  }

  @GetMapping("/search")
  public Page<FacilityDocument> searchFacilities(
      @RequestParam("searchType") String searchType,
      @RequestParam("keyword") String keyword,
      Pageable pageable) {
    log.info("API 호출: 검색 타입 '{}', 키워드 '{}'로 시설 검색 결과를 페이지네이션하여 조회합니다. Page: {}, Size: {}", searchType, keyword, pageable.getPageNumber(), pageable.getPageSize());

    // searchFacilities 메서드를 호출하여 검색과 페이지네이션을 함께 처리합니다.
    return facilityDataManagementSVC.searchFacilities(searchType, keyword, pageable);
  }
}