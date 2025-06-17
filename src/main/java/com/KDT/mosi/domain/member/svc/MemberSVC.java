package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberSVC {

  // 회원 등록
  Long join(Member member);


  //확장형: 역할 + 약관 동의 포함
  Long join(Member member, List<String> roles, List<Long> agreedTermsIds);

  // 회원 이메일로 조회
  Optional<Member> findByEmail(String email);

  // 회원 ID로 조회
  Optional<Member> findById(Long memberId);

  // 이메일 중복 체크
  boolean isExistEmail(String email);
}
