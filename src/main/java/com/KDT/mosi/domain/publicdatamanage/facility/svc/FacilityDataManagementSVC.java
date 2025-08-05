package com.KDT.mosi.domain.publicdatamanage.facility.svc;

import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 시설 데이터 관리와 관련된 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 이 인터페이스를 구현하는 클래스에서 실제 로직을 구현하게 됩니다.
 */
public interface FacilityDataManagementSVC {

  FacilityDocument saveFacilityDocument(FacilityDocument document);

  List<FacilityDocument> saveAllFacilityDocuments(List<FacilityDocument> documents);

  Optional<FacilityDocument> getFacilityDocumentById(Long uid);

  long countAllFacilities();

  void deleteAllFacilities();

  Optional<FacilityDocument> findLatestFacilityDocument();

  Iterable<FacilityDocument> getAllFacilityDocuments();

  // 모든 시설 문서를 페이지네이션하여 조회하는 메서드 추가
  Page<FacilityDocument> findAll(Pageable pageable);

  // subject 필드를 기준으로 검색하여 페이지네이션하는 메서드 추가
  Page<FacilityDocument> findBySubject(String keyword, Pageable pageable);

  /**
   * 검색어와 필터 조건을 포함하여 시설 데이터를 페이지네이션하여 조회합니다.
   * @param searchType 검색 필드 (subject, setValueNm 등)
   * @param keyword 검색어
   * @param pageable 페이징 정보
   * @return 조건에 맞는 FacilityDocument의 페이징된 결과
   */
  Page<FacilityDocument> searchFacilities(String searchType, String keyword, Pageable pageable);
}