package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoleDAOImpl implements RoleDAO {

  private final NamedParameterJdbcTemplate template;

  // 역할 전체 조회
  @Override
  public List<Role> findAll() {
    String sql = "SELECT role_id, role_name FROM role ORDER BY role_id";
    return template.query(sql, BeanPropertyRowMapper.newInstance(Role.class));
  }

  // 회원에게 역할 부여
  @Override
  public int addRoleToMember(Long memberId, String roleId) {
    String sql = "INSERT INTO member_role (member_id, role_id) VALUES (:memberId, :roleId)";
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId)
        .addValue("roleId", roleId);
    return template.update(sql, param);
  }
}
