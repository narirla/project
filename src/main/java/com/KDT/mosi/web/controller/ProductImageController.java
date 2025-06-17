package com.KDT.mosi.web;

import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

  private final ProductImageSVC productImageSVC;

  // 이미지 등록
  @PostMapping
  public ResponseEntity<ProductImage> createImage(@RequestBody ProductImage productImage) {
    ProductImage saved = productImageSVC.save(productImage);
    return new ResponseEntity<>(saved, HttpStatus.CREATED);
  }

  // 상품 이미지 목록 조회
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductImage>> getImagesByProductId(@PathVariable Long productId) {
    List<ProductImage> images = productImageSVC.getImagesByProductId(productId);
    return ResponseEntity.ok(images);
  }

  // 이미지 단건 조회
  @GetMapping("/{imageId}")
  public ResponseEntity<ProductImage> getImageById(@PathVariable Long imageId) {
    Optional<ProductImage> image = productImageSVC.getImageById(imageId);
    return image.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // 이미지 수정
  @PutMapping("/{imageId}")
  public ResponseEntity<ProductImage> updateImage(@PathVariable Long imageId, @RequestBody ProductImage productImage) {
    productImage.setImageId(imageId);
    ProductImage updated = productImageSVC.update(productImage);
    return ResponseEntity.ok(updated);
  }

  // 이미지 삭제
  @DeleteMapping("/{imageId}")
  public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
    productImageSVC.deleteById(imageId);
    return ResponseEntity.noContent().build();
  }

  // 특정 상품 이미지 전체 삭제
  @DeleteMapping("/product/{productId}")
  public ResponseEntity<Void> deleteImagesByProduct(@PathVariable Long productId) {
    productImageSVC.deleteByProductId(productId);
    return ResponseEntity.noContent().build();
  }
}