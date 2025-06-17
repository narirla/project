package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.ProductCoursePoint;

import java.util.List;
import java.util.Optional;

public interface ProductCoursePointDAO {

  ProductCoursePoint save(ProductCoursePoint coursePoint);

  List<ProductCoursePoint> findByProductId(Long productId);

  Optional<ProductCoursePoint> findById(Long coursePointId);

  int update(ProductCoursePoint coursePoint);

  void deleteById(Long coursePointId);

  void deleteByProductId(Long productId);
}