package com.KDT.mosi.web.form.product;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductCoursePoint;
import com.KDT.mosi.domain.entity.ProductImage;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductUpdateForm {

  private Product product;
  private List<ProductImage> existingImages; // 기존 이미지 (DB에서 조회)
  private List<MultipartFile> uploadImages;  // 새로 업로드할 이미지 (폼에서 업로드)
  private List<ProductCoursePoint> coursePoints;
  private String nickname;
  private String intro;
  private String sellerImage;

  // 파일 첨부
  private MultipartFile documentFile;

  // 이미지 삭제용 ID 리스트
  private List<Long> deleteImageIds;

  // 코스포인트 삭제용 ID 리스트
  private List<Long> deleteCoursePointIds;
}