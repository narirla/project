package com.KDT.mosi.domain.review.svc;

import com.KDT.mosi.domain.common.svc.CodeSVC;
import com.KDT.mosi.domain.dto.CodeDTO;
import com.KDT.mosi.domain.entity.review.*;
import com.KDT.mosi.domain.review.dao.ReviewDAO;
import com.KDT.mosi.web.form.review.TagInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSVCImpl implements ReviewSVC{
  private final ReviewDAO reviewDAO;
  private final CodeSVC codeSVC;

  private static final Map<String, String> KEY_TO_CODE = Map.of(
      "area",             "B0101",
      "pet",              "B0102",
      "restaurant",       "B0103",
      "culture_history",  "B0104",
      "season_nature",    "B0105",
      "silver_disables",  "B0106"
  );

  @Override
  public boolean reviewCheck(Long reviewId, Long loginId) {
    Long id = findBuyerIdByReviewId(reviewId)
        .orElseThrow(() -> new AccessDeniedException("리뷰가 없거나 접근 불가"));

    if (!Objects.equals(id, loginId)) {
      throw new AccessDeniedException("본인 리뷰가 아닙니다.");
    }
    log.info("리뷰 수정 가능1");
    return true;
  }

  @Override
  public Optional<ReviewInfo> orderCheck(Long orderItemId, Long loginId) {
    ReviewInfo reviewInfo = findBuyerIdByOrderItemId(orderItemId)
        .orElseThrow(() -> new AccessDeniedException("주문 아이템이 없거나 접근 불가"));

    if (!Objects.equals(reviewInfo.getBuyerId(), loginId)) {
      throw new AccessDeniedException("본인 주문이 아닙니다.");
    }
    log.info("Reviewed={}",reviewInfo.getReviewed());
    if (!"N".equals(reviewInfo.getReviewed())) {
      throw new AccessDeniedException("이미 작성한 리뷰 입니다.");
    }
    return Optional.of(reviewInfo);
  }

  @Override
  public Optional<ReviewProduct> summaryFindById(Long orderItemId, Long loginId) {
    ReviewInfo reviewInfo = orderCheck(orderItemId, loginId)
        .orElseThrow(() -> new AccessDeniedException("주문 검증 실패"));

    return reviewDAO.summaryFindById(orderItemId)
        .map(rp -> { rp.setOptionType(reviewInfo.getOptionType()); return rp; });
  }

  @Override
  public Optional<ReviewProduct> summaryFindByProductId(Long productId) {
    return reviewDAO.summaryFindById(productId);
  }

  @Override
  public Optional<ReviewInfo> findBuyerIdByOrderItemId(Long id) {

    return reviewDAO.findBuyerIdByOrderItemId(id);
  }

  @Override
  public List<TagInfo> findTagList(String category) {
    if (!categoryFind(category)) {
      return List.of(); // 카테고리가 없으면 빈 리스트
    }
    String in = category.trim();
    String codeId = KEY_TO_CODE.getOrDefault(in.toLowerCase(), in.toUpperCase());

    return reviewDAO.findTagList(codeId);
  }

  @Override
  public boolean categoryFind(String category) {
    if (category == null || category.isBlank()) return false;


    String in = category.trim();
    String codeId = KEY_TO_CODE.getOrDefault(in.toLowerCase(), in.toUpperCase());

    List<CodeDTO> list = codeSVC.getB01();

    return list != null && list.stream()
        .anyMatch(c -> codeId.equalsIgnoreCase(c.getCodeId()));
  }

  @Override
  public Optional<Long> findBuyerIdByReviewId(Long id) {
    return reviewDAO.findBuyerIdByReviewId(id);
  }

  @Override
  @Transactional
  public Long reviewSave(List<Long> ids, Review review) {
    // 1) 주문 검증 + productId 세팅
    ReviewInfo reviewInfo = orderCheck(review.getOrderItemId(), review.getBuyerId())
        .orElseThrow(() -> new AccessDeniedException("주문 검증 실패"));
    review.setProductId(reviewInfo.getProductId()); // ✅ 필수

    // 2) 카테고리 결정(서버)
    String category = this.findCategory(reviewInfo.getProductId())
        .orElseThrow(() -> new IllegalStateException("상품 카테고리를 찾을 수 없습니다."));

    // 3) 태그 정규화(0/null/음수 제거, 중복 제거, 순서 보존)
    List<Long> normIds =
        (ids == null ? java.util.Collections.<Long>emptyList() : ids).stream()
            .filter(java.util.Objects::nonNull)
            .map(Long::longValue)
            .filter(id -> id > 0)     // ← 0, 음수는 "선택 안 함"으로 간주
            .distinct()               // 중복 제거(클릭 순서 보존됨)
            .collect(java.util.stream.Collectors.toList());

    // 3-1) 허용 태그 검증(실제 값이 있을 때만)
    if (!normIds.isEmpty()) {
      java.util.Set<Long> allowedIds = this.findTagList(category).stream()
          .map(TagInfo::getTagId)
          .collect(java.util.stream.Collectors.toSet());

      for (Long id : normIds) {
        if (!allowedIds.contains(id)) {
          throw new IllegalArgumentException("허용되지 않는 태그입니다. tagId=" + id);
        }
      }
    }

    // 4) 리뷰 저장
    Long reviewId = reviewDAO.saveReview(review);

    // 5) 리뷰-태그 매핑 저장 (없으면 스킵)
    long sortOrder = 1;
    for (Long tagId : normIds) {
      ReviewTag rt = new ReviewTag();
      rt.setReviewId(reviewId);
      rt.setTagId(tagId);
      rt.setSortOrder(sortOrder++);
      reviewDAO.saveReviewTag(rt);
    }

    // 6) ORDER_ITEMS.REVIEWED = 'Y'
    int updated = reviewDAO.updateReviewed(review.getOrderItemId());
    if (updated != 1) {
      throw new IllegalStateException("ORDER_ITEMS 업데이트 실패 (updated=" + updated + ")");
    }

    return reviewId;
  }



  @Override
  public Optional<String> findCategory(Long productId) {
    return reviewDAO.findCategory(productId);
  }

  @Override
  public List<ReviewList> reviewFindAll(Long buyerId, int pageNo, int numOfRows) {
    return reviewDAO.reviewFindAll(buyerId, pageNo, numOfRows);
  }

  @Override
  public List<ReviewList> reviewFindAllSeller(Long sellerId, int pageNo, int numOfRows) {
    return reviewDAO.reviewFindAllSeller(sellerId, pageNo, numOfRows);
  }

  @Override
  public Long getReviewTotalCount(Long buyerId) {
    return reviewDAO.getReviewTotalCount(buyerId);
  }

  @Override
  public Long getSellerReviewTotalCount(Long memberId) {
    return reviewDAO.getSellerReviewTotalCount(memberId);
  }

  @Override
  public int deleteByIds(Long reviewId, Long loginId) {
    this.reviewCheck(reviewId,loginId);
    reviewDAO.updateReviewWrite(reviewId);
    int cnt = reviewDAO.deleteByIds(reviewId);      // 실삭제
    if (cnt == 0) throw new IllegalStateException("이미 삭제되었거나 존재하지 않습니다.");
    return cnt;
  }

  @Override
  public boolean updateReviewWrite(Long reviewId) {
    return reviewDAO.updateReviewWrite(reviewId);
  }

  @Override
  public Optional<ReviewEdit> findReviewId(Long reviewId, Long loginId) {
    this.reviewCheck(reviewId,loginId);
    return reviewDAO.findReviewId(reviewId);
  }

  @Transactional
  @Override
  public Long reviewEditUpdate(List<Long> ids, Review review) {
    // 1) 소유/권한 체크
    this.reviewCheck(review.getReviewId(), review.getBuyerId());

    // 2) 카테고리 결정(리뷰 → 상품 → 카테고리)
    String category = this.findReviewId(review.getReviewId(), review.getBuyerId())
        .map(ReviewEdit::getProductId)
        .flatMap(this::findCategory)
        .orElseThrow(() -> new IllegalStateException("리뷰 또는 카테고리를 찾을 수 없습니다."));

    // 3) 허용 태그
    List<TagInfo> allowedTags = this.findTagList(category);
    Set<Long> allowedIds = allowedTags.stream()
        .map(TagInfo::getTagId)
        .collect(Collectors.toSet());

    // 4) 입력 태그 정제: null/0/음수 제거, 중복 제거(선택)
    List<Long> safeIds = (ids == null) ? Collections.emptyList() :
        ids.stream()
            .filter(Objects::nonNull)
            .map(Long::longValue)
            .filter(x -> x > 0)       // ★ 0(미선택) 제외
            .distinct()               // ★ 중복 방지(원치 않으면 제거)
            .collect(Collectors.toList());

    // 5) 검증(정제된 것만)
    for (Long id : safeIds) {
      if (!allowedIds.contains(id)) {
        throw new IllegalArgumentException("허용되지 않는 태그입니다. tagId=" + id);
      }
    }

    // 6) 저장(비어있으면 태그 전부 제거/미저장은 DAO 구현에 따름)
    return reviewDAO.reviewEditUpdate(
        review.getReviewId(),
        review.getScore(),
        safeIds,                    // ★ 정제 리스트 전달
        review.getContent()
    );
  }


  @Override
  public boolean reviewReport(Long reviewId, Long memberId) {
    return reviewDAO.reviewReport(reviewId, memberId);
  }

  @Override
  public int saveReport(Long reviewId, Long memberId, String reason) {
    return reviewDAO.saveReport(reviewId, memberId, reason);
  }

  @Override
  public boolean existsReport(Long reviewId, Long memberId) {
    return reviewDAO.existsReport(reviewId,memberId);
  }

  @Override
  public List<ProductReview> productReviewList(Long productId, int pageNo, int pageSiz) {
    return reviewDAO.productReviewList(productId, pageNo, pageSiz);
  }

  @Override
  public List<ProductReview> productReviewList(Long productId, int pageNo, int pageSize, Long loginId) {


    return reviewDAO.productReviewListId(productId, pageNo, pageSize,loginId);
  }

  @Override
  public Optional<ReviewProduct> reviewProfile(Long memberId) {
    return reviewDAO.reviewProfile(memberId);
  }

  @Override
  public Long productReviewCnt(Long productId) {
    return reviewDAO.productReviewCnt(productId);
  }


}
