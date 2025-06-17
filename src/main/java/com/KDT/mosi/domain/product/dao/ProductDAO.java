package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {

  // 상품 등록 또는 저장
  Product save(Product product);

  // 전체 목록 조회(페이징 포함)
  List<Product> findAll(int pageNo, int numOfRows);

  // 상세페이지 조회
  Optional<Product> findById(Long id);

  // 결제 후 상세페이지 (상세페이지와 다르게 결제상태 반영 등 추가 조건 가능)
  Optional<Product> findByIdAfterPay(Long id);

  // 수정 (ID 기준)
  Product updateById(Long productId, Product product);

  // 단건 삭제
  void deleteById(Long id);

  // 여러건 삭제
  void deleteByIds(List<Long> ids);

  // 상품 총 건수 조회 (페이징용)
  long getTotalCount();
}