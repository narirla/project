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

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements MemberDAO {

  private final NamedParameterJdbcTemplate template;

  // 회원 저장
  @Override
  public Long save(Member member) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO member (member_id, email, name, passwd, tel, nickname, gender, address, pic, create_date, update_date) ");
    sql.append("VALUES (member_member_id_seq.nextval, :email, :name, :passwd, :tel, :nickname, :gender, :address, :pic, systimestamp, systimestamp)");

    SqlParameterSource param = new BeanPropertySqlParameterSource(member);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(sql.toString(), param, keyHolder, new String[]{"member_id"});

    return ((Number) keyHolder.getKeys().get("member_id")).longValue();
  }

  // 이메일로 회원 조회
  @Override
  public Optional<Member> findByEmail(String email) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM member ");
    sql.append(" WHERE email = :email");

    try {
      Member member = template.queryForObject(
          sql.toString(),
          new MapSqlParameterSource("email", email),
          BeanPropertyRowMapper.newInstance(Member.class)
      );
      return Optional.of(member);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  // ID로 회원 조회
  @Override
  public Optional<Member> findById(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM member ");
    sql.append(" WHERE member_id = :memberId");

    try {
      Member member = template.queryForObject(
          sql.toString(),
          new MapSqlParameterSource("memberId", memberId),
          BeanPropertyRowMapper.newInstance(Member.class)
      );
      return Optional.of(member);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  // 이메일 존재 여부
  @Override
  public boolean isExistEmail(String email) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT count(*) FROM member ");
    sql.append(" WHERE email = :email");

    Integer count = template.queryForObject(
        sql.toString(),
        new MapSqlParameterSource("email", email),
        Integer.class
    );
    return count != null && count > 0;
  }
}
