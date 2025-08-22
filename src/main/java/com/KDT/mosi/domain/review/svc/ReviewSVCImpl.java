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

    return reviewDAO.summaryFindById(reviewInfo.getProductId())
        .map(rp -> { rp.setOptionType(reviewInfo.getOptionType()); return rp; });
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
    String category = this.findCategory(review.getOrderItemId())
        .orElseThrow(() -> new IllegalStateException("상품 카테고리를 찾을 수 없습니다."));

    // 3) 허용 태그 조회 & 검증
    List<TagInfo> allowedTags = this.findTagList(category);
    Set<Long> allowedIds = allowedTags.stream().map(TagInfo::getTagId).collect(Collectors.toSet());

    List<Long> safeIds = (ids == null) ? Collections.emptyList() : ids;
    for (Long id : safeIds) {
      if (!allowedIds.contains(id)) {
        throw new IllegalArgumentException("허용되지 않는 태그입니다. tagId=" + id);
      }
    }

    // 4) 리뷰 저장
    Long reviewId = reviewDAO.saveReview(review);

    // 5) 리뷰-태그 매핑 저장
    long sortOrder = 1;
    for (Long tagId : safeIds) {
      ReviewTag rt = new ReviewTag();
      rt.setReviewId(reviewId);
      rt.setTagId(tagId);
      rt.setSortOrder(sortOrder++);
      reviewDAO.saveReviewTag(rt);
    }

    // 6) ORDER_ITEMS.REVIEWED='Y'
    int updated = reviewDAO.updateReviewed(review.getOrderItemId());
    if (updated != 1) {
      throw new IllegalStateException("ORDER_ITEMS 업데이트 실패 (updated=" + updated + ")");
    }

    return reviewId;
  }


  @Override
  public Optional<String> findCategory(Long orderItemId) {
    return reviewDAO.findCategory(orderItemId);
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
  public int deleteByIds(Long id, Long loginId) {
    this.reviewCheck(id,loginId);
    int cnt = reviewDAO.deleteByIds(id);      // 실삭제
    if (cnt == 0) throw new IllegalStateException("이미 삭제되었거나 존재하지 않습니다.");
    return cnt;
  }
}
