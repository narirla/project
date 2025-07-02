package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.ProductCoursePoint;
import com.KDT.mosi.domain.product.dao.ProductCoursePointDAO;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCoursePointSVCImpl implements ProductCoursePointSVC {

  private final ProductCoursePointDAO coursePointDAO;

  @Override
  public ProductCoursePoint save(ProductCoursePoint coursePoint) {
    return coursePointDAO.save(coursePoint);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductCoursePoint> getCoursePointsByProductId(Long productId) {
    return coursePointDAO.findByProductId(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductCoursePoint> getCoursePointById(Long coursePointId) {
    return coursePointDAO.findById(coursePointId);
  }

  @Override
  public ProductCoursePoint update(ProductCoursePoint coursePoint) {
    coursePointDAO.update(coursePoint);
    return coursePoint;
  }

  @Override
  public void deleteById(Long coursePointId) {
    coursePointDAO.deleteById(coursePointId);
  }

  @Override
  public void deleteByProductId(Long productId) {
    coursePointDAO.deleteByProductId(productId);
  }
}