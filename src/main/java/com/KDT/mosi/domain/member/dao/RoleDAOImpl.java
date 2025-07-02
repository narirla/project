package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 역할 DAO 구현 클래스
 * - ROLE 테이블 전용
 */
@Repository
@RequiredArgsConstructor
public class RoleDAOImpl implements RoleDAO {

  private final NamedParameterJdbcTemplate template;

  /**
   * 전체 역할 목록 조회
   * - ROLE 테이블에서 role_id, role_name 조회
   *
   * @return 역할 리스트
   */
  @Override
  public List<Role> findAll() {
    String sql = "SELECT role_id, role_name FROM role ORDER BY role_id";
    return template.query(sql, BeanPropertyRowMapper.newInstance(Role.class));
  }
}
