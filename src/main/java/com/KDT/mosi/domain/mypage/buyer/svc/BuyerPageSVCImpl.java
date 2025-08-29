package com.KDT.mosi.domain.mypage.buyer.svc;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BuyerPageSVCImpl implements BuyerPageSVC {

  private final BuyerPageDAO buyerPageDAO;
  private final MemberDAO memberDAO;

  /**
   * 마이페이지 등록
   *
   * @param buyerPage 등록할 구매자 마이페이지 정보
   * @return 생성된 페이지 ID
   */
  @Override
  public Long create(BuyerPage buyerPage) {
    return buyerPageDAO.save(buyerPage);
  }

  /**
   * 회원 ID로 마이페이지 조회
   *
   * @param memberId 조회할 회원의 ID
   * @return 해당 회원의 마이페이지 정보(Optional)
   */
  @Override
  public Optional<BuyerPage> findByMemberId(Long memberId) {
    return buyerPageDAO.findByMemberId(memberId);
  }

  /**
   * 마이페이지 정보 수정
   *
   * @param pageId    수정할 마이페이지 ID
   * @param buyerPage 수정할 내용
   * @return 수정된 행 수
   */
  @Override
  public int update(Long pageId, BuyerPage buyerPage) {
    return buyerPageDAO.updateById(pageId, buyerPage);
  }

  /**
   * 마이페이지 삭제 (회원 ID 기준)
   * - 회원 탈퇴 시 연관된 마이페이지 제거용
   *
   * @param memberId 삭제 대상 회원 ID
   * @return 삭제된 행 수
   */
  @Override
  public int deleteByMemberId(Long memberId) {
    return buyerPageDAO.deleteByMemberId(memberId);
  }

  /**
   * 마이페이지 ID로 조회
   *
   * @param pageId 마이페이지 ID
   * @return 마이페이지 정보(Optional)
   */
  @Override
  public Optional<BuyerPage> findById(Long pageId) {
    return buyerPageDAO.findById(pageId);
  }

  /**
   * 마이페이지 저장 (신규/수정 자동 분기)
   * - 해당 회원의 마이페이지가 존재하면 UPDATE, 없으면 INSERT
   *
   * @param buyerPage 저장할 마이페이지 정보
   * @return 저장된 페이지 ID
   */
  @Override
  public Long saveOrUpdate(BuyerPage buyerPage) {
    Optional<BuyerPage> existingOpt = buyerPageDAO.findByMemberId(buyerPage.getMemberId());

    if (existingOpt.isPresent()) {
      BuyerPage existing = existingOpt.get();

      if (!buyerPage.getNickname().equals(existing.getNickname())
          && buyerPageDAO.existsByNickname(buyerPage.getNickname())) {
        throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
      }

      // 1) BuyerPage 갱신
      buyerPageDAO.updateById(existing.getPageId(), buyerPage);

      // 2) Member.nickname 갱신
      memberDAO.updateNickname(buyerPage.getMemberId(), buyerPage.getNickname());

      return existing.getPageId();
    }

    if (buyerPageDAO.existsByNickname(buyerPage.getNickname())) {
      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
    }

    Long pageId = buyerPageDAO.save(buyerPage);

    // Member.nickname 동기화
    memberDAO.updateNickname(buyerPage.getMemberId(), buyerPage.getNickname());

    return pageId;
  }

  @Override
  public boolean existsByNickname(String nickname) {
    return buyerPageDAO.existsByNickname(nickname);
  }


}
