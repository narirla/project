package com.KDT.mosi.domain.information.restaurant;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;

import java.util.List;

public interface RestaurantInfoSVC {

  /**
   * 전체 맛집 목록
   */
  List<RestaurantInfoDocument> getRestaurants();

  /**
   * 필터링된 맛집 조회
   */
  List<RestaurantInfoDocument> getFilteredRestaurants(String search, String district, String cuisineType);

  /**
   * 지도용 맛집 조회
   */
  List<RestaurantInfoDocument> getRestaurantsForMap();

  /**
   * 검색 자동완성
   */
  List<String> getSearchSuggestions(String query, int limit);

  /**
   * 구군 목록 조회
   */
  List<String> getDistrictList();

  /**
   * 업종 목록 조회
   */
  List<String> getCuisineTypeList();
}