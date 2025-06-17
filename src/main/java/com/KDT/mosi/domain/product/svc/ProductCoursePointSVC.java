package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.ProductCoursePoint;

import java.util.List;
import java.util.Optional;

public interface ProductCoursePointSVC {

  ProductCoursePoint save(ProductCoursePoint coursePoint);

  List<ProductCoursePoint> getCoursePointsByProductId(Long productId);

  Optional<ProductCoursePoint> getCoursePointById(Long coursePointId);

  ProductCoursePoint update(ProductCoursePoint coursePoint);

  void deleteById(Long coursePointId);

  void deleteByProductId(Long productId);
}