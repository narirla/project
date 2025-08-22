package com.KDT.mosi.domain.product.svc;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.domain.product.document.ProductDocument;
import com.KDT.mosi.domain.product.dto.response.ProductSearchResponse;
import com.KDT.mosi.domain.product.repository.ProductDocumentRepository;
import com.KDT.mosi.domain.product.repository.SearchTrendDocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
  private final ElasticsearchOperations elasticsearchOperations;
  private final ElasticsearchClient esClient;
  private final SearchTrendDocumentRepository searchTrendDocumentRepository;

  @PostConstruct
  public void setupIndex() {
    if (elasticsearchOperations.indexOps(ProductDocument.class).exists()) {
      log.info("products 인덱스가 이미 존재합니다. 삭제 후 다시 생성합니다.");
      elasticsearchOperations.indexOps(ProductDocument.class).delete();
    }

    log.info("새로운 products 인덱스를 생성하고 한국어(nori) 분석기 매핑을 적용합니다.");
    elasticsearchOperations.indexOps(ProductDocument.class).create();
    elasticsearchOperations.indexOps(ProductDocument.class).putMapping();

    indexAllProductsFromDB();
  }

  @Transactional
  public void indexProducts(List<Product> products) {
    List<ProductDocument> productDocuments = products.stream()
        .map(this::convertToDocument)
        .collect(Collectors.toList());
    productDocumentRepository.saveAll(productDocuments);
    log.info("총 " + productDocuments.size() + "개의 도큐먼트가 성공적으로 인덱싱되었습니다.");
  }

  private ProductDocument convertToDocument(Product product) {
    return ProductDocument.builder()
        .productId(String.valueOf(product.getProductId()))
        .category(product.getCategory())
        .title(product.getTitle())
        .description(product.getDescription())
        .autocomplete_title(product.getTitle())
        .autocomplete_description(product.getDescription())
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

  public ProductSearchResponse searchProducts(String keyword, int page, int size) {
    String normalizedKeyword = keyword.trim().toLowerCase();

    Query query = Query.of(q -> q
        .multiMatch(m -> m
            .fields("title^3", "description^2", "autocomplete_title^2", "autocomplete_description^2")
            .query(normalizedKeyword)
            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
        )
    );

    org.springframework.data.elasticsearch.core.query.Query nativeQuery = new NativeQueryBuilder()
        .withQuery(query)
        .withPageable(PageRequest.of(page - 1, size))
        .build();

    SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

    long totalCount = searchHits.getTotalHits();

    if (totalCount > 0) {
      searchTrendService.saveSearchKeyword(keyword);
    }

    List<ProductDocument> products = searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());

    return ProductSearchResponse.builder()
        .products(products)
        .totalCount(totalCount)
        .build();
  }

  public void indexAllProductsFromDB() {
    log.info("DB의 모든 상품을 인덱싱합니다.");
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
  }

  public List<String> searchAutocomplete(String keyword) {
    if (keyword == null || keyword.trim().isEmpty() || keyword.length() < 1) {
      return Collections.emptyList();
    }

    Query query = Query.of(q -> q
        .match(m -> m
            .field("autocomplete_title")
            .query(keyword)
        )
    );

    org.springframework.data.elasticsearch.core.query.Query nativeQuery = new NativeQueryBuilder()
        .withQuery(query)
        .withPageable(PageRequest.of(0, 5))
        .build();

    SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

    return searchHits.getSearchHits().stream()
        .map(SearchHit::getContent)
        .map(ProductDocument::getTitle)
        .filter(title -> title.length() <= 40)
        .collect(Collectors.toList());
  }

  /**
   * 오타가 발생했을 경우 유사한 검색어를 제안하는 메서드
   * @param keyword 검색어
   * @return 제안 검색어 목록
   */
 public List<String> suggestSearchTerms(String keyword) {
//    if (keyword == null || keyword.trim().isEmpty()) {
//      return Collections.emptyList();
//    }
//
//    // 1. Suggest 요청을 위한 Query 생성
//    Query query = Query.findAll()
//        .withSuggester(Suggester.term("title-suggester", keyword)
//            .field("title")
//            .size(5)); // 최대 5개 추천
//
//    // 2. Elasticsearch에 Suggestion 요청을 보냅니다.
//    SearchHits<?> searchHits = elasticsearchOperations.search(query, ProductDocument.class, IndexCoordinates.of("products"));
//
    List<String> suggestions = new ArrayList<>();
//
//    // 3. SearchHits에서 Suggestion 결과를 추출합니다. (Elasticsearch 8.x+용 수정)
//    if (searchHits.hasSuggest()) {
//      List<Suggestion<TermSuggestOption>> termSuggestions = searchHits.getSuggest().getSuggestions("title-suggester");
//      if (termSuggestions != null && !termSuggestions.isEmpty()) {
//        suggestions = termSuggestions.get(0).getOptions().stream()
//            .map(TermSuggestOption::getText)
//            .collect(Collectors.toList());
//      }
//    }
    return suggestions;
 }
}