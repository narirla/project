package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.product.dao.ProductImageDAO;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageSVCImpl implements ProductImageSVC {

  private final ProductImageDAO productImageDAO;

  @Override
  public ProductImage save(ProductImage productImage) {
    return productImageDAO.save(productImage);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductImage> getImagesByProductId(Long productId) {
    return productImageDAO.findByProductId(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductImage> getImageById(Long imageId) {
    return productImageDAO.findById(imageId);
  }

  @Override
  public ProductImage update(ProductImage productImage) {
    productImageDAO.update(productImage);
    return productImage;
  }

  @Override
  public void deleteById(Long imageId) {
    productImageDAO.deleteById(imageId);
  }

  @Override
  public void deleteByProductId(Long productId) {
    productImageDAO.deleteByProductId(productId);
  }
}