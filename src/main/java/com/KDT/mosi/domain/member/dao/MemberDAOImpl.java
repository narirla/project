package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements MemberDAO {

  private final NamedParameterJdbcTemplate template;

  /**
   * 회원 저장
   * - 시퀀스를 사용하여 member_id 자동 생성
   */
  @Override
  public Long save(Member member) {
    String sql = """
      INSERT INTO member 
        (member_id, email, name, passwd, tel, nickname, gender, address, birth_date, pic, create_date, update_date) 
      VALUES 
        (member_member_id_seq.nextval, :email, :name, :passwd, :tel, :nickname, :gender, :address, :birthDate, :pic, systimestamp, systimestamp)
      """;

    SqlParameterSource param = new BeanPropertySqlParameterSource(member);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    template.update(sql, param, keyHolder, new String[]{"member_id"});
    return ((Number) keyHolder.getKeys().get("member_id")).longValue();
  }

  /** 이메일로 회원 조회 */
  @Override
  public Optional<Member> findByEmail(String email) {
    String sql = "SELECT * FROM member WHERE email = :email";

    try {
      Member member = template.queryForObject(
          sql,
          new MapSqlParameterSource("email", email),
          BeanPropertyRowMapper.newInstance(Member.class)
      );
      return Optional.of(member);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /** 회원 ID로 조회 */
  @Override
  public Optional<Member> findById(Long memberId) {
    String sql = "SELECT * FROM member WHERE member_id = :memberId";

    try {
      Member member = template.queryForObject(
          sql,
          new MapSqlParameterSource("memberId", memberId),
          BeanPropertyRowMapper.newInstance(Member.class)
      );
      return Optional.of(member);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /** 이메일 중복 여부 확인 */
  @Override
  public boolean isExistEmail(String email) {
    String sql = "SELECT count(*) FROM member WHERE email = :email";

    Integer count = template.queryForObject(
        sql,
        new MapSqlParameterSource("email", email),
        Integer.class
    );
    return count != null && count > 0;
  }

  /** 회원 정보 수정 */
  @Override
  public int update(Member member) {
    String sql = """
      UPDATE member SET 
        name = :name, passwd = :passwd, tel = :tel, nickname = :nickname, gender = :gender, 
        address = :address, birth_date = :birthDate, pic = :pic, update_date = systimestamp 
      WHERE member_id = :memberId
    """;

    SqlParameterSource param = new BeanPropertySqlParameterSource(member);
    return template.update(sql, param);
  }

  /** 전화번호로 이메일 찾기 */
  @Override
  public Optional<String> findEmailByTel(String tel) {
    String sql = "SELECT email FROM member WHERE tel = :tel";

    try {
      String email = template.queryForObject(sql, new MapSqlParameterSource("tel", tel), String.class);
      return Optional.ofNullable(email);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /** 비밀번호 재설정 */
  @Override
  public int updatePassword(String email, String newPassword) {
    String sql = "UPDATE member SET passwd = :passwd, update_date = systimestamp WHERE email = :email";

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("passwd", newPassword)
        .addValue("email", email);

    return template.update(sql, param);
  }

  /** 닉네임 중복 여부 확인 */
  @Override
  public boolean isExistNickname(String nickname) {
    String sql = "SELECT COUNT(*) FROM member WHERE nickname = :nickname";

    Integer count = template.queryForObject(sql, new MapSqlParameterSource("nickname", nickname), Integer.class);
    return count != null && count > 0;
  }

  /** 회원 탈퇴 */
  @Override
  public int deleteById(Long memberId) {
    String sql = "DELETE FROM member WHERE member_id = :memberId";
    return template.update(sql, new MapSqlParameterSource("memberId", memberId));
  }

  /** 이메일로 회원 ID 찾기 */
  @Override
  public Optional<Long> findMemberIdByEmail(String email) {
    String sql = "SELECT member_id FROM member WHERE email = :email";
    Map<String, Object> params = Map.of("email", email);

    try {
      Long memberId = template.queryForObject(sql, params, Long.class);
      return Optional.ofNullable(memberId);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /** 회원 ID 존재 여부 확인 */
  @Override
  public boolean isExistMemberId(Long memberId) {
    return findById(memberId).isPresent();
  }

  /** 전화번호 수정 */
  @Override
  public int updateTel(Long memberId, String tel) {
    String sql = "UPDATE member SET tel = :tel, update_date = systimestamp WHERE member_id = :memberId";

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("tel", tel)
        .addValue("memberId", memberId);

    return template.update(sql, param);
  }

  /** 비밀번호 수정 */
  @Override
  public int updatePasswd(Long memberId, String passwd) {
    String sql = "UPDATE member SET passwd = :passwd, update_date = systimestamp WHERE member_id = :memberId";

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("passwd", passwd)
        .addValue("memberId", memberId);

    return template.update(sql, param);
  }

  /** 회원 ID로 비밀번호 조회 */
  @Override
  public String findPasswdById(Long memberId) {
    String sql = "SELECT passwd FROM member WHERE member_id = :memberId";
    return template.queryForObject(sql, Map.of("memberId", memberId), String.class);
  }


  /**
   * ✅ 다중 ROLE 지원
   * - queryForList로 다중 ROLE 처리
   */
  @Override
  public List<String> findRolesByMemberId(Long memberId) {
    String sql = """
            SELECT r.role_name
            FROM member_role mr
            JOIN role r ON mr.role_id = r.role_id
            WHERE mr.member_id = :memberId
        """;

    Map<String, Object> params = Map.of("memberId", memberId);

    return template.queryForList(sql, params, String.class);
  }

}
