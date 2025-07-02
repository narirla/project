package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.member.dao.MemberRoleDAO;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import com.KDT.mosi.domain.terms.dao.TermsDAO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 회원 서비스 구현체
 * - 회원 등록, 조회, 수정, 역할 부여, 약관 동의 처리 등 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSVCImpl implements MemberSVC {

  private final MemberDAO memberDAO;
  private final MemberRoleDAO memberRoleDAO;
  private final TermsDAO termsDAO;
  private final BuyerPageDAO buyerPageDAO;

  /**
   * 회원 등록 (기본형)
   * - 회원 정보만 저장 후, 기본 역할 "R01(구매자)" 자동 부여
   *
   * @param member 회원 정보
   * @return 저장된 회원 ID
   */
  @Override
  @Transactional
  public Long join(Member member) {
    // 1. 회원 정보 저장
    Long memberId = memberDAO.save(member);

    // 2. 기본 역할(R01) 자동 부여
    memberRoleDAO.addRole(memberId, "R01");

    return memberId;
  }

  /**
   * 회원 등록 (확장형)
   * - 회원 정보와 함께 역할 및 약관 동의 정보 저장
   *
   * @param member 등록할 회원 정보
   * @param roles 부여할 역할 ID 목록 (예: ["R01", "R02"])
   * @param agreedTermsIds 동의한 약관 ID 목록
   * @return 저장된 회원 ID
   */
  @Override
  @Transactional
  public Long join(Member member, List<String> roles, List<Long> agreedTermsIds) {
    Long memberId = memberDAO.save(member);
    log.info("회원 저장 완료: {}", memberId);

    // 1. 역할 등록
    if (roles != null && !roles.isEmpty()) {
      log.info("역할 등록 시작: {}", roles);
      for (String roleId : roles) {
        memberRoleDAO.addRole(memberId, roleId);
      }
      log.info("역할 등록 완료");
    } else {
      log.info("역할 정보 없음 또는 비어 있음");
    }

    // 2. 약관 동의 등록
    if (agreedTermsIds != null && !agreedTermsIds.isEmpty()) {
      log.info("약관 동의 등록 시작: {}", agreedTermsIds);
      for (Long termsId : agreedTermsIds) {
        termsDAO.agreeTerms(memberId, termsId);
      }
      log.info("약관 동의 등록 완료");
    } else {
      log.info("약관 동의 정보 없음 또는 비어 있음");
    }

    return memberId;
  }

  /**
   * 이메일로 회원 조회
   */
  @Override
  public Optional<Member> findByEmail(String email) {
    return memberDAO.findByEmail(email);
  }

  /**
   * 회원 ID로 회원 조회
   */
  @Override
  public Optional<Member> findById(Long memberId) {
    return memberDAO.findById(memberId);
  }

  /**
   * 이메일 중복 여부 확인 (회원가입 시 사용)
   */
  @Override
  public boolean isExistEmail(String email) {
    return memberDAO.isExistEmail(email);
  }

  /**
   * 이메일 존재 여부 확인 (비밀번호 재설정 등에서 사용)
   */
  @Override
  public boolean existsByEmail(String email) {
    return memberDAO.findByEmail(email).isPresent();
  }

  /**
   * 회원 정보 수정
   * - 비밀번호가 비어 있을 경우 기존 비밀번호 유지
   */
  @Override
  public void modify(Long id, Member member) {
    member.setMemberId(id);

    // 기존 비밀번호 유지 처리
    if (member.getPasswd() == null || member.getPasswd().isBlank()) {
      memberDAO.findById(id).ifPresent(existing -> member.setPasswd(existing.getPasswd()));
    }

    memberDAO.update(member);
  }

  /**
   * 전화번호로 이메일 찾기
   */
  @Override
  public String findEmailByTel(String tel) {
    return memberDAO.findEmailByTel(tel).orElse(null);
  }

  /**
   * 이메일 존재 여부 확인 및 반환 (비밀번호 찾기 등에서 사용)
   */
  @Override
  public String findEmailByEmail(String email) {
    return memberDAO.findByEmail(email)
        .map(Member::getEmail)
        .orElse(null);
  }

  /**
   * 비밀번호 재설정
   * - 성공 시 true 반환
   */
  @Override
  public boolean resetPassword(String email, String newPassword) {
    return memberDAO.updatePassword(email, newPassword) == 1;
  }

  /**
   * 회원이 특정 역할을 가지고 있는지 확인
   */
  @Override
  public boolean hasRole(Long memberId, String roleId) {
    return memberRoleDAO.findRolesByMemberId(memberId)
        .stream()
        .anyMatch(role -> role.getRoleId().equals(roleId));
  }

  /**
   * 닉네임 중복 여부 확인
   */
  @Override
  public boolean isExistNickname(String nickname) {
    return memberDAO.isExistNickname(nickname);
  }

  /**
   * 회원 탈퇴
   * - 1단계: 마이페이지 정보 삭제
   * - 2단계: 회원 정보 삭제
   */
  @Override
  public int deleteById(Long memberId) {
    buyerPageDAO.deleteByMemberId(memberId);
    return memberDAO.deleteById(memberId);
  }

}
