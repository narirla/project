package com.KDT.mosi.domain.mypage.seller.dao;

import com.KDT.mosi.domain.entity.SellerPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 판매자 마이페이지 DAO 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SellerPageDAOImpl implements SellerPageDAO {

  private final NamedParameterJdbcTemplate template;

  /**
   * 결과셋 → SellerPage 매핑(RowMapper)
   * - 숫자 컬럼은 getObject로 받아 NULL 보존
   */
  private RowMapper<SellerPage> sellerPageRowMapper() {
    return (rs, rowNum) -> {
      SellerPage sp = new SellerPage();
      sp.setPageId(rs.getLong("page_id"));
      sp.setMemberId(rs.getLong("member_id"));
      sp.setNickname(rs.getString("nickname"));
      sp.setImage(rs.getBytes("image"));
      sp.setIntro(rs.getString("intro"));

      // NULL 보존
      sp.setSalesCount((Integer) rs.getObject("sales_count", Integer.class));
      sp.setReviewAvg((Double) rs.getObject("review_avg", Double.class));

      sp.setZonecode(rs.getString("zonecode"));
      sp.setAddress(rs.getString("address"));
      sp.setDetailAddress(rs.getString("detail_address"));
      sp.setCreateDate(rs.getTimestamp("create_date"));
      sp.setUpdateDate(rs.getTimestamp("update_date"));
      return sp;
    };
  }

  /**
   * 판매자 마이페이지 등록
   * - Oracle: 시퀀스 선조회 후 명시 바인딩
   */
  @Override
  public Long save(SellerPage sellerpage) {

    // 0) 널 방어
    if (sellerpage == null || sellerpage.getMemberId() == null) {
      throw new IllegalArgumentException("memberId must not be null");
    }

    // 1) 시퀀스 선조회(안정 패턴)
    Long pageId = template.queryForObject(
        "SELECT SELLER_PAGE_SEQ.NEXTVAL FROM DUAL",
        new MapSqlParameterSource(),
        Long.class
    );

    // 2) INSERT
    String sql =
        "INSERT INTO SELLER_PAGE (" +
            "  PAGE_ID, MEMBER_ID, IMAGE, INTRO, NICKNAME, SALES_COUNT, REVIEW_AVG, CREATE_DATE, UPDATE_DATE" +
            ") VALUES (" +
            "  :pageId, :memberId, :image, :intro, :nickname, :salesCount, :reviewAvg, SYSTIMESTAMP, SYSTIMESTAMP" +
            ")";

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("pageId", pageId)
        .addValue("memberId", sellerpage.getMemberId())
        .addValue("image", sellerpage.getImage())
        .addValue("intro", sellerpage.getIntro())
        .addValue("nickname", sellerpage.getNickname())
        .addValue("salesCount", sellerpage.getSalesCount() == null ? 0 : sellerpage.getSalesCount())
        .addValue("reviewAvg", sellerpage.getReviewAvg() == null ? 0.0 : sellerpage.getReviewAvg());

    template.update(sql, param);
    return pageId;
  }

  /**
   * 회원 ID로 마이페이지 조회
   */
  @Override
  public Optional<SellerPage> findByMemberId(Long memberId) {
    String sql = "SELECT * FROM SELLER_PAGE WHERE MEMBER_ID = :memberId";
    SqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
    try {
      SellerPage sellerpage = template.queryForObject(sql, param, sellerPageRowMapper());
      return Optional.ofNullable(sellerpage);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * 페이지 ID로 마이페이지 조회
   */
  @Override
  public Optional<SellerPage> findById(Long pageId) {
    String sql = "SELECT * FROM SELLER_PAGE WHERE PAGE_ID = :pageId";
    SqlParameterSource param = new MapSqlParameterSource("pageId", pageId);
    try {
      SellerPage sellerpage = template.queryForObject(sql, param, sellerPageRowMapper());
      return Optional.ofNullable(sellerpage);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * 회원 ID로 마이페이지 존재 여부
   */
  @Override
  public boolean existByMemberId(Long memberId) {
    String sql =
        "SELECT CASE WHEN EXISTS(SELECT 1 FROM SELLER_PAGE WHERE MEMBER_ID = :memberId) " +
            "THEN 1 ELSE 0 END FROM DUAL";
    Integer v = template.queryForObject(sql, new MapSqlParameterSource("memberId", memberId), Integer.class);
    return v != null && v == 1;
  }

  /**
   * 닉네임 중복 여부
   */
  @Override
  public boolean existByNickname(String nickname) {
    String sql =
        "SELECT CASE WHEN EXISTS(SELECT 1 FROM SELLER_PAGE WHERE NICKNAME = :nickname) " +
            "THEN 1 ELSE 0 END FROM DUAL";
    Integer v = template.queryForObject(sql, new MapSqlParameterSource("nickname", nickname), Integer.class);
    return v != null && v == 1;
  }

<<<<<<< HEAD
=======
  // memberID로 별명 찾기
  @Override
  public Optional<String> findNicknameByMemberId(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT nickname FROM seller_page WHERE member_id = :memberId");

    MapSqlParameterSource param = new MapSqlParameterSource();
    param.addValue("memberId", memberId);

    try {
      String nickname = template.queryForObject(sql.toString(), param, String.class);
      return Optional.ofNullable(nickname);
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
      // 조회 결과가 없을 경우
      return Optional.empty();
    }
  }


>>>>>>> e506fd3749059f9445a987ad395676865572bc94
  /**
   * 마이페이지 정보 수정
   */
  @Override
  public int updateById(Long pageId, SellerPage sellerpage) {
    String sql =
        "UPDATE SELLER_PAGE SET " +
            "  IMAGE = :image, " +
            "  INTRO = :intro, " +
            "  NICKNAME = :nickname, " +
            "  SALES_COUNT = :salesCount, " +
            "  REVIEW_AVG = :reviewAvg, " +
            "  ZONECODE = :zonecode, " +
            "  ADDRESS = :address, " +
            "  DETAIL_ADDRESS = :detailAddress, " +
            "  UPDATE_DATE = SYSTIMESTAMP " +
            "WHERE PAGE_ID = :pageId";

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("image", sellerpage.getImage())
        .addValue("intro", sellerpage.getIntro())
        .addValue("nickname", sellerpage.getNickname())
        // NOT NULL 컬럼 보정(필요 시)
        .addValue("salesCount", sellerpage.getSalesCount() == null ? 0 : sellerpage.getSalesCount())
        .addValue("reviewAvg", sellerpage.getReviewAvg() == null ? 0.0 : sellerpage.getReviewAvg())
        .addValue("zonecode", sellerpage.getZonecode())
        .addValue("address", sellerpage.getAddress())
        .addValue("detailAddress", sellerpage.getDetailAddress())
        .addValue("pageId", pageId);

    return template.update(sql, param);
  }

  /**
   * 회원 ID로 마이페이지 삭제
   */
  @Override
  public int deleteByMemberId(Long memberId) {
    String sql = "DELETE FROM SELLER_PAGE WHERE MEMBER_ID = :memberId";
    return template.update(sql, new MapSqlParameterSource("memberId", memberId));
  }
}
