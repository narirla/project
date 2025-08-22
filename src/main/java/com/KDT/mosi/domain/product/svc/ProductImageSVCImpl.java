package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.product.dao.ProductImageDAO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageSVCImpl implements ProductImageSVC {

  private final ProductImageDAO productImageDAO;

  @Override
  public void saveAll(List<ProductImage> images) {
    // 이미지가 null 이거나 비어있을 경우, 더 이상 진행하지 않음
    if (images == null || images.isEmpty()) {
      return;
    }

    for (ProductImage image : images) {
      // ⭐⭐⭐ NULL 값 방지: fileName이 null일 경우 빈 문자열로 설정 ⭐⭐⭐
      if (image.getFileName() == null) {
        image.setFileName("");
      }
      if (image.getFileSize() == null) {
        image.setFileSize(0L);
      }
      if (image.getMimeType() == null) {
        image.setMimeType("");
      }
      if (image.getImageData() == null) {
        image.setImageData(new byte[0]);
      }

      // fileName이 빈 문자열인 경우는 DB에 삽입하지 않고 건너뜁니다.
      if (image.getFileName().isEmpty()) {
        continue;
      }

      productImageDAO.insert(image);
    }
  }

  @Override
  public List<ProductImage> findByProductId(Long productId) {
    return productImageDAO.findByProductId(productId);
  }

  @Override
  public void deleteByProductId(Long productId) {
    productImageDAO.deleteByProductId(productId);
  }

  // getImagesByProductId 와 기능 중복, 필요 시 삭제 가능
  @Override
  public List<ProductImage> getImagesByProductId(Long productId) {
    return findByProductId(productId);
  }

  @Override
  public boolean addProductImage(ProductImage productImage) {
    return productImageDAO.insert(productImage) > 0;
  }

  @Override
  public boolean deleteProductImage(Long imageId) {
    return productImageDAO.delete(imageId) > 0;
  }

  @Override
  public Optional<ProductImage> findById(Long imageId) {
    return productImageDAO.findById(imageId);
  }
}