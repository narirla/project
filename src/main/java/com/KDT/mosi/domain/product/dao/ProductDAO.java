package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
  // 상품 생성
  Product insert(Product product);

  // 상품 수정
  Product update(Product product);

  // 상품 삭제
  void delete(Long productId);

  // 상품 개별 조회
  Optional<Product> findById(Long productId);

  // 멤버별 상품 전체조회(페이징 포함)
  List<Product> findByMemberIdWithPaging(Long memberId, int page, int size);

  // 카테고리별 조회(페이징 포함)
  List<Product> findByCategoryWithPaging(String category, int page, int size);

  // 멤버별 상품 상태별 조회(페이징 포함)
  List<Product> findByMemberIdAndStatusWithPaging(Long memberId, String status, int page, int size);

  // 페이징 처리된 상품 리스트 조회 (pageNumber: 1부터 시작, pageSize: 한 페이지에 보여줄 상품 갯수)
  List<Product> findAllByPage(int pageNumber, int pageSize);

  // 전체 상품 갯수 조회
  long countAll();

  // 카테고리별 상품 갯수 조회
  long countByCategory(String category);

  // 멤버 존재 여부 확인
  public interface MemberDAO {
    boolean existsById(Long memberId);
  }
  // 판매자별 상품 등록 갯수 확인(status 포함)
  long countByMemberIdAndStatus(Long memberId, String status);

  // 판매자별 상품 등록 갯수 확인
  long countByMemberId(Long memberId);

  // ⭐⭐⭐ 새로 추가된 메서드들 ⭐⭐⭐

  /**
   * 새로운 상품 이미지 리스트를 저장합니다.
   * @param images 저장할 ProductImage 리스트
   */
  void saveImages(List<ProductImage> images);

  /**
   * 주어진 이미지 ID 리스트에 해당하는 이미지를 삭제합니다.
   * @param imageIds 삭제할 이미지 ID 리스트
   */
  void deleteImagesByIds(List<Long> imageIds);

  /**
   * 특정 상품에 속한 이미지 중 가장 높은 이미지 순서(imageOrder)를 조회합니다.
   * @param productId 상품 ID
   * @return 가장 높은 imageOrder 값 또는 이미지가 없을 경우 null
   */
  Integer findMaxImageOrderByProductId(Long productId);
}