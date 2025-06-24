package com.KDT.mosi.domain.terms.dao;

import com.KDT.mosi.domain.entity.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermsDAOImpl implements TermsDAO {

  private final NamedParameterJdbcTemplate template;

  // 모든 약관 조회
  @Override
  public List<Terms> findAll() {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT terms_id, name, content, is_required, version, created_at ");
    sql.append("  FROM terms ");
    sql.append(" ORDER BY terms_id ");

    return template.query(sql.toString(), BeanPropertyRowMapper.newInstance(Terms.class));
  }

  // 회원이 약관에 동의
  @Override
  public int agreeTerms(Long memberId, Long termsId) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO member_terms ");
    sql.append("      (member_id, terms_id, agreed_at) ");
    sql.append("VALUES (:memberId, :termsId, systimestamp) ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId)
        .addValue("termsId", termsId);

    return template.update(sql.toString(), param);
  }

  @Override
  public void save(long memberId, long termsId) {
    String sql = "INSERT INTO member_terms (member_id, terms_id, agreed_at) " +
        "VALUES (:memberId, :termsId, systimestamp)";

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", memberId)
        .addValue("termsId", termsId);

    template.update(sql, param);
  }

}
