package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Role;

import java.util.List;

/**
 * 역할 DAO (ROLE 테이블 전용)
 */
public interface RoleDAO {

  /**
   * 전체 역할 목록 조회
   *
   * @return 역할 리스트
   */
  List<Role> findAll();
}
