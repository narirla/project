package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductDAOImpl implements ProductDAO {

  private final NamedParameterJdbcTemplate template;

  // 수동매핑: PRODUCT 테이블 전체 컬럼 매핑
  private RowMapper<Product> productRowMapper = (rs, rowNum) -> {
    Product product = new Product();

    product.setProductId(rs.getLong("product_id"));
    product.setMemberId(rs.getLong("member_id"));
    product.setName(rs.getString("name"));
    product.setCategory(rs.getString("category"));
    product.setPrice(rs.getInt("price"));
    product.setStatus(rs.getString("status"));
    product.setDescription(rs.getString("description"));
    product.setDetail(rs.getString("detail"));
    product.setGuideYn(rs.getString("guide_yn"));
    product.setReqMoney(rs.getInt("req_money"));
    product.setReqPeople(rs.getInt("req_people"));
    product.setAge(rs.getInt("age"));

    product.setFoodInfo(rs.getString("food_info"));
    product.setSleepInfo(rs.getString("sleep_info"));
    product.setStoreInfo(rs.getString("store_info"));
    product.setPromoYn(rs.getString("promo_yn"));
    product.setTransportInfo(rs.getString("transport_info"));

    Timestamp createDate = rs.getTimestamp("create_date");
    product.setCreateDate(createDate != null ? createDate.toLocalDateTime() : null);

    Timestamp updateDate = rs.getTimestamp("update_date");
    product.setUpdateDate(updateDate != null ? updateDate.toLocalDateTime() : null);

    return product;
  };

  // 저장
  @Override
  public Product save(Product product) {
    String sql = "INSERT INTO product (" +
        "product_id, member_id, name, category, price, status, description, detail, " +
        "guide_yn, req_money, req_people, age, food_info, sleep_info, store_info, " +
        "promo_yn, transport_info, create_date, update_date) " +
        "VALUES (PRODUCT_PRODUCT_ID_SEQ.nextval, :memberId, :name, :category, :price, :status, :description, :detail, " +
        ":guideYn, :reqMoney, :reqPeople, :age, :foodInfo, :sleepInfo, :storeInfo, " +
        ":promoYn, :transportInfo, :createDate, :updateDate)";

    SqlParameterSource param = new BeanPropertySqlParameterSource(product);
    template.update(sql, param);

    // Oracle 시퀀스 연동 시 발생하는 PK 반환은 별도 쿼리 필요 (생략)

    return product;
  }

// 페이징 조회
  @Override
  public List<Product> findAll(int pageNo, int numOfRows) {
    String sql = "SELECT * FROM product " +
        "ORDER BY product_id DESC " +
        "OFFSET (:offset) ROWS FETCH NEXT :limit ROWS ONLY";

    int offset = (pageNo - 1) * numOfRows;

    MapSqlParameterSource param = new MapSqlParameterSource()
        .addValue("offset", offset)
        .addValue("limit", numOfRows);

    return template.query(sql, param, productRowMapper);
  }

  // 결제 전 상세 조회 (food_info, sleep_info, store_info, promo_yn, transport_info 제외)
  @Override
  public Optional<Product> findById(Long id) {
    String sql = "SELECT product_id, member_id, name, category, price, status, description, detail, guide_yn, " +
        "req_money, req_people, age, create_date, update_date " +
        "FROM product WHERE product_id = :id";

    SqlParameterSource param = new MapSqlParameterSource("id", id);

    RowMapper<Product> mapperExcludingInfo = (rs, rowNum) -> {
      Product product = new Product();

      product.setProductId(rs.getLong("product_id"));
      product.setMemberId(rs.getLong("member_id"));
      product.setName(rs.getString("name"));
      product.setCategory(rs.getString("category"));
      product.setPrice(rs.getInt("price"));
      product.setStatus(rs.getString("status"));
      product.setDescription(rs.getString("description"));
      product.setDetail(rs.getString("detail"));
      product.setGuideYn(rs.getString("guide_yn"));
      product.setReqMoney(rs.getInt("req_money"));
      product.setReqPeople(rs.getInt("req_people"));
      product.setAge(rs.getInt("age"));

      // foodInfo, sleepInfo, storeInfo, promoYn, transportInfo 필드는 제외

      Timestamp createDate = rs.getTimestamp("create_date");
      product.setCreateDate(createDate != null ? createDate.toLocalDateTime() : null);

      Timestamp updateDate = rs.getTimestamp("update_date");
      product.setUpdateDate(updateDate != null ? updateDate.toLocalDateTime() : null);

      return product;
    };

    try {
      Product product = template.queryForObject(sql, param, mapperExcludingInfo);
      return Optional.of(product);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  // 결제 후 상세 조회 (food_info, sleep_info, store_info, promo_yn, transport_info 포함)
  @Override
  public Optional<Product> findByIdAfterPay(Long id) {
    String sql = "SELECT product_id, member_id, name, category, price, status, description, detail, guide_yn, " +
        "req_money, req_people, age, food_info, sleep_info, store_info, promo_yn, transport_info, create_date, update_date " +
        "FROM product WHERE product_id = :id AND status = 'Y'";

    SqlParameterSource param = new MapSqlParameterSource("id", id);

    // 기존 productRowMapper 그대로 사용 가능
    try {
      Product product = template.queryForObject(sql, param, productRowMapper);
      return Optional.of(product);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  // 수정
  @Override
  public Product updateById(Long productId, Product product) {
    String sql = "UPDATE product SET " +
        "name = :name, category = :category, price = :price, status = :status, description = :description, " +
        "detail = :detail, guide_yn = :guideYn, req_money = :reqMoney, req_people = :reqPeople, age = :age, " +
        "food_info = :foodInfo, sleep_info = :sleepInfo, store_info = :storeInfo, promo_yn = :promoYn, " +
        "transport_info = :transportInfo, update_date = :updateDate " +
        "WHERE product_id = :productId";

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("name", product.getName())
        .addValue("category", product.getCategory())
        .addValue("price", product.getPrice())
        .addValue("status", product.getStatus())
        .addValue("description", product.getDescription())
        .addValue("detail", product.getDetail())
        .addValue("guideYn", product.getGuideYn())
        .addValue("reqMoney", product.getReqMoney())
        .addValue("reqPeople", product.getReqPeople())
        .addValue("age", product.getAge())
        .addValue("foodInfo", product.getFoodInfo())
        .addValue("sleepInfo", product.getSleepInfo())
        .addValue("storeInfo", product.getStoreInfo())
        .addValue("promoYn", product.getPromoYn())
        .addValue("transportInfo", product.getTransportInfo())
        .addValue("updateDate", product.getUpdateDate())
        .addValue("productId", productId);

    template.update(sql, param);
    return product;
  }

  // 단건 삭제
  @Override
  public void deleteById(Long id) {
    String sql = "DELETE FROM product WHERE product_id = :id";
    template.update(sql, Map.of("id", id));
  }

  // 다중 삭제
  @Override
  public void deleteByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return;

    String sql = "DELETE FROM product WHERE product_id IN (:ids)";
    template.update(sql, Map.of("ids", ids));
  }

  // 총 건수 조회
  @Override
  public long getTotalCount() {
    String sql = "SELECT COUNT(*) FROM product";
    Long count = template.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    return count != null ? count : 0L;
  }

}