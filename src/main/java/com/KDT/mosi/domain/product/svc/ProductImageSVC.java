package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageSVC {

  ProductImage save(ProductImage productImage);

  List<ProductImage> getImagesByProductId(Long productId);

  Optional<ProductImage> getImageById(Long imageId);

  ProductImage update(ProductImage productImage);

  void deleteById(Long imageId);

  void deleteByProductId(Long productId);
}