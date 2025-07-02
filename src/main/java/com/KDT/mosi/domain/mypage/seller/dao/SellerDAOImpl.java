package com.KDT.mosi.domain.mypage.seller.dao;

import com.KDT.mosi.domain.entity.SellerPage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class SellerDAOImpl implements SellerPageDao {

  private final JdbcTemplate jdbcTemplate;

  public SellerDAOImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private final RowMapper<SellerPage> rowMapper = (rs, rowNum) -> {
    SellerPage sp = new SellerPage();
    sp.setPageId(rs.getLong("PAGE_ID"));
    sp.setMemberId(rs.getLong("MEMBER_ID"));
    sp.setImage(rs.getBytes("IMAGE"));
    sp.setIntro(rs.getString("INTRO"));
    sp.setSalesCount(rs.getInt("SALES_COUNT"));
    sp.setReviewAvg(rs.getDouble("REVIEW_AVG"));
    sp.setCreateDate(rs.getTimestamp("CREATE_DATE"));
    sp.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
    return sp;
  };

  @Override
  public void insert(SellerPage sp) {
    String sql = "INSERT INTO SELLER_PAGE (PAGE_ID, MEMBER_ID, IMAGE, INTRO, SALES_COUNT, REVIEW_AVG, CREATE_DATE, UPDATE_DATE) " +
        "VALUES (SEQ_SELLER_PAGE.NEXTVAL, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";
    jdbcTemplate.update(sql, sp.getMemberId(), sp.getImage(), sp.getIntro(), sp.getSalesCount(), sp.getReviewAvg());
  }

  @Override
  public Optional<SellerPage> findByMemberId(Long memberId) {
    try {
      String sql = "SELECT * FROM SELLER_PAGE WHERE MEMBER_ID = ?";
      SellerPage result = jdbcTemplate.queryForObject(sql, rowMapper, memberId);
      return Optional.ofNullable(result);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public void update(SellerPage sp) {
    String sql = "UPDATE SELLER_PAGE SET IMAGE = ?, INTRO = ?, SALES_COUNT = ?, REVIEW_AVG = ?, UPDATE_DATE = SYSTIMESTAMP WHERE PAGE_ID = ?";
    jdbcTemplate.update(sql, sp.getImage(), sp.getIntro(), sp.getSalesCount(), sp.getReviewAvg(), sp.getPageId());
  }

  @Override
  public void delete(Long pageId) {
    jdbcTemplate.update("DELETE FROM SELLER_PAGE WHERE PAGE_ID = ?", pageId);
  }
}
