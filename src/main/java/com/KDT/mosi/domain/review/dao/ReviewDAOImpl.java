package com.KDT.mosi.domain.review.dao;

import com.KDT.mosi.domain.entity.review.*;
import com.KDT.mosi.web.form.review.TagInfo;
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
@RequiredArgsConstructor
@Repository
public class ReviewDAOImpl implements ReviewDAO{

  final private NamedParameterJdbcTemplate template;

  @Override
  public Optional<ReviewProduct> summaryFindById(Long id) {

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT p.product_id AS product_id,p.category AS category,p.title AS title,p.create_date AS create_date,sp.nickname AS nickname,i.mime_type AS MIME_TYPE,i.image_data AS image_data  ");
    sql.append("FROM product p ");
    sql.append("LEFT JOIN product_image i ");
    sql.append("  ON p.PRODUCT_ID = i.PRODUCT_ID ");
    sql.append(" AND i.image_order = ( ");
    sql.append("       SELECT MIN(pi.image_order) FROM product_image pi WHERE pi.product_id = p.product_id ");
    sql.append("     ) ");
    sql.append("LEFT JOIN seller_page sp ON sp.member_id = p.member_id ");
    sql.append("WHERE p.product_ID = :productId ");;

    SqlParameterSource param = new MapSqlParameterSource().addValue("productId",id);

    ReviewProduct reviewProduct = null;
    try {
      reviewProduct = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(ReviewProduct.class));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }

    return Optional.of(reviewProduct);
  }

  @Override
  public Optional<ReviewInfo> findBuyerIdByOrderItemId(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT buyer_id,product_id,option_type,REVIEWED ");
    sql.append("FROM ORDER_ITEMS i ");
    sql.append("JOIN orders o ");
    sql.append("ON i.order_id = o.order_id ");
    sql.append("WHERE i.order_item_id= :orderItemId");

    ReviewInfo reviewInfo;
    try {
      SqlParameterSource param = new MapSqlParameterSource().addValue("orderItemId",id);
      reviewInfo = template.queryForObject(sql.toString(), param,  BeanPropertyRowMapper.newInstance(ReviewInfo.class));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }

    return Optional.of(reviewInfo);
  }

  @Override
  public List<TagInfo> findTagList(String category) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT tag_id, label, slug ");
    sql.append("FROM tag ");
    sql.append("WHERE useyn = 'Y' ");
    sql.append("AND (commonyn = 'Y' OR tcategory = :category) ");
    sql.append("ORDER BY ");
    sql.append("DECODE(commonyn, 'Y', 0, 1), ");
    sql.append("DECODE(recoyn,  'Y', 0, 1), ");
    sql.append("tag_id ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("category", category);
    //db요청
    List<TagInfo> list = template.query(sql.toString(), param, BeanPropertyRowMapper.newInstance(TagInfo.class));

    return list;
  }

  @Override
  public Optional<Long> findBuyerIdByReviewId(Long id) {
    String sql = "SELECT BUYER_ID FROM REVIEW WHERE REVIEW_ID = :id";
    SqlParameterSource param = new MapSqlParameterSource().addValue("id", id);
    try {
      Long buyerId = template.queryForObject(sql, param, Long.class);
      return Optional.ofNullable(buyerId);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty(); // 해당 리뷰 없음
    }
  }

  @Override
  public Long saveReview(Review review) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO review(REVIEW_ID,PRODUCT_ID,BUYER_ID,ORDER_ITEM_ID,content,SCORE) ");
    sql.append("VALUES (REVIEW_SEQ.nextval,:productId,:buyerId,:orderItemId,:content,:score) ");

    SqlParameterSource param = new BeanPropertySqlParameterSource(review);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(sql.toString(), param, keyHolder, new String[]{"REVIEW_ID"});

    Number key = (Number)keyHolder.getKeys().get("REVIEW_ID");
    return key.longValue();
  }

  @Override
  public int saveReviewTag(ReviewTag reviewTag) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO REVIEW_TAG(REVIEW_ID,tag_id,sort_order) ");
    sql.append("VALUES (:reviewId,:tagId,:sortOrder) ");

    SqlParameterSource param = new BeanPropertySqlParameterSource(reviewTag);
    int result = template.update(sql.toString(), param);
    return result;
  }

  @Override
  public boolean findTagId(Long id,String category) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT count(*) ");
    sql.append("FROM TAG ");
    sql.append("WHERE tag_id = :id ");
    sql.append("AND (tcategory = :category OR commonyn='Y') ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("category", category)
        .addValue("id", id);
    int i = template.queryForObject(sql.toString(), param, Integer.class);
    if(i>0) return true;

    return false;
  }

  @Override
  public int updateReviewed(Long orderItemId) {
    String sql = "UPDATE ORDER_ITEMS " +
        "SET REVIEWED = 'Y' " +
        "WHERE ORDER_ITEM_ID = :orderItemId";

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("orderItemId", orderItemId);

    return template.update(sql, params);
  }

  @Override
  public Optional<String> findCategory(Long orderItemId) {
    String sql = """
      SELECT p.category
        FROM order_items oi
        JOIN product p ON p.product_id = oi.product_id
       WHERE oi.order_item_id = :orderItemId
      """;
    MapSqlParameterSource params = new MapSqlParameterSource("orderItemId", orderItemId);
    List<String> list = template.query(sql, params,
        (rs, rn) -> rs.getString(1));
    return list.stream().findFirst();
  }

  @Override
  public List<ReviewList> reviewFindAll(Long buyerId, int pageNo, int numOfRows) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    sql.append("r.review_id,r.content,r.score,r.seller_recoyn AS seller_reco_yn,r.create_date AS rcreate,r.update_date AS rupdate, ");
    sql.append("p.title AS title,p.create_date AS pcreate,p.update_date AS pupdate, ");
    sql.append("oi.option_type AS option_type, ");
    sql.append("tags.tag_ids,tags.tag_labels, ");
    sql.append("i.image_id AS product_image_id,i.mime_type AS product_image_mime " );
    sql.append("FROM review r ");
    sql.append("LEFT JOIN product p ");
    sql.append("ON p.product_id = r.product_id ");
    sql.append("LEFT JOIN order_items oi ");
    sql.append("ON oi.order_item_id = r.order_item_id ");
    sql.append("LEFT JOIN ( ");
    sql.append("    SELECT ");
    sql.append("rt.review_id, ");
    sql.append("    LISTAGG(t.tag_id, ',')      WITHIN GROUP (ORDER BY rt.sort_order, t.tag_id) AS tag_ids, ");
    sql.append("LISTAGG(t.label, ' | ')     WITHIN GROUP (ORDER BY rt.sort_order, t.tag_id) AS tag_labels ");
    sql.append("FROM review_tag rt ");
    sql.append("JOIN tag t ON t.tag_id = rt.tag_id ");
    sql.append("GROUP BY rt.review_id ");
    sql.append(") tags ");
    sql.append("ON tags.review_id = r.review_id ");
    sql.append("LEFT JOIN ( ");
    sql.append("SELECT product_id, image_id, mime_type ");
    sql.append("FROM ( ");
    sql.append("SELECT pi.*, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY image_order, image_id) rn ");
    sql.append("FROM product_image pi ");
    sql.append(") WHERE rn = 1 ) i ON i.product_id = r.product_id ");
    sql.append("WHERE r.buyer_id = :buyerId ");
    sql.append("ORDER BY r.create_date DESC ");
    sql.append("OFFSET (:pageNo - 1) * :numOfRows ROWS ");
    sql.append("FETCH NEXT :numOfRows ROWS ONLY ");

    Map<String,Object> map = Map.of("buyerId",buyerId,"pageNo", pageNo, "numOfRows", numOfRows);

    //db요청
    List<ReviewList> list = template.query(sql.toString(),map, BeanPropertyRowMapper.newInstance(ReviewList.class));

    return list;
  }


  @Override
  public List<ReviewList> reviewFindAllSeller(Long sellerId, int pageNo, int numOfRows) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    sql.append("r.review_id,r.content,r.score,r.seller_recoyn AS seller_reco_yn,r.create_date AS rcreate,r.update_date AS rupdate, ");
    sql.append("p.title AS title,p.create_date AS pcreate,p.update_date AS pupdate, ");
    sql.append("oi.option_type AS option_type, ");
    sql.append("tags.tag_ids,tags.tag_labels, ");
    sql.append("i.image_id AS product_image_id,i.mime_type AS product_image_mime, " );
    sql.append("m.NICKNAME AS nickname " );
    sql.append("FROM review r ");
    sql.append("LEFT JOIN product p ");
    sql.append("ON p.product_id = r.product_id ");
    sql.append("LEFT JOIN order_items oi ");
    sql.append("ON oi.order_item_id = r.order_item_id ");
    sql.append("LEFT JOIN member m ON m.member_id = r.buyer_id ");
    sql.append("LEFT JOIN ( ");
    sql.append("    SELECT ");
    sql.append("rt.review_id, ");
    sql.append("    LISTAGG(t.tag_id, ',')      WITHIN GROUP (ORDER BY rt.sort_order, t.tag_id) AS tag_ids, ");
    sql.append("LISTAGG(t.label, ' | ')     WITHIN GROUP (ORDER BY rt.sort_order, t.tag_id) AS tag_labels ");
    sql.append("FROM review_tag rt ");
    sql.append("JOIN tag t ON t.tag_id = rt.tag_id ");
    sql.append("GROUP BY rt.review_id ");
    sql.append(") tags ");
    sql.append("ON tags.review_id = r.review_id ");
    sql.append("LEFT JOIN ( ");
    sql.append("SELECT product_id, image_id, mime_type ");
    sql.append("FROM ( ");
    sql.append("SELECT pi.*, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY image_order, image_id) rn ");
    sql.append("FROM product_image pi ");
    sql.append(") WHERE rn = 1 ) i ON i.product_id = r.product_id ");
    sql.append("WHERE p.member_id = :sellerId ");
    sql.append("ORDER BY r.create_date DESC ");
    sql.append("OFFSET (:pageNo - 1) * :numOfRows ROWS ");
    sql.append("FETCH NEXT :numOfRows ROWS ONLY ");

    Map<String,Object> map = Map.of("sellerId",sellerId,"pageNo", pageNo, "numOfRows", numOfRows);

    //db요청
    List<ReviewList> list = template.query(sql.toString(),map, BeanPropertyRowMapper.newInstance(ReviewList.class));

    return list;
  }

  @Override
  public Long getReviewTotalCount(Long buyerId) {
    String sql = "SELECT count(review_id) FROM review WHERE buyer_id =:buyerId ";

    SqlParameterSource param = new MapSqlParameterSource().addValue("buyerId", buyerId);
    Long i = template.queryForObject(sql, param, Long.class);

    return i;
  }

  @Override
  public Long getSellerReviewTotalCount(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT count(review_id) ");
    sql.append("FROM review r ");
    sql.append("LEFT JOIN product p ");
    sql.append("ON p.product_id=r.PRODUCT_ID ");
    sql.append("WHERE p.member_id = :memberId ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("memberId", memberId);
    Long i = template.queryForObject(sql.toString(), param, Long.class);

    return i;
  }

  @Override
  public int deleteByIds(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE ");
    sql.append("FROM review ");
    sql.append(" WHERE review_id = :id ");

    //수동매핑
    SqlParameterSource param = new MapSqlParameterSource().addValue("id",id);

    int rows = template.update(sql.toString(), param);

    return rows;
  }
}
