package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductCoursePoint;
import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductCoursePointSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.form.product.FilteredProductsDTO;
import com.KDT.mosi.web.form.product.PaginationInfo;
import com.KDT.mosi.web.form.product.ProductManagingForm;
import com.KDT.mosi.web.form.product.ProductValidationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

  private final ProductSVC productSVC;
  private final ProductImageSVC productImageSVC;
  private final ProductCoursePointSVC productCoursePointSVC;
  private final SellerPageSVC sellerPageSVC;

  @Autowired
  public ProductApiController(ProductSVC productSVC, ProductImageSVC productImageSVC, ProductCoursePointSVC productCoursePointSVC, SellerPageSVC sellerPageSVC) {
    this.productSVC = productSVC;
    this.productImageSVC = productImageSVC;
    this.productCoursePointSVC = productCoursePointSVC;
    this.sellerPageSVC = sellerPageSVC;
  }

  /**
   * 상품 폼 데이터 유효성 검사 API (기존 코드)
   */
  @PostMapping("/validate")
  public ResponseEntity<?> validateProductForm(@Valid @RequestBody ProductValidationForm form, BindingResult bindingResult) {
    // 커스텀 유효성 검사 로직 (예: 판매 가격이 정상 가격보다 큰지 확인)
    if (form.getNormalPrice() != null && form.getSalesPrice() != null) {
      if (form.getSalesPrice() > form.getNormalPrice()) {
        bindingResult.addError(new FieldError("form", "salesPrice", "판매 가격은 정상 가격보다 작아야 합니다."));
      }
    }

    if (form.getGuidePrice() != null && form.getSalesGuidePrice() != null) {
      if (form.getSalesGuidePrice() > form.getGuidePrice()) {
        bindingResult.addError(new FieldError("form", "salesGuidePrice", "가이드 동반 판매 가격은 동반 가격보다 작아야 합니다."));
      }
    }

    // 소요 기간 또는 시간 유효성 검사 (두 값 모두 0인지)
    if (form.getTotalDay() != null && form.getTotalTime() != null) {
      if (form.getTotalDay() == 0 && form.getTotalTime() == 0) {
        bindingResult.addError(new FieldError("form", "totalDay", "소요 기간 또는 시간을 올바르게 입력해주세요."));
      }
    }

    if (bindingResult.hasErrors()) {
      Map<String, String> errors = new HashMap<>();
      for (FieldError error : bindingResult.getFieldErrors()) {
        errors.put(error.getField(), error.getDefaultMessage());
      }
      return ResponseEntity.badRequest().body(errors);
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 비동기 상품 목록 필터링 및 페이징 API
   */
  @GetMapping("/manage/filtered-data")
  public ResponseEntity<?> getFilteredProducts(HttpSession session,
                                               @RequestParam(name = "page", defaultValue = "1") int page,
                                               @RequestParam(name = "size", defaultValue = "5") int size,
                                               @RequestParam(name = "status", defaultValue = "all") String status) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body("로그인한 회원이 아닙니다.");
    }
    Long memberId = loginMember.getMemberId();

    List<Product> products;
    long totalCount;

    if ("all".equals(status)) {
      products = productSVC.getProductsByMemberIdAndPage(memberId, page, size);
      totalCount = productSVC.countByMemberId(memberId);
    } else if ("판매중".equals(status) || "판매대기".equals(status)) {
      products = productSVC.getProductsByMemberIdAndStatusAndPage(memberId, status, page, size);
      totalCount = productSVC.countByMemberIdAndStatus(memberId, status);
    } else {
      status = "all";
      products = productSVC.getProductsByMemberIdAndPage(memberId, page, size);
      totalCount = productSVC.countByMemberId(memberId);
    }

    List<ProductManagingForm> managingForms = new ArrayList<>();
    for (Product product : products) {
      List<ProductImage> images = productImageSVC.findByProductId(product.getProductId());
      List<ProductCoursePoint> coursePoints = productCoursePointSVC.findByProductId(product.getProductId());

      double discountAmount = (double) product.getNormalPrice() - (double) product.getSalesPrice();
      double salePercent = (discountAmount / product.getNormalPrice()) * 100;
      long salesRate = Math.round(salePercent);
      if (salesRate < 0) {
        salesRate = 0;
      }

      ProductManagingForm form = new ProductManagingForm();
      form.setProduct(product);
      form.setImages(images);
      form.setCoursePoints(coursePoints);
      form.setSalesRate(salesRate);

      managingForms.add(form);
    }

    int currentPage = page;
    int totalPages = (int) Math.ceil((double) totalCount / size);
    int displayPageNum = 10;
    int endPage = (int) (Math.ceil(currentPage / (double) displayPageNum) * displayPageNum);
    int startPage = endPage - displayPageNum + 1;

    if (startPage < 1) {
      startPage = 1;
    }
    if (endPage > totalPages) {
      endPage = totalPages;
    }

    PaginationInfo paginationInfo = new PaginationInfo();
    paginationInfo.setCurrentPage(currentPage);
    paginationInfo.setTotalPages(totalPages);
    paginationInfo.setStartPage(startPage);
    paginationInfo.setEndPage(endPage);
    paginationInfo.setTotalCount(totalCount);
    paginationInfo.setSelectedStatus(status);

    FilteredProductsDTO responseDto = new FilteredProductsDTO();
    responseDto.setContent(managingForms);
    responseDto.setTotalCount(totalCount);
    responseDto.setPagination(paginationInfo);

    return ResponseEntity.ok(responseDto);
  }
}