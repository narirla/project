package com.KDT.mosi.domain.review.svc;

import com.KDT.mosi.domain.entity.review.*;
import com.KDT.mosi.web.form.review.TagInfo;

import java.util.List;
import java.util.Optional;

public interface ReviewSVC {

  //작성 확인
  boolean reviewCheck(Long reviewId, Long loginId);

  //구매 확인
  Optional<ReviewInfo> orderCheck(Long orderItemId, Long loginId);

  //상품 요약 정보 확인
  Optional<ReviewProduct> summaryFindById(Long orderItemId, Long loginId);

  //수정용 상품 요약 정보 확인
  Optional<ReviewProduct> summaryFindByProductId(Long productId);

  //상품 구매자 확인
  Optional<ReviewInfo> findBuyerIdByOrderItemId(Long id);

  //태그 반환
  List<TagInfo> findTagList(String category);

  //태그 카테고리 확인
  boolean categoryFind(String category);

  //리뷰 작성자 확인
  Optional<Long> findBuyerIdByReviewId(Long id);

  //리뷰 저장
  Long reviewSave(List<Long> ids, Review review);

  //product category 확인
  Optional<String> findCategory(Long orderItemId);

  //구매자 리뷰 목록
  List<ReviewList> reviewFindAll(Long buyerId, int pageNo, int numOfRows);

  //판매자 리뷰 목록
  List<ReviewList> reviewFindAllSeller(Long sellerId, int pageNo, int numOfRows);

  //구매자 리뷰 전체 갯수
  Long getReviewTotalCount(Long buyerId);

  //판매자 리뷰 전체 갯수
  Long getSellerReviewTotalCount(Long memberId);

  //리뷰 삭제
  public int deleteByIds(Long id, Long loginId);

  //리뷰 다시 작성 가능
  boolean updateReviewWrite(Long reviewId);

  //리뷰 수정 정보
  Optional<ReviewEdit> findReviewId (Long id, Long loginId);

  //리뷰 수정 저장
  Long reviewEditUpdate(List<Long> ids, Review review);


  //리뷰 신고
  boolean reviewReport(Long reviewId, Long memberId);

  //리뷰 신고 저장
  int saveReport(Long reviewId, Long memberId, String reason);

  //리뷰 신고 확인
  boolean existsReport(Long reviewId, Long memberId);

  //제품 상세 페이지 리뷰 목록
  List<ProductReview> productReviewList(Long productId, int pageNo,int pageSiz);

  //제품 상세 페이지 리뷰 전체 갯수
  Long productReviewCnt(Long productId);

  //제품 상세 페이지 리뷰 목록(작성자 본인 닉네임은 전체 출력)
  List<ProductReview> productReviewList(Long productId, int pageNo, int pageSize,Long loginId);

  //리뷰 작성자 profile
  Optional<ReviewProduct> reviewProfile(Long memberId);
}
