package com.KDT.mosi.web.controller.review;

import com.KDT.mosi.domain.entity.review.Review;
import com.KDT.mosi.domain.entity.review.ReviewList;
import com.KDT.mosi.domain.review.svc.ReviewSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import com.KDT.mosi.web.form.review.ReviewSaveApi;
import com.KDT.mosi.web.form.review.TagInfo;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/review")
@RestController
@RequiredArgsConstructor
public class ApiReviewController {

  private final ReviewSVC reviewSVC;

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
  public ResponseEntity<ApiResponse<List<TagInfo>>> getTags(
      @PathVariable("category") String category
  ) {
    List<TagInfo> tags = reviewSVC.findTagList(category);
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

}
