package com.KDT.mosi.domain.mypage.seller.svc;

import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 판매자 마이페이지 서비스 구현체
 * - DAO를 통해 실제 데이터베이스 연산 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용
public class SellerPageSVCImpl implements SellerPageSVC {

  private final SellerPageDAO sellerPageDAO;

  /**
   * 회원 ID로 마이페이지 존재 여부 확인
   */
  @Override
  public boolean existByMemberId(Long memberId) {
    return sellerPageDAO.existByMemberId(memberId);
  }

  /**
   * 판매자 마이페이지 등록
   */
  @Override
  @Transactional(/* rollbackFor = Exception.class */) // 쓰기 트랜잭션
  public Long save(SellerPage sellerpage) {
    if (sellerpage.getMemberId() == null) {
      throw new IllegalArgumentException("memberId must not be null");
    }
    return sellerPageDAO.save(sellerpage);
  }

  /**
   * 회원 ID로 판매자 마이페이지 조회
   */
  @Override
  public Optional<SellerPage> findByMemberId(Long memberId) {
    return sellerPageDAO.findByMemberId(memberId);
  }

  /**
   * 페이지 ID로 판매자 마이페이지 조회
   */
  @Override
  public Optional<SellerPage> findById(Long pageId) {
    return sellerPageDAO.findById(pageId);
  }

  /**
   * 판매자 마이페이지 수정
   */
  @Override
  @Transactional(/* rollbackFor = Exception.class */) // 쓰기 트랜잭션
  public int updateById(Long pageId, SellerPage sellerpage) {
    return sellerPageDAO.updateById(pageId, sellerpage);
  }

  /**
   * 회원 ID로 판매자 마이페이지 삭제
   */
  @Override
  @Transactional(/* rollbackFor = Exception.class */) // 쓰기 트랜잭션
  public int deleteByMemberId(Long memberId) {
    return sellerPageDAO.deleteByMemberId(memberId);
  }

  /**
   * 닉네임 중복 여부 확인
   */
  @Override
  public boolean existByNickname(String nickname) {
    return sellerPageDAO.existByNickname(nickname);
  }

  @Override
  public Optional<String> getNicknameByMemberId(Long memberId) {
    // SellerPageDAO에 findNicknameByMemberId 메서드를 호출
    return sellerPageDAO.findNicknameByMemberId(memberId);
  }
}
