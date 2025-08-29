package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.web.form.product.ProductTempSaveForm; // DTO 임포트
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

  // 사용자별 상품 전체 조회(페이징 포함)
  List<Product> getProductsByMemberIdAndPage(Long memberId, int page, int size);

  // 사용자별 상품 상태별 조회(페이징 포함)
  List<Product> getProductsByMemberIdAndStatusAndPage(Long memberId, String status, int page, int size);

  // 구매자 상품 목록 페이지(페이징 포함)
  List<Product> getProductsByCategoryAndPageAndSize(String category, int page, int size);

  // 페이징 조회
  List<Product> getProductsByPage(int pageNumber, int pageSize);

  // 전체 상품 수 조회
  long countAllProducts();

  // 판매자별 상품 수 조회
  long countByMemberIdAndStatus(Long memberId, String status);

  long countByCategory(String category);

  long countByMemberId(Long memberId);

  /**
   * 상품 상태(status) 변경
   * @param productId 상품 ID
   * @param status 변경할 상태 값
   */
  void updateProductStatus(Long productId, String status);

  /**
   * 상품 임시저장
   * @param form 임시저장할 상품 데이터 (DTO)
   * @param memberId 회원 ID
   * @return 임시저장된 상품 엔티티
   */
  Product saveTempProduct(ProductTempSaveForm form, Long memberId);
}