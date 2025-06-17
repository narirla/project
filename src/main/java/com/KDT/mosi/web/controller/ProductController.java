package com.KDT.mosi.web;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductSVC productSVC;

  // 상품 등록
  @PostMapping
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    log.info("Create product request: {}", product);
    Product savedProduct = productSVC.save(product);
    return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
  }

  // 상품 전체 목록 (페이징)
  @GetMapping
  public ResponseEntity<List<Product>> getProducts(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int numOfRows
  ) {
    log.info("Get products request: pageNo={}, numOfRows={}", pageNo, numOfRows);
    List<Product> products = productSVC.findAll(pageNo, numOfRows);
    return ResponseEntity.ok(products);
  }

  // 결제 전 상세 조회
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    log.info("Get product by id (before pay): {}", id);
    Optional<Product> productOpt = productSVC.findById(id);
    return productOpt.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // 결제 후 상세 조회
  @GetMapping("/{id}/after-pay")
  public ResponseEntity<Product> getProductByIdAfterPay(@PathVariable Long id) {
    log.info("Get product by id (after pay): {}", id);
    Optional<Product> productOpt = productSVC.findByIdAfterPay(id);
    return productOpt.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // 수정
  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id,
      @RequestBody Product product
  ) {
    log.info("Update product id {} with data: {}", id, product);
    // ID 일치 체크 (선택사항: 클라이언트 데이터 신뢰도에 따라)
    if (!id.equals(product.getProductId())) {
      return ResponseEntity.badRequest().build();
    }
    Product updatedProduct = productSVC.updateById(id, product);
    return ResponseEntity.ok(updatedProduct);
  }

  // 단건 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    log.info("Delete product id: {}", id);
    productSVC.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  // 여러건 삭제
  @DeleteMapping
  public ResponseEntity<Void> deleteProducts(@RequestBody List<Long> ids) {
    log.info("Delete multiple products ids: {}", ids);
    productSVC.deleteByIds(ids);
    return ResponseEntity.noContent().build();
  }

  // 총 상품 수 조회 (선택적 API)
  @GetMapping("/count")
  public ResponseEntity<Long> getProductCount() {
    long count = productSVC.getTotalCount();
    log.info("Total product count: {}", count);
    return ResponseEntity.ok(count);
  }
}