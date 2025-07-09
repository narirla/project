package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductSVC {
  // 상품 등록 (insert)
  Product registerProduct(Product product);

  // 상품 수정 (update)
  Product updateProduct(Product product);

  // 상품 삭제
  void removeProduct(Long productId);

  // 상품 조회
  Optional<Product> getProduct(Long productId);

  // 페이징 조회
  List<Product> getProductsByPage(int pageNumber, int pageSize);

  // 전체 상품 수 조회
  long countAllProducts();
}
