package com.KDT.mosi.domain.review.svc;

import com.KDT.mosi.domain.entity.review.Review;
import com.KDT.mosi.domain.entity.review.ReviewInfo;
import com.KDT.mosi.domain.entity.review.ReviewList;
import com.KDT.mosi.domain.entity.review.ReviewProduct;
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

}
