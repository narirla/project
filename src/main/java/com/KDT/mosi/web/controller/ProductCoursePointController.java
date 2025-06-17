package com.KDT.mosi.web;

import com.KDT.mosi.domain.entity.ProductCoursePoint;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-course-points")
@RequiredArgsConstructor
public class ProductCoursePointController {

  private final ProductCoursePointSVC coursePointSVC;

  // 코스포인트 등록
  @PostMapping
  public ResponseEntity<ProductCoursePoint> createCoursePoint(@RequestBody ProductCoursePoint coursePoint) {
    ProductCoursePoint created = coursePointSVC.save(coursePoint);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  // 상품별 코스포인트 목록 조회
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductCoursePoint>> getCoursePointsByProduct(@PathVariable Long productId) {
    List<ProductCoursePoint> points = coursePointSVC.getCoursePointsByProductId(productId);
    return ResponseEntity.ok(points);
  }

  // 단건 조회
  @GetMapping("/{id}")
  public ResponseEntity<ProductCoursePoint> getCoursePointById(@PathVariable Long id) {
    Optional<ProductCoursePoint> point = coursePointSVC.getCoursePointById(id);
    return point.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // 수정
  @PutMapping("/{id}")
  public ResponseEntity<ProductCoursePoint> updateCoursePoint(
      @PathVariable Long id,
      @RequestBody ProductCoursePoint coursePoint) {
    coursePoint.setCoursePointId(id);
    ProductCoursePoint updated = coursePointSVC.update(coursePoint);
    return ResponseEntity.ok(updated);
  }

  // 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCoursePoint(@PathVariable Long id) {
    coursePointSVC.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  // 특정 상품 코스 전체 삭제
  @DeleteMapping("/product/{productId}")
  public ResponseEntity<Void> deleteCoursePointsByProduct(@PathVariable Long productId) {
    coursePointSVC.deleteByProductId(productId);
    return ResponseEntity.noContent().build();
  }
}