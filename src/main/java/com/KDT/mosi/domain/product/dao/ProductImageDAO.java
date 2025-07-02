package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageDAO {

  ProductImage save(ProductImage productImage);

  List<ProductImage> findByProductId(Long productId);

  Optional<ProductImage> findById(Long imageId);

  int update(ProductImage productImage);

  void deleteById(Long imageId);

  void deleteByProductId(Long productId);

}