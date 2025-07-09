package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.ProductCoursePoint;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-course-points")
public class ProductCoursePointController {

  private final ProductCoursePointSVC productCoursePointService;

  public ProductCoursePointController(ProductCoursePointSVC productCoursePointService) {
    this.productCoursePointService = productCoursePointService;
  }

  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductCoursePoint>> getPointsByProduct(@PathVariable Long productId) {
    List<ProductCoursePoint> points = productCoursePointService.getPointsByProductId(productId);
    return ResponseEntity.ok(points);
  }

  @PostMapping
  public ResponseEntity<String> addCoursePoint(@RequestBody ProductCoursePoint point) {
    boolean success = productCoursePointService.addCoursePoint(point);
    if(success) return ResponseEntity.ok("코스포인트 등록 성공");
    else return ResponseEntity.status(500).body("코스포인트 등록 실패");
  }

  @DeleteMapping("/{coursePointId}")
  public ResponseEntity<String> deleteCoursePoint(@PathVariable Long coursePointId) {
    boolean success = productCoursePointService.deleteCoursePoint(coursePointId);
    if(success) return ResponseEntity.ok("코스포인트 삭제 성공");
    else return ResponseEntity.status(500).body("코스포인트 삭제 실패");
  }
}