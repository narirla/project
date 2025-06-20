package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.svc.SellerSVC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/seller-page")
public class SellerPageController {

  private final SellerSVC sellerPageService;

  public SellerPageController(SellerSVC sellerSVC) {
    this.sellerPageService = sellerSVC;
  }

  @PostMapping("/create")
  public ResponseEntity<String> createPage(
      @RequestParam Long memberId,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(required = false) String intro
  ) throws IOException {
    SellerPage page = new SellerPage();
    page.setMemberId(memberId);
    page.setIntro(intro);
    page.setImage(image != null ? image.getBytes() : null);
    sellerPageService.createSellerPage(page);
    return ResponseEntity.ok("판매자 페이지 생성 완료");
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<SellerPage> getPage(@PathVariable Long memberId) {
    return sellerPageService.getSellerPageByMemberId(memberId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/update/{pageId}")
  public ResponseEntity<String> updatePage(
      @PathVariable Long pageId,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(required = false) String intro,
      @RequestParam int salesCount,
      @RequestParam double reviewAvg
  ) throws IOException {
    SellerPage page = new SellerPage();
    page.setPageId(pageId);
    page.setImage(image != null ? image.getBytes() : null);
    page.setIntro(intro);
    page.setSalesCount(salesCount);
    page.setReviewAvg(reviewAvg);

    sellerPageService.updateSellerPage(page);
    return ResponseEntity.ok("판매자 페이지 수정 완료");
  }

  @DeleteMapping("/delete/{pageId}")
  public ResponseEntity<String> deletePage(@PathVariable Long pageId) {
    sellerPageService.deleteSellerPage(pageId);
    return ResponseEntity.ok("삭제 완료");
  }
}
