package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.terms.dao.TermsDAO;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import lombok.extern.slf4j.Slf4j;
>>>>>>> feature/member
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
=======
/**
 * 회원 서비스 구현체
 * - 회원 등록, 조회, 수정, 역할/약관 처리, 인증 관련 기능 구현
 */
@Slf4j
>>>>>>> feature/member
@Service
@RequiredArgsConstructor
public class MemberSVCImpl implements MemberSVC {

  private final MemberDAO memberDAO;
  private final RoleDAO roleDAO;
  private final TermsDAO termsDAO;

<<<<<<< HEAD
=======
  /**
   * 단순 회원 등록
   * @param member 회원 정보
   * @return 등록된 회원의 ID
   */
>>>>>>> feature/member
  @Override
  public Long join(Member member) {
    return memberDAO.save(member);
  }

<<<<<<< HEAD
  @Override
  public Long join(Member member, List<String> roles, List<Long> agreedTermsIds) {
    Long memberId = memberDAO.save(member);

    // 역할 등록
    if (roles != null) {
      for (String roleId : roles) {
        roleDAO.addRoleToMember(memberId, roleId);
      }
    }

    // 약관 동의 등록
    if (agreedTermsIds != null) {
      for (Long termsId : agreedTermsIds) {
        termsDAO.agreeTerms(memberId, termsId);
      }
=======
  /**
   * 확장형 회원 등록: 역할 + 약관 동의 포함
   * @param member 회원 정보
   * @param roles 회원이 선택한 역할 리스트
   * @param agreedTermsIds 동의한 약관 ID 리스트
   * @return 등록된 회원의 ID
   */
  @Override
  public Long join(Member member, List<String> roles, List<Long> agreedTermsIds) {
    Long memberId = memberDAO.save(member);
    log.info("회원 저장 완료: {}", memberId);

    // 역할 등록
    if (roles != null && !roles.isEmpty()) {
      log.info("역할 등록 시작: {}", roles);
      for (String roleId : roles) {
        roleDAO.addRoleToMember(memberId, roleId);
      }
      log.info("역할 등록 완료");
    } else {
      log.info("역할 정보 없음 또는 비어 있음");
    }

    // 약관 동의 등록
    if (agreedTermsIds != null && !agreedTermsIds.isEmpty()) {
      log.info("약관 동의 등록 시작: {}", agreedTermsIds);
      for (Long termsId : agreedTermsIds) {
        termsDAO.agreeTerms(memberId, termsId);
      }
      log.info("약관 동의 등록 완료");
    } else {
      log.info("약관 동의 정보 없음 또는 비어 있음");
>>>>>>> feature/member
    }

    return memberId;
  }

<<<<<<< HEAD
=======
  /**
   * 이메일로 회원 조회
   * @param email 이메일
   * @return Optional<Member>
   */
>>>>>>> feature/member
  @Override
  public Optional<Member> findByEmail(String email) {
    return memberDAO.findByEmail(email);
  }

<<<<<<< HEAD
=======
  /**
   * 회원 ID로 조회
   * @param memberId 회원 ID
   * @return Optional<Member>
   */
>>>>>>> feature/member
  @Override
  public Optional<Member> findById(Long memberId) {
    return memberDAO.findById(memberId);
  }

<<<<<<< HEAD
=======
  /**
   * 이메일 존재 여부 확인 (회원가입 시 중복 방지용)
   */
>>>>>>> feature/member
  @Override
  public boolean isExistEmail(String email) {
    return memberDAO.isExistEmail(email);
  }
<<<<<<< HEAD
=======

  /**
   * 이메일 존재 여부 확인 (추가적인 인증용)
   */
  @Override
  public boolean existsByEmail(String email) {
    return memberDAO.findByEmail(email).isPresent();
  }

  /**
   * 회원 정보 수정
   * @param id 회원 ID
   * @param member 수정할 정보가 담긴 Member 객체
   */
  @Override
  public void modify(Long id, Member member) {
    member.setMemberId(id);
    memberDAO.update(member);  // DAO에 update 메서드 구현 필요
  }

  /**
   * 전화번호로 이메일 찾기
   */
  @Override
  public String findEmailByTel(String tel) {
    return memberDAO.findEmailByTel(tel).orElse(null);
  }

  /**
   * 이메일로 이메일 확인 (비밀번호 재설정 등)
   */
  @Override
  public String findEmailByEmail(String email) {
    return memberDAO.findByEmail(email)
        .map(Member::getEmail)
        .orElse(null);
  }

  /**
   * 비밀번호 재설정
   * @param email 대상 이메일
   * @param newPassword 새 비밀번호
   * @return 성공 여부
   */
  @Override
  public boolean resetPassword(String email, String newPassword) {
    return memberDAO.updatePassword(email, newPassword) == 1;
  }

  /**
   * 회원이 특정 역할을 가지고 있는지 확인
   * @param memberId 회원 ID
   * @param roleId 역할 ID (예: R01)
   * @return 해당 역할을 갖고 있으면 true
   */
  @Override
  public boolean hasRole(Long memberId, String roleId) {
    return roleDAO.findRolesByMemberId(memberId)
        .stream()
        .anyMatch(role -> role.getRoleId().equals(roleId));
  }
>>>>>>> feature/member
}
