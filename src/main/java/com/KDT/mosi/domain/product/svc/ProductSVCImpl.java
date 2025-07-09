package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductSVCImpl implements ProductSVC {

  private final ProductDAO productDAO;
  private final MemberDAO memberDAO;

  public ProductSVCImpl(ProductDAO productDAO, MemberDAO memberDAO) {
    this.productDAO = productDAO;
    this.memberDAO = memberDAO;
  }

  @Override
  public Product registerProduct(Product product) {
    Long memberId = extractMemberId(product);
    validateMemberId(memberId);
    return productDAO.insert(product);
  }

  @Override
  public Product updateProduct(Product product) {
    Long memberId = extractMemberId(product);
    validateMemberId(memberId);
    // 기존 상품 존재 여부 체크 추가 가능
    return productDAO.update(product);
  }

  @Override
  public void removeProduct(Long productId) {
    productDAO.delete(productId);
  }

  @Override
  public Optional<Product> getProduct(Long productId) {
    return productDAO.findById(productId);
  }

  @Override
  public List<Product> getProductsByPage(int pageNumber, int pageSize) {
    return productDAO.findAllByPage(pageNumber, pageSize);
  }

  @Override
  public long countAllProducts() {
    return productDAO.countAll();
  }

  // Member 객체에서 ID 안전하게 추출
  private Long extractMemberId(Product product) {
    if (product == null || product.getMember() == null) {
      return null;
    }
    return product.getMember().getMemberId();
  }

  // memberId 유효성 검사
  private void validateMemberId(Long memberId) {
    if (memberId == null || !memberDAO.isExistMemberId(memberId)) {
      throw new IllegalArgumentException("존재하지 않는 회원 ID입니다: " + memberId);
    }
  }
}