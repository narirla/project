package com.KDT.mosi.domain.mypage.buyer.dao;

import com.KDT.mosi.domain.entity.BuyerPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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

  //수동 매핑
  private RowMapper<BuyerPage> buyerPageRowMapper(){
    return (rs, rowNum) ->{
      BuyerPage buyerPage =new BuyerPage();
      buyerPage.setPageId(rs.getLong("page_id"));
      buyerPage.setMemberId(rs.getLong("member_id"));
      buyerPage.setImage(rs.getBytes("image"));
      buyerPage.setIntro(rs.getString("intro"));
      buyerPage.setRecentOrder(rs.getString("recent_order"));
      buyerPage.setPoint(rs.getInt("point"));

      if (rs.getTimestamp("create_date") != null){
        buyerPage.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
      }
      if (rs.getTimestamp("update_date") != null){
        buyerPage.setCreateDate(rs.getTimestamp("update_date").toLocalDateTime());
      }
      return buyerPage;
    };
  }

  //등록
  @Override
  public Long save(BuyerPage buyerPage) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO BUYER_PAGE (PAGE_ID, MEMBER_ID, IMAGE, INTRO, RECENT_ORDER, POINT, CREATE_DATE, UPDATE_DATE) ");
    sql.append("VALUES (BUYER_PAGE_SEQ.NEXTVAL, :memberId, :image, :intro, :recentOrder, :point, systimestamp, systimestamp) ");

    SqlParameterSource param = new BeanPropertySqlParameterSource(buyerPage);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    template.update(sql.toString(),param,keyHolder, new String[]{"page_id"});

    return ((Number) keyHolder.getKeys().get("page_id")).longValue();
  }

  //조회
  @Override
  public Optional<BuyerPage> findByMemberId(Long memberId) {
    StringBuffer sql =new StringBuffer();
    sql.append("SELECT page_id, member_id, image, intro, recent_order, point, create_date, update_date ");
    sql.append("  FROM BUYER_PAGE ");
    sql.append(" WHERE member_id = :memberId ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("memberId",memberId);

    BuyerPage result = null;
    try {
      result = template.queryForObject(sql.toString(), param, buyerPageRowMapper());
    } catch (EmptyResultDataAccessException e){
      return Optional.empty();
    }


    return Optional.of(result);
  }

  //수정
  @Override
  public int updateById(Long pageId, BuyerPage buyerPage) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE BUYER_PAGE ");
    sql.append("   SET image = :image, intro = :intro, recent_order = :recentOrder, point = :point, update_date = systimestamp ");
    sql.append(" WHERE page_id = :pageId ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("image", buyerPage.getImage())
        .addValue("intro", buyerPage.getIntro())
        .addValue("recentOrder", buyerPage.getRecentOrder())
        .addValue("point", buyerPage.getPoint())
        .addValue("pageId", pageId);

    return template.update(sql.toString(),param);
  }

  //삭제
  @Override
  public int deleteById(Long pageId) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM BUYER_PAGE WHERE page_id = :pageId ");

    MapSqlParameterSource param = new MapSqlParameterSource("pageId", pageId);
    return template.update(sql.toString(), param);
  }
}
