package com.KDT.mosi.domain.mypage.buyer.dao;

import com.KDT.mosi.domain.entity.BuyerPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
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
public class BuyerPageDAOImpl implements BuyerPageDAO {

  private final NamedParameterJdbcTemplate template;

  // ✅ 수동 매퍼: ResultSet → BuyerPage 변환
  private RowMapper<BuyerPage> buyerPageRowMapper() {
    return (rs, rowNum) -> {
      BuyerPage buyerPage = new BuyerPage();
      buyerPage.setPageId(rs.getLong("page_id"));
      buyerPage.setMemberId(rs.getLong("member_id"));
      buyerPage.setImage(rs.getBytes("image"));
      buyerPage.setIntro(rs.getString("intro"));
      buyerPage.setRecentOrder(rs.getString("recent_order"));
      buyerPage.setPoint(rs.getInt("point"));

      if (rs.getTimestamp("create_date") != null) {
        buyerPage.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
      }
      if (rs.getTimestamp("update_date") != null) {
        buyerPage.setUpdateDate(rs.getTimestamp("update_date").toLocalDateTime()); // ✅ 수정됨
      }
      buyerPage.setNickname(rs.getString("nickname"));
      return buyerPage;
    };
  }

  // ✅ 마이페이지 등록
  @Override
  public Long save(BuyerPage buyerPage) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO BUYER_PAGE ");
    sql.append("(PAGE_ID, MEMBER_ID, IMAGE, INTRO, RECENT_ORDER, POINT, CREATE_DATE, UPDATE_DATE, NICKNAME) ");
    sql.append("VALUES (BUYER_PAGE_SEQ.NEXTVAL, :memberId, :image, :intro, :recentOrder, :point, systimestamp, systimestamp, :nickname) ");

    SqlParameterSource param = new BeanPropertySqlParameterSource(buyerPage);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    template.update(sql.toString(), param, keyHolder, new String[]{"page_id"});

    return ((Number) keyHolder.getKeys().get("page_id")).longValue();
  }

  // ✅ 회원 ID로 조회
  @Override
  public Optional<BuyerPage> findByMemberId(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT page_id, member_id, image, intro, recent_order, point, create_date, update_date, nickname ");
    sql.append("  FROM BUYER_PAGE ");
    sql.append(" WHERE member_id = :memberId ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("memberId", memberId);

    try {
      BuyerPage result = template.queryForObject(sql.toString(), param, buyerPageRowMapper());
      return Optional.of(result);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  // ✅ 페이지 ID로 수정
  @Override
  public int updateById(Long pageId, BuyerPage buyerPage) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE BUYER_PAGE ");
    sql.append("   SET image = :image, ");
    sql.append("       intro = :intro, ");
    sql.append("       nickname = :nickname, ");
    sql.append("       tel = :tel, ");
    sql.append("       address = :address, ");
    sql.append("       zonecode = :zonecode, ");
    sql.append("       detail_address = :detailAddress, ");
    sql.append("       notification = :notification, ");
    sql.append("       update_date = systimestamp ");
    sql.append(" WHERE page_id = :pageId ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("image", buyerPage.getImage())
        .addValue("intro", buyerPage.getIntro())
        .addValue("nickname", buyerPage.getNickname())
        .addValue("tel", buyerPage.getTel())
        .addValue("address", buyerPage.getAddress())
        .addValue("zonecode", buyerPage.getZonecode())
        .addValue("detailAddress", buyerPage.getDetailAddress())
        .addValue("notification", buyerPage.getNotification())
        .addValue("pageId", pageId);

    return template.update(sql.toString(), param);
  }

  // ✅ 회원 ID로 마이페이지 삭제
  @Override
  public int deleteByMemberId(Long memberId) {
    // SQL: 특정 회원의 마이페이지 정보 삭제
    String sql = "DELETE FROM BUYER_PAGE WHERE MEMBER_ID = :memberId";

    // 파라미터 바인딩
    MapSqlParameterSource param = new MapSqlParameterSource("memberId", memberId);

    // SQL 실행 및 삭제된 행 수 반환
    return template.update(sql, param);
  }


  // ✅ 페이지 ID로 조회 (삭제 권한 확인용)
  @Override
  public Optional<BuyerPage> findById(Long pageId) {
    String sql = "SELECT * FROM buyer_page WHERE page_id = :pageId";

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("pageId", pageId);

    try {
      BuyerPage buyerPage = template.queryForObject(
          sql,
          param,
          BeanPropertyRowMapper.newInstance(BuyerPage.class)
      );
      return Optional.of(buyerPage);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }
}
