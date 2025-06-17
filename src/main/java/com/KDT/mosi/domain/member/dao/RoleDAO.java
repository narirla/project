package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Role;

import java.util.List;

public interface RoleDAO {

  /**
   * 전체 역할 조회
   * @return 역할 목록
   */
  List<Role> findAll();

  /**
   * 회원에게 역할 부여
   * @param memberId 회원 ID
   * @param roleId 역할 ID (R01, R02)
   * @return 반영된 행 수
   */
  int addRoleToMember(Long memberId, String roleId);
}
