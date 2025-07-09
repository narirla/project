package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;
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

  // 페이징 처리된 상품 리스트 조회 (pageNumber: 1부터 시작, pageSize: 한 페이지에 보여줄 상품 갯수)
  List<Product> findAllByPage(int pageNumber, int pageSize);

  // 전체 상품 갯수 조회
  long countAll();

  // 멤버 존재 여부 확인
  public interface MemberDAO {
    boolean existsById(Long memberId);
  }
}