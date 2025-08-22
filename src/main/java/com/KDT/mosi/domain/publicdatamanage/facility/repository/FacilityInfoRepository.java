package com.KDT.mosi.domain.publicdatamanage.facility.repository;

import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacilityInfoRepository extends ElasticsearchRepository<FacilityDocument, Long> {

  Optional<FacilityDocument> findTopByOrderByUidDesc();

  Optional<FacilityDocument> findTopByOrderByTimestampDesc();

  // 제목으로 검색하는 메서드 추가
  Page<FacilityDocument> findBySubjectContaining(String subject, Pageable pageable);

  // 카테고리로 검색하는 메서드 추가
  Page<FacilityDocument> findBySetValueNmContaining(String setValueNm, Pageable pageable);
}