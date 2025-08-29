package com.KDT.mosi.web.controller.review;

import com.KDT.mosi.domain.entity.review.*;
import com.KDT.mosi.domain.product.dao.ProductImageDAO;
import com.KDT.mosi.domain.review.svc.ReviewSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import com.KDT.mosi.web.form.review.ReviewSaveApi;
import com.KDT.mosi.web.form.review.ReviewUpdateApi;
import com.KDT.mosi.web.form.review.TagInfo;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Slf4j
@RequestMapping("/api/review")
@RestController
@RequiredArgsConstructor
public class ApiReviewController {

  private final ReviewSVC reviewSVC;
  private final ProductImageDAO productImageDAO;

  //리뷰 추가
  @PostMapping
  public ResponseEntity<ApiResponse<Review>> add(
      @RequestBody @Valid ReviewSaveApi reviewSaveApi,
      HttpSession session
  ) {
    // 1) 로그인 사용자 id 세션에서 꺼내기
    Long loginId = (Long) session.getAttribute("loginMemberId");
    // 2) Review 엔티티 변환
    Review review = new Review();
    review.setOrderItemId(reviewSaveApi.getOrderItemId());
    review.setBuyerId(loginId);
    review.setScore(reviewSaveApi.getScore());
    review.setContent(reviewSaveApi.getContent());

    // 3) 저장 (id 반환)
    Long reviewId = reviewSVC.reviewSave(reviewSaveApi.getTagIds(), review);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.of(ApiResponseCode.SUCCESS, null));
  }

  // 리뷰 수정
  @PostMapping("/update")
  public ResponseEntity<ApiResponse<Void>> updateReview(
      @RequestBody @Valid ReviewUpdateApi reviewUpdateApi,
      HttpSession session
  ) {
    // 1) 로그인 사용자 id
    Long loginId = (Long) session.getAttribute("loginMemberId");

    // 2) Review 엔티티 변환
    Review review = new Review();
    review.setReviewId(reviewUpdateApi.getReviewId());
    review.setBuyerId(loginId);
    review.setScore(reviewUpdateApi.getScore());
    review.setContent(reviewUpdateApi.getContent());

    // 3) 수정 저장 (태그 재저장 포함)
    reviewSVC.reviewEditUpdate(reviewUpdateApi.getTagIds(), review);

    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
  }

  @GetMapping("/paging/buyer")
  public ResponseEntity<ApiResponse<List<ReviewList>>> list(
      @RequestParam(value="pageNo", defaultValue = "1") Integer pageNo,
      @RequestParam(value="numOfRows", defaultValue = "5") Integer numOfRows,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    List<ReviewList> items = reviewSVC.reviewFindAll(loginId, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, items));
  }

  @GetMapping("/paging/seller")
  public ResponseEntity<ApiResponse<List<ReviewList>>> sellerList(
      @RequestParam(value="pageNo", defaultValue = "1") Integer pageNo,
      @RequestParam(value="numOfRows", defaultValue = "5") Integer numOfRows,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    List<ReviewList> items = reviewSVC.reviewFindAllSeller(loginId, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, items));
  }

  // 태그 반환: 공용 + 해당 카테고리 (카테고리가 없으면 빈 배열 [])
  @GetMapping("/tag/{category}")
//  @GetMapping(value="/tag/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse<List<TagInfo>>> getTags(
      @PathVariable("category") String category
  ) {
    List<TagInfo> tags = reviewSVC.findTagList(category);
    log.info("category={}",category);
    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, tags));
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Integer>> deleteById(
      @PathVariable("id") Long id,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    reviewSVC.deleteByIds(id,loginId);

    ApiResponse<Integer> body = ApiResponse.of(ApiResponseCode.SUCCESS, null);
    return ResponseEntity.ok(body);
  }

  //구매자 전체 건수 가져오기
  @GetMapping("/buyer/totCnt")
  public ResponseEntity<ApiResponse<Long>> buyerTotalCount(HttpSession session) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    Long totalCount = reviewSVC.getReviewTotalCount(loginId);
    ApiResponse<Long> bbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);

    return ResponseEntity.ok(bbsApiResponse);
  }

  //구매자 전체 건수 가져오기
  @GetMapping("/seller/totCnt")
  public ResponseEntity<ApiResponse<Long>> sellerTotalCount(HttpSession session) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    Long totalCount = reviewSVC.getSellerReviewTotalCount(loginId);
    ApiResponse<Long> bbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);

    return ResponseEntity.ok(bbsApiResponse);
  }

  @GetMapping("/product-images/{imageId}")
  public ResponseEntity<byte[]> image(@PathVariable("imageId") Long imageId){
    return productImageDAO.findById(imageId)
        .map(img -> ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(img.getMimeType()))
            .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
            .body(img.getImageData())   // ← 여기!
        )
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{reviewId}/report")
  public ResponseEntity<ApiResponse<Void>> report(
      @PathVariable("reviewId") Long reviewId,
      @RequestBody ReviewReport reviewReport,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    if (loginId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    // PathVariable·세션 값 덮어쓰기
    reviewReport.setReviewId(reviewId);
    reviewReport.setMemberId(loginId);

    reviewSVC.saveReport(
        reviewReport.getReviewId(),
        reviewReport.getMemberId(),
        reviewReport.getReason()
    );

    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
  }
  @GetMapping(value="/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
//  @GetMapping("/product/{productId}")
  public ResponseEntity<ApiResponse<List<ProductReview>>> productReviewList(
      @RequestParam(value="pageNo", defaultValue = "1") Integer pageNo,
      @RequestParam(value="numOfRows", defaultValue = "5") Integer pageSize,
      @PathVariable("productId") Long productId,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    if (loginId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    List<ProductReview> productReviews = reviewSVC.productReviewList(productId, pageNo, 5,loginId);

    return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, productReviews));
  }
  @GetMapping(value="/product/profile-images/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
//  @GetMapping("/product/profile-images/{imageId}")
  public ResponseEntity<ApiResponse<ReviewProduct>> profileImage(
      @PathVariable("imageId") Long imageId,
      HttpSession session
  ) {
    Long loginId = (Long) session.getAttribute("loginMemberId");
    if (loginId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    ReviewProduct rp = reviewSVC.reviewProfile(imageId)
        .orElse(null);

    if (rp == null) {
      return ResponseEntity.notFound().build();
    }

    ApiResponse<ReviewProduct> response =
        ApiResponse.of(ApiResponseCode.SUCCESS, rp);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/product/reviewCnt/{productId}")
  public ResponseEntity<ApiResponse<Long>> productReviewCnt(
      @PathVariable("productId") Long productId,
      HttpSession session
  ){
    Long loginId = (Long) session.getAttribute("loginMemberId");
    if (loginId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    Long num = reviewSVC.productReviewCnt(productId);
    ApiResponse<Long> reviewApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, num);
    return ResponseEntity.ok(reviewApiResponse);
  }

}
