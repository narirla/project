package com.KDT.mosi.domain.publicdatamanage.facility.svc;

import com.KDT.mosi.domain.publicdatamanage.facility.document.FacilityDocument;
import com.KDT.mosi.domain.publicdatamanage.facility.repository.FacilityInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityDataManagementSVCImpl implements FacilityDataManagementSVC {

  private final FacilityInfoRepository facilityInfoRepository;

  @Override
  public FacilityDocument saveFacilityDocument(FacilityDocument document) {
    log.info("Saving single document with uid: {}", document.getUid());
    return facilityInfoRepository.save(document);
  }

  @Override
  public List<FacilityDocument> saveAllFacilityDocuments(List<FacilityDocument> documents) {
    if (documents == null || documents.isEmpty()) {
      return List.of();
    }
    List<FacilityDocument> savedDocuments = (List<FacilityDocument>) facilityInfoRepository.saveAll(documents);
    log.info("Successfully saved {} documents to Elasticsearch.", savedDocuments.size());
    return savedDocuments;
  }

  @Override
  public Optional<FacilityDocument> getFacilityDocumentById(Long uid) {
    return facilityInfoRepository.findById(uid);
  }

  @Override
  public long countAllFacilities() {
    return facilityInfoRepository.count();
  }

  @Override
  public void deleteAllFacilities() {
    long count = facilityInfoRepository.count();
    if (count > 0) {
      facilityInfoRepository.deleteAll();
      log.info("Successfully deleted all {} documents from the facility index.", count);
    } else {
      log.info("No documents to delete in the facility index.");
    }
  }

  @Override
  public Optional<FacilityDocument> findLatestFacilityDocument() {
    return facilityInfoRepository.findTopByOrderByTimestampDesc();
  }

  @Override
  public Iterable<FacilityDocument> getAllFacilityDocuments() {
    return facilityInfoRepository.findAll();
  }

  // 새로 추가된 findAll 메서드 구현
  @Override
  public Page<FacilityDocument> findAll(Pageable pageable) {
    return facilityInfoRepository.findAll(pageable);
  }

  // 새로 추가된 findBySubject 메서드 구현
  @Override
  public Page<FacilityDocument> findBySubject(String keyword, Pageable pageable) {
    return facilityInfoRepository.findBySubjectContaining(keyword, pageable);
  }

  @Override
  public Page<FacilityDocument> searchFacilities(String searchType, String keyword, Pageable pageable) {
    if (!StringUtils.hasText(searchType) || !StringUtils.hasText(keyword)) {
      return facilityInfoRepository.findAll(pageable);
    }

    switch (searchType) {
      case "subject":
        return facilityInfoRepository.findBySubjectContaining(keyword, pageable);
      case "setValueNm":
        return facilityInfoRepository.findBySetValueNmContaining(keyword, pageable);
      default:
        return facilityInfoRepository.findAll(pageable);
    }
  }
}