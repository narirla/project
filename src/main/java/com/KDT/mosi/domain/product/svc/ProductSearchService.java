package com.KDT.mosi.domain.product.svc;

import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.domain.product.document.ProductDocument;
import com.KDT.mosi.domain.product.dto.response.ProductSearchResponse;
import com.KDT.mosi.domain.product.repository.ProductDocumentRepository;
import com.KDT.mosi.domain.entity.Product;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

  private final ProductDocumentRepository productDocumentRepository;
  private final ProductDAO productDAO;
  private final SearchTrendService searchTrendService;
  private final ElasticsearchClient esClient;
  private final ObjectMapper objectMapper;

  // 상품 데이터를 Elasticsearch에 인덱싱 (Bulk Indexing)
  @Transactional
  public void indexProducts(List<Product> products) {
    List<ProductDocument> productDocuments = products.stream()
        .map(this::convertToDocument)
        .collect(Collectors.toList());

    productDocumentRepository.saveAll(productDocuments);
    log.info("총 " + productDocuments.size() + "개의 도큐먼트가 성공적으로 인덱싱되었습니다.");
  }

  // Product 엔티티를 ProductDocument로 변환하는 헬퍼 메서드
  private ProductDocument convertToDocument(Product product) {
    return ProductDocument.builder()
        .productId(String.valueOf(product.getProductId()))
        .category(product.getCategory())
        .title(product.getTitle())
        .description(product.getDescription())
        .normalPrice(product.getNormalPrice())
        .guideYn(product.getGuideYn())
        .guidePrice(product.getGuidePrice())
        .salesPrice(product.getSalesPrice())
        .salesGuidePrice(product.getSalesGuidePrice())
        .totalDay(product.getTotalDay())
        .totalTime(product.getTotalTime())
        .reqMoney(product.getReqMoney())
        .sleepInfo(product.getSleepInfo())
        .transportInfo(product.getTransportInfo())
        .foodInfo(product.getFoodInfo())
        .reqPeople(product.getReqPeople())
        .target(product.getTarget())
        .stucks(product.getStucks())
        .detail(product.getDetail())
        .fileName(product.getFileName())
        .fileType(product.getFileType())
        .fileSize(product.getFileSize())
        .priceDetail(product.getPriceDetail())
        .gpriceDetail(product.getGpriceDetail())
        .status(product.getStatus())
        .createDate(product.getCreateDate().toLocalDate())
        .updateDate(product.getUpdateDate() != null ? product.getUpdateDate().toLocalDate() : null)
        .build();
  }

  /**
   * 키워드와 페이지네이션 정보를 받아 Elasticsearch에서 상품을 검색합니다.
   *
   * @param keyword 검색할 키워드
   * @param page    현재 페이지 번호 (1부터 시작)
   * @param size    한 페이지당 보여줄 상품 수
   * @return 검색된 상품 목록과 전체 개수를 포함한 ProductSearchResponse 객체
   */
  public ProductSearchResponse searchProducts(String keyword, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, size); // Pageable 객체 생성 (페이지 번호는 0부터 시작)

    // Spring Data Elasticsearch Repository를 이용한 검색
    // keyword를 두 번 전달하여 title과 description 모두 검색하도록 수정
    Page<ProductDocument> searchResults = productDocumentRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageRequest);

    // 검색된 키워드 저장
    searchTrendService.saveSearchKeyword(keyword);

    // ProductSearchResponse 객체로 결과 반환
    return ProductSearchResponse.builder()
        .products(searchResults.getContent())
        .totalCount(searchResults.getTotalElements())
        .build();
  }


  // 인덱스 존재 여부를 확인하고, 인덱스가 없는 경우에만 DB의 모든 상품을 인덱싱
  public void indexAllProductsFromDB() {
    try {
      // "products" 인덱스가 존재하는지 확인
      ExistsRequest existsRequest = ExistsRequest.of(e -> e.index("products"));
      boolean indexExists = esClient.indices().exists(existsRequest).value();

      // 새로 추가/수정된 코드
      if (indexExists) {
        log.info("products 인덱스가 이미 존재합니다. 기존 인덱스를 삭제하고 다시 인덱싱을 시작합니다.");
        DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(d -> d.index("products"));
        esClient.indices().delete(deleteRequest);
      }

      // 인덱스가 존재하지 않으면 인덱싱 시작
      log.info("products 인덱스가 존재하지 않습니다. DB의 모든 상품을 인덱싱합니다.");
      long totalCount = productDAO.countAll();
      int pageSize = 100;
      int totalPages = (int) Math.ceil((double) totalCount / pageSize);

      for (int page = 1; page <= totalPages; page++) {
        List<Product> products = productDAO.findAllByPage(page, pageSize);
        List<ProductDocument> productDocuments = products.stream()
            .map(this::convertToDocument)
            .collect(Collectors.toList());

        productDocumentRepository.saveAll(productDocuments);
        log.info("Page " + page + "/" + totalPages + " : " + products.size() + " products indexed.");
      }
      log.info("Total " + totalCount + " products have been successfully indexed.");

    } catch (IOException e) {
      System.err.println("인덱스 존재 여부 확인 중 오류가 발생했습니다: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // ✅ 새로운 방법으로 자동완성 메서드를 구현합니다.
  /**
   * 상품명(title)과 설명(description)을 기반으로 자동완성 검색 결과를 제공하는 메서드
   * (Spring Data Elasticsearch Repository를 사용하여 구현)
   * @param keyword 검색 키워드
   * @return 검색된 상품명 리스트
   */
  public List<String> searchAutocomplete(String keyword) {
    if (keyword == null || keyword.trim().isEmpty() || keyword.length() < 2) {
      return Collections.emptyList();
    }

    // ✅ 1. 'title' 필드에서 키워드를 포함하는 도큐먼트를 찾습니다.
    //    Containing은 SQL의 LIKE %keyword% 와 유사하게 작동합니다.
    //    우리가 원하는 '접두사 검색'에 가깝게 작동합니다.
    PageRequest pageRequest = PageRequest.of(0, 5); // 첫 페이지에서 최대 5개 결과만 가져옵니다.
    Page<ProductDocument> searchResults = productDocumentRepository.findByTitleContaining(keyword, pageRequest);

    // ✅ 2. 검색 결과에서 상품명(title)만 추출하여 리스트로 반환합니다.
    return searchResults.getContent().stream()
        .map(ProductDocument::getTitle)
        .collect(Collectors.toList());
  }
}