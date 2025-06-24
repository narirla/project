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

  /**
   * 역할 전체 조회
   * - role 테이블에서 모든 역할 정보를 조회
   * @return 역할 목록
   */
  @Override
  public List<Role> findAll() {
    String sql = "SELECT role_id, role_name FROM role ORDER BY role_id";
    return template.query(sql, BeanPropertyRowMapper.newInstance(Role.class));
  }

  /**
   * 회원에게 역할 부여
   * - member_role 테이블에 새로운 역할 정보 삽입
   * @param memberId 회원 ID
   * @param roleId 역할 ID (R01, R02 등)
   * @return 반영된 행 수
   */
  @Override
  public int addRoleToMember(Long memberId, String roleId) {
    String sql = "INSERT INTO member_role (member_id, role_id) VALUES (:memberId, :roleId)";
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId)
        .addValue("roleId", roleId);
    return template.update(sql, param);
  }

  /**
   * 역할 저장 (addRoleToMember와 동일 기능)
   * - 별도 명시된 메서드 이름으로 중복 삽입 처리 가능
   */
  @Override
  public void save(long memberId, String roleId) {
    String sql = "INSERT INTO member_role (member_id, role_id) VALUES (:memberId, :roleId)";
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId)
        .addValue("roleId", roleId);
    template.update(sql, param);
  }

  /**
   * 회원 ID로 역할 목록 조회
   * - member_role과 role 테이블을 조인하여 역할 정보 조회
   * @param memberId 회원 ID
   * @return 역할 리스트 (Role 객체)
   */
  @Override
  public List<Role> findRolesByMemberId(Long memberId) {
    String sql = """
      SELECT r.role_id, r.role_name
      FROM member_role mr
        JOIN role r ON mr.role_id = r.role_id
      WHERE mr.member_id = :memberId
    """;

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId);

    return template.query(sql, param, BeanPropertyRowMapper.newInstance(Role.class));
  }

}
