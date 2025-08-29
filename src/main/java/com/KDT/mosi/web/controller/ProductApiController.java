package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.form.product.FilteredProductsDTO;
import com.KDT.mosi.web.form.product.PaginationInfo;
import com.KDT.mosi.web.form.product.ProductManagingForm;
import com.KDT.mosi.web.form.product.ProductUpdateForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

  @Autowired
  private ProductSVC productSVC;
  @Autowired
  private ProductImageSVC productImageSVC;
  @Autowired
  private SellerPageSVC sellerPageSVC;

  // 상품 유효성 검사 (multipart/form-data 요청 처리)
  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validate(@Valid @ModelAttribute ProductUpdateForm productUpdateForm, BindingResult bindingResult) {
    Map<String, Object> response = new HashMap<>();
    if (bindingResult.hasErrors()) {
      Map<String, String> errors = new HashMap<>();
      for (FieldError error : bindingResult.getFieldErrors()) {
        errors.put(error.getField(), error.getDefaultMessage());
      }
      response.put("errors", errors);
      return ResponseEntity.status(400).body(response);
    }
    response.put("success", true);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductManagingForm> getProduct(@PathVariable("id") Long id) {
    Product product = productSVC.getProduct(id).orElse(null);
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    List<ProductImage> images = productImageSVC.getImagesByProductId(id);

    long discountAmount = product.getNormalPrice() - product.getSalesPrice();
    double salePercent = (discountAmount / (double) product.getNormalPrice()) * 100;
    long salesRate = Math.round(salePercent);
    if (salesRate < 0) {
      salesRate = 0;
    }

    ProductManagingForm form = new ProductManagingForm();
    form.setProduct(product);
    form.setImages(images);
    form.setSalesRate(salesRate);

    return ResponseEntity.ok(form);
  }

  @GetMapping("/list")
  public ResponseEntity<FilteredProductsDTO> getProductList(
      @RequestParam(value = "status", defaultValue = "판매중") String status,
      @RequestParam(value = "searchType", defaultValue = "TITLE") String searchType,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "sort", defaultValue = "SALES_DESC") String sort,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      HttpSession session
  ) {
    Member member = (Member) session.getAttribute("loginMember");
    if (member == null) {
      return ResponseEntity.status(401).body(null);
    }
    Long memberId = member.getMemberId();
    List<Product> products;
    long totalCount;

    // ⭐⭐ 핵심 수정 부분: 'all' 상태 필터링 처리
    if ("all".equals(status)) {
      products = productSVC.getProductsByMemberIdAndPage(memberId, page, size);
      totalCount = productSVC.countByMemberId(memberId);
    } else {
      products = productSVC.getProductsByMemberIdAndStatusAndPage(memberId, status, page, size);
      totalCount = productSVC.countByMemberIdAndStatus(memberId, status);
    }

    List<ProductManagingForm> managingForms = new ArrayList<>();
    for (Product product : products) {
      List<ProductImage> images = productImageSVC.getImagesByProductId(product.getProductId());

      long discountAmount = product.getNormalPrice() - product.getSalesPrice();
      double salePercent = (discountAmount / (double) product.getNormalPrice()) * 100;
      long salesRate = Math.round(salePercent);
      if (salesRate < 0) {
        salesRate = 0;
      }

      ProductManagingForm form = new ProductManagingForm();
      form.setProduct(product);
      form.setImages(images);
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

    FilteredProductsDTO response = new FilteredProductsDTO();
    response.setContent(managingForms);
    response.setPagination(paginationInfo);
    response.setTotalCount(totalCount);

    return ResponseEntity.ok(response);
  }
}