package com.KDT.mosi.domain.review.dao;

import com.KDT.mosi.domain.entity.review.*;
import com.KDT.mosi.web.form.review.TagInfo;

import java.util.List;
import java.util.Optional;

public interface ReviewDAO {
  //상품 요약 정보 확인
  Optional<ReviewProduct> summaryFindById(Long id);

  //상품 구매자 확인
  Optional<ReviewInfo> findBuyerIdByOrderItemId(Long id);

  //태그 반환
  List<TagInfo> findTagList(String category);

  //리뷰 작성자 확인
  Optional<Long> findBuyerIdByReviewId(Long id);

  //리뷰 저장
  Long saveReview(Review review);

  //리뷰 태그 저장
  int saveReviewTag(ReviewTag reviewTag);

  //리뷰 태그 확인
  boolean findTagId(Long id,String category);

  //리뷰 작성
  int updateReviewed(Long orderItemId);

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
  int deleteByIds(Long id);
}
