package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.domain.product.document.ProductDocument;
import com.KDT.mosi.domain.product.repository.ProductDocumentRepository;
import com.KDT.mosi.domain.entity.Product;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    System.out.println("총 " + productDocuments.size() + "개의 도큐먼트가 성공적으로 인덱싱되었습니다.");
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


  // 키워드 검색 (multi_match 쿼리 사용)
  public List<ProductDocument> searchProductsByKeyword(String keyword) throws IOException {
    Query multiMatchQuery = MultiMatchQuery.of(m -> m
        .query(keyword)
        .fields("title", "description", "category")
    )._toQuery();

    SearchResponse<ProductDocument> response = esClient.search(s -> s
            .index("products")
            .query(multiMatchQuery)
        , ProductDocument.class
    );

    List<ProductDocument> products = response.hits().hits().stream()
        .map(Hit::source)
        .collect(Collectors.toList());

    searchTrendService.updateSearchTrend(keyword);

    return products;
  }

  // 인덱스 존재 여부를 확인하고, 인덱스가 없는 경우에만 DB의 모든 상품을 인덱싱
  public void indexAllProductsFromDB() {
    try {
      // "products" 인덱스가 존재하는지 확인
      ExistsRequest existsRequest = ExistsRequest.of(e -> e.index("products"));
      boolean indexExists = esClient.indices().exists(existsRequest).value();

      if (indexExists) {
        System.out.println("products 인덱스가 이미 존재합니다. 인덱싱을 건너뜁니다.");
        return; // 인덱스가 존재하면 메서드 종료
      }

      // 인덱스가 존재하지 않으면 인덱싱 시작
      System.out.println("products 인덱스가 존재하지 않습니다. DB의 모든 상품을 인덱싱합니다.");
      long totalCount = productDAO.countAll();
      int pageSize = 100;
      int totalPages = (int) Math.ceil((double) totalCount / pageSize);

      for (int page = 1; page <= totalPages; page++) {
        List<Product> products = productDAO.findAllByPage(page, pageSize);
        List<ProductDocument> productDocuments = products.stream()
            .map(this::convertToDocument)
            .collect(Collectors.toList());

        productDocumentRepository.saveAll(productDocuments);
        System.out.println("Page " + page + "/" + totalPages + " : " + products.size() + " products indexed.");
      }
      System.out.println("Total " + totalCount + " products have been successfully indexed.");

    } catch (IOException e) {
      System.err.println("인덱스 존재 여부 확인 중 오류가 발생했습니다: " + e.getMessage());
      e.printStackTrace();
    }
  }
}