package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.web.form.product.ProductTempSaveForm; // DTO 임포트
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductSVCImpl implements ProductSVC {

  private final ProductDAO productDAO;
  private final MemberDAO memberDAO;

  public ProductSVCImpl(ProductDAO productDAO, MemberDAO memberDAO) {
    this.productDAO = productDAO;
    this.memberDAO = memberDAO;
  }

  @Override
  @Transactional
  public Product registerProduct(Product product) {
    Long memberId = extractMemberId(product);
    validateMemberId(memberId);
    return productDAO.insert(product);
  }

  @Override
  @Transactional
  public Product updateProduct(Product product) {
    Long memberId = extractMemberId(product);
    validateMemberId(memberId);
    return productDAO.update(product);
  }

  @Override
  @Transactional
  public void removeProduct(Long productId) {
    productDAO.delete(productId);
  }

  @Override
  public Optional<Product> getProduct(Long productId) {
    return productDAO.findById(productId);
  }

  @Override
  public List<Product> getProductsByMemberIdAndPage(Long memberId, int page, int size){
    return productDAO.findByMemberIdWithPaging(memberId, page, size);
  }

  @Override
  public List<Product> getProductsByMemberIdAndStatusAndPage(Long memberId, String status, int page, int size) {
    return productDAO.findByMemberIdAndStatusWithPaging(memberId, status, page, size);
  }

  // 카테고리별 상품출력
  @Override
  public List<Product> getProductsByCategoryAndPageAndSize(String category, int page, int size) {
    return productDAO.findByCategoryWithPaging(category, page, size);
  }

  @Transactional
  public void updateProductStatus(Long productId, String status) {
    Optional<Product> optionalProduct = productDAO.findById(productId);
    if (optionalProduct.isEmpty()) {
      throw new IllegalArgumentException("해당 상품이 존재하지 않습니다.");
    }

    Product product = optionalProduct.get();
    product.setStatus(status);
    product.setUpdateDate(new Date(System.currentTimeMillis()));
    productDAO.update(product);
  }

  @Override
  public long countByMemberIdAndStatus(Long memberId, String status) {
    return productDAO.countByMemberIdAndStatus(memberId, status);
  }

  // 카테고리별 상품 갯수
  @Override
  public long countByCategory(String category) {
    return productDAO.countByCategory(category);
  }

  @Override
  public List<Product> getProductsByPage(int pageNumber, int pageSize) {
    return productDAO.findAllByPage(pageNumber, pageSize);
  }

  @Override
  public long countAllProducts() {
    return productDAO.countAll();
  }

  private Long extractMemberId(Product product) {
    if (product == null || product.getMember() == null) {
      return null;
    }
    return product.getMember().getMemberId();
  }

  private void validateMemberId(Long memberId) {
    if (memberId == null || !memberDAO.isExistMemberId(memberId)) {
      throw new IllegalArgumentException("존재하지 않는 회원 ID입니다: " + memberId);
    }
  }

  public long countByMemberId(Long memberId){ return productDAO.countByMemberId(memberId); }

  // -------------------------------------------------------------
  // ✨✨✨ 임시저장 기능 구현 부분 ✨✨✨
  // -------------------------------------------------------------
  @Override
  @Transactional
  public Product saveTempProduct(ProductTempSaveForm form, Long memberId) {
    // 1. DTO -> 엔티티 변환
    Product product = new Product();

    // memberId 설정 (member 엔티티를 조회하여 설정)
    product.setMember(memberDAO.findById(memberId).orElseThrow(
        () -> new IllegalArgumentException("존재하지 않는 회원 ID입니다.")
    ));

    // 백엔드에서 자동으로 설정하는 필수 필드
    product.setStatus("임시저장");
    product.setCreateDate(new Date(System.currentTimeMillis()));
    product.setUpdateDate(new Date(System.currentTimeMillis()));

    // 2. 사용자가 입력한 필드 설정 (NULL 허용)
    // title
    product.setTitle(form.getTitle());

    // category
    product.setCategory(form.getCategory() != null && !form.getCategory().isEmpty() ? form.getCategory() : "임시저장");

    // 나머지 필드들
    product.setGuideYn(form.getGuideYn());
    product.setNormalPrice(form.getNormalPrice());
    product.setGuidePrice(form.getGuidePrice());
    product.setSalesPrice(form.getSalesPrice());
    product.setSalesGuidePrice(form.getSalesGuidePrice());
    product.setTotalDay(form.getTotalDay());
    product.setTotalTime(form.getTotalTime());
    product.setReqMoney(form.getReqMoney());
    product.setSleepInfo(form.getSleepInfo());
    product.setTransportInfo(form.getTransportInfo());
    product.setFoodInfo(form.getFoodInfo());
    product.setReqPeople(form.getReqPeople());
    product.setTarget(form.getTarget());
    product.setStucks(form.getStucks());
    product.setDescription(form.getDescription());
    product.setDetail(form.getDetail());

    // 줄바꿈 정규화 (\r\n → \n)
    if (form.getPriceDetail() != null) {
      product.setPriceDetail(form.getPriceDetail().replaceAll("\r\n", "\n"));
    }
    if (form.getGpriceDetail() != null) {
      product.setGpriceDetail(form.getGpriceDetail().replaceAll("\r\n", "\n"));
    }


    // 3. 파일 처리
    if (form.getDocumentFile() != null && !form.getDocumentFile().isEmpty()) {
      try {
        product.setFileName(form.getDocumentFile().getOriginalFilename());
        product.setFileType(form.getDocumentFile().getContentType());
        product.setFileSize(form.getDocumentFile().getSize());
        product.setFileData(form.getDocumentFile().getBytes());
      } catch (IOException e) {
        log.error("파일 처리 중 오류 발생", e);
        throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
      }
    }

    // 4. DAO를 통해 DB에 저장 (insert)
    return productDAO.insert(product);
  }
}