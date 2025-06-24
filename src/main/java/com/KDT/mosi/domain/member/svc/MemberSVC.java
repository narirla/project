package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberSVC {

  // 회원 등록
  Long join(Member member);

<<<<<<< HEAD

  //확장형: 역할 + 약관 동의 포함
=======
  // 확장형: 역할 + 약관 동의 포함
>>>>>>> feature/member
  Long join(Member member, List<String> roles, List<Long> agreedTermsIds);

  // 회원 이메일로 조회
  Optional<Member> findByEmail(String email);

  // 회원 ID로 조회
  Optional<Member> findById(Long memberId);

  // 이메일 중복 체크
  boolean isExistEmail(String email);
<<<<<<< HEAD
=======

  // 인터페이스
  boolean existsByEmail(String email);

  void modify(Long id, Member member);


  // ✅ 전화번호로 이메일 찾기
  String findEmailByTel(String tel);

  // ✅ 이메일로 존재 확인 (비밀번호 재설정 시 사용)
  String findEmailByEmail(String email);

  // ✅ 비밀번호 재설정
  boolean resetPassword(String email, String newPassword);

  boolean hasRole(Long memberId, String roleId);

>>>>>>> feature/member
}
