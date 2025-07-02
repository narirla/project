package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Member;

import java.util.Optional;

public interface MemberDAO {

  /**
   * 회원 등록
   * @param member 회원 정보
   * @return 회원 번호 (memberId)
   */
  Long save(Member member);

  /**
   * 이메일로 회원 조회
   * @param email 이메일
   * @return Optional<Member>
   */
  Optional<Member> findByEmail(String email);

  /**
   * 회원 ID로 조회
   * @param memberId 회원 ID
   * @return Optional<Member>
   */
  Optional<Member> findById(Long memberId);

  /**
   * 이메일 중복 확인
   * @param email 이메일
   * @return 존재 여부
   */
  boolean isExistEmail(String email);
<<<<<<< HEAD
=======

  /**
   * 회원 정보 수정
   * @param member 수정할 회원 정보
   * @return 수정된 행 수
   */
  int update(Member member);

  Optional<String> findEmailByTel(String tel);


  int updatePassword(String email, String newPassword);

>>>>>>> feature/member
}
