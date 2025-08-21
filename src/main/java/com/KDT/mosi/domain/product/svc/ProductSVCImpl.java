package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.domain.product.document.ProductDocument;
import com.KDT.mosi.domain.product.repository.ProductDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductSVCImpl implements ProductSVC {

  private final ProductDAO productDAO;
  private final MemberDAO memberDAO;
  private final ProductDocumentRepository productDocumentRepository;
  private final SellerPageSVC sellerPageSVC;

  // ------------------------------------ 상품 CRUD ------------------------------------

  /**
   * 상품 등록 (insert) - Oracle DB 저장 후 Elasticsearch 동기화
   */
  @Override
  @Transactional
  public Product registerProduct(Product product) {
    Long memberId = extractMemberId(product);
    validateMemberId(memberId);

    // 1. Oracle DB에 상품 등록
    Product savedProduct = productDAO.insert(product);
    log.info("Oracle DB에 상품이 등록되었습니다. 상품 ID: {}", savedProduct.getProductId());

    // 2. Elasticsearch에 상품 동기화
    syncToElasticsearch(savedProduct);
    log.info("Elasticsearch에 상품이 동기화되었습니다. 상품 ID: {}", savedProduct.getProductId());

    return savedProduct;
  }

  // 신규 상품(임시저장 포함) 등록 메서드
  @Override
  public Product saveNewProduct(Product product, MultipartFile[] uploadFiles) {
    try {
      // 1. 대표 이미지 처리: 파일이 없는 경우 NULL이 아닌 빈 값으로 설정
      if (uploadFiles != null && uploadFiles.length > 0 && !uploadFiles[0].isEmpty()) {
        MultipartFile mainFile = uploadFiles[0];
        product.setFileName(mainFile.getOriginalFilename());
        product.setFileData(mainFile.getBytes());
        product.setFileType(mainFile.getContentType());
        product.setFileSize(mainFile.getSize());
      } else {
        product.setFileName("");
        product.setFileData(new byte[0]);
        product.setFileType("");
        product.setFileSize(0L);
      }

      // 2. 상품 정보 저장 및 productId 확보
      Product savedProduct = productDAO.insert(product);

      // 3. 추가 이미지 리스트 처리
      if (uploadFiles != null && uploadFiles.length > 1) {
        List<ProductImage> productImages = new ArrayList<>();
        int imageOrder = 1;
        for (int i = 1; i < uploadFiles.length; i++) {
          MultipartFile addFile = uploadFiles[i];
          if (!addFile.isEmpty()) {
            ProductImage img = new ProductImage();
            img.setProduct(savedProduct);
            img.setFileName(addFile.getOriginalFilename());
            try {
              img.setImageData(addFile.getBytes());
            } catch (IOException e) {
              log.error("Failed to read image data for file: {}", addFile.getOriginalFilename(), e);
              continue;
            }
            img.setFileSize(addFile.getSize());
            img.setMimeType(addFile.getContentType());
            img.setImageOrder(imageOrder++);
            productImages.add(img);
          }
        }
        if (!productImages.isEmpty()) {
          productDAO.saveImages(productImages);
        }
      }
      return savedProduct;

    } catch (IOException e) {
      log.error("파일 처리 중 예외 발생: {}", e.getMessage(), e);
      throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
    }
  }

// ⭐⭐ 기존 상품 수정/업데이트 메서드
@Override
@Transactional
public void updateProduct(Product product, MultipartFile[] uploadImages, String deleteImageIds) {
  try {
    // 1. 상품 엔티티 수정
    Product updatedProduct = productDAO.update(product);

    // 2. 삭제할 기존 이미지 처리
    if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
      List<Long> idsToDelete = Arrays.stream(deleteImageIds.split(","))
          .map(Long::parseLong)
          .collect(Collectors.toList());
      productDAO.deleteImagesByIds(idsToDelete);
    }

    // 3. 새로 업로드된 이미지 저장
    if (uploadImages != null && uploadImages.length > 0) {
      saveProductImages(updatedProduct, Arrays.asList(uploadImages));
    }

    // 4. Elasticsearch 동기화
    syncToElasticsearch(updatedProduct);
  } catch (IOException e) {
    log.error("상품 이미지 업데이트 중 오류 발생", e);
    throw new RuntimeException("상품 이미지 업데이트 중 오류가 발생했습니다.", e);
  }
}

/**
 * 상품 수정 (update) - Oracle DB 수정 후 Elasticsearch 동기화
 */
@Override
@Transactional
public Product updateProduct(Product product) {
  Long memberId = extractMemberId(product);
  validateMemberId(memberId);

  // 1. Oracle DB에 상품 정보 업데이트
  Product updatedProduct = productDAO.update(product);
  log.info("Oracle DB에 상품이 수정되었습니다. 상품 ID: {}", updatedProduct.getProductId());

  // 2. Elasticsearch에 상품 동기화 (업데이트)
  syncToElasticsearch(updatedProduct);
  log.info("Elasticsearch에 상품이 업데이트되었습니다. 상품 ID: {}", updatedProduct.getProductId());

  return updatedProduct;
}

/**
 * 상품 삭제 - Oracle DB 삭제 후 Elasticsearch에서도 삭제
 */
@Override
@Transactional
public void removeProduct(Long productId) {
  // 1. Oracle DB에서 상품 삭제
  productDAO.delete(productId);
  log.info("Oracle DB에서 상품이 삭제되었습니다. 상품 ID: {}", productId);

  // 2. Elasticsearch에서도 상품 삭제
  productDocumentRepository.deleteById(productId);
  log.info("Elasticsearch에서 상품이 삭제되었습니다. 상품 ID: {}", productId);
}

// ------------------------------------ 기타 비즈니스 로직 ------------------------------------

@Override
@Transactional
public void updateProductStatus(Long productId, String status) {
  Optional<Product> optionalProduct = productDAO.findById(productId);
  if (optionalProduct.isEmpty()) {
    throw new IllegalArgumentException("해당 상품이 존재하지 않습니다.");
  }

  Product product = optionalProduct.get();
  product.setStatus(status);
  product.setUpdateDate(new Date(System.currentTimeMillis())); // 현재 시간으로 세팅
  productDAO.update(product);
  log.info("상품 상태가 변경되었습니다. 상품 ID: {}, 상태: {}", productId, status);

  // 상태 변경 후 Elasticsearch 동기화
  syncToElasticsearch(product);
  log.info("Elasticsearch에 상품 상태가 동기화되었습니다. 상품 ID: {}", productId);
}

// ------------------------------------ 헬퍼 메서드 ------------------------------------

// 이미지 저장을 위한 헬퍼 메서드
private void saveProductImages(Product product, List<MultipartFile> files) throws IOException {
  // 기존 이미지 중 가장 높은 order 값 가져오기
  Integer lastOrder = productDAO.findMaxImageOrderByProductId(product.getProductId());
  int newOrder = (lastOrder != null) ? lastOrder + 1 : 1;

  List<ProductImage> newImages = new ArrayList<>();
  for (MultipartFile file : files) {
    if (file != null && !file.isEmpty()) {
      ProductImage pi = new ProductImage();
      pi.setProduct(product);
      pi.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
      pi.setFileSize(file.getSize());
      pi.setMimeType(file.getContentType());
      pi.setImageOrder(newOrder++);
      pi.setImageData(file.getBytes());
      newImages.add(pi);
    }
  }
  productDAO.saveImages(newImages);
}

/**
 * Oracle DB의 Product 엔티티를 Elasticsearch 도큐먼트로 변환하여 동기화합니다.
 *
 * @param product 동기화할 Product 엔티티
 */
private void syncToElasticsearch(Product product) {
  String nickname = null;
  Long memberId = null;

  if (product.getMember() != null) {
    memberId = product.getMember().getMemberId();
    // SellerPageSVC를 통해 닉네임 조회
    Optional<String> nicknameOptional = sellerPageSVC.getNicknameByMemberId(memberId);
    if (nicknameOptional.isPresent()) {
      nickname = nicknameOptional.get();
    }
  }

  ProductDocument document = new ProductDocument();
  document.setProductId(product.getProductId());
  document.setMemberId(memberId);
  document.setCategory(product.getCategory());
  document.setTitle(product.getTitle());
  document.setNickname(nickname);
  document.setSalesPrice(product.getSalesPrice());
  document.setSalesGuidePrice(product.getSalesGuidePrice());
  document.setTotalDay(product.getTotalDay());
  document.setTotalTime(product.getTotalTime());
  document.setReqMoney(product.getReqMoney());
  document.setSleepInfo(product.getSleepInfo());
  document.setTransportInfo(product.getTransportInfo());
  document.setFoodInfo(product.getFoodInfo());
  document.setReqPeople(product.getReqPeople());
  document.setTarget(product.getTarget());
  document.setStucks(product.getStucks());
  document.setDescription(product.getDescription());
  document.setStatus(product.getStatus());
  document.setCreateDate(product.getCreateDate() != null ? new java.util.Date(product.getCreateDate().getTime()) : null);
  document.setUpdateDate(product.getUpdateDate() != null ? new java.util.Date(product.getUpdateDate().getTime()) : null);

  productDocumentRepository.save(document);
}

// ------------------------------------ 기존에 있던 다른 메서드들은 변경 없이 그대로 유지 ------------------------------------

@Override
public Optional<Product> getProduct(Long productId) {
  return productDAO.findById(productId);
}

@Override
public List<Product> getProductsByMemberIdAndPage(Long memberId, int page, int size) {
  return productDAO.findByMemberIdWithPaging(memberId, page, size);
}

@Override
public List<Product> getProductsByMemberIdAndStatusAndPage(Long memberId, String status, int page, int size) {
  return productDAO.findByMemberIdAndStatusWithPaging(memberId, status, page, size);
}

@Override
public List<Product> getProductsByCategoryAndPageAndSize(String category, int page, int size) {
  return productDAO.findByCategoryWithPaging(category, page, size);
}

@Override
public long countByMemberIdAndStatus(Long memberId, String status) {
  return productDAO.countByMemberIdAndStatus(memberId, status);
}

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

@Override
public long countByMemberId(Long memberId) {
  return productDAO.countByMemberId(memberId);
}

// ------------------------------------ 유효성 검사 헬퍼 메서드 ------------------------------------

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
}