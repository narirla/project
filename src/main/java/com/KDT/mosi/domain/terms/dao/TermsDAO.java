package com.KDT.mosi.domain.terms.dao;

import com.KDT.mosi.domain.entity.Terms;

import java.util.List;

public interface TermsDAO {

  /**
   * 모든 약관 조회
   * @return 약관 목록
   */
  List<Terms> findAll();

  /**
   * 회원이 약관에 동의
   * @param memberId 회원 ID
   * @param termsId 약관 ID
   * @return 반영된 행 수
   */
  int agreeTerms(Long memberId, Long termsId);

  void save(long l, long l1);
}
