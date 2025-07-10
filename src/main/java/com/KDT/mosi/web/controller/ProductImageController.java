package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-images")
public class ProductImageController {

  private final ProductSVC productSVC;
  private final ProductImageSVC productImageSVC;

  public ProductImageController(ProductImageSVC productImageService, ProductSVC productSVC) {
    this.productSVC = productSVC;
    this.productImageSVC = productImageService;
  }

  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductImage>> getImagesByProduct(@PathVariable Long productId) {
    List<ProductImage> images = productImageSVC.getImagesByProductId(productId);
    return ResponseEntity.ok(images);
  }

  @PostMapping
  public ResponseEntity<String> addImage(@RequestBody ProductImage productImage) {
    boolean success = productImageSVC.addProductImage(productImage);
    if (success) return ResponseEntity.ok("이미지 등록 성공");
    else return ResponseEntity.status(500).body("이미지 등록 실패");
  }

  @DeleteMapping("/{imageId}")
  public ResponseEntity<String> deleteImage(@PathVariable Long imageId) {
    boolean success = productImageSVC.deleteProductImage(imageId);
    if (success) return ResponseEntity.ok("이미지 삭제 성공");
    else return ResponseEntity.status(500).body("이미지 삭제 실패");
  }

  @GetMapping("/{imageId}/data")
  public ResponseEntity<byte[]> getImageData(@PathVariable Long imageId) {
    return productImageSVC.findById(imageId)
        .map(img -> {
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.parseMediaType(img.getMimeType()));
          headers.setContentLength(img.getFileSize());
          return new ResponseEntity<>(img.getImageData(), headers, HttpStatus.OK);
        })
        .orElse(ResponseEntity.notFound().build());
  }
}
