package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional
public class ProductDAOImpl implements ProductDAO {

  private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);
  private final NamedParameterJdbcTemplate jdbcTemplate;

  // 생성자 주입
  public ProductDAOImpl(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // 시퀀스 값 가져오기
  public Long getNextProductId() {
    String sql = "SELECT PRODUCT_PRODUCT_ID_SEQ.NEXTVAL FROM DUAL";
    return jdbcTemplate.getJdbcOperations().queryForObject(sql, Long.class);
  }

  // RowMapper 구현 - 수동 매핑
  private static final class ProductRowMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
      Product p = new Product();
      p.setProductId(rs.getLong("product_id"));
      Member member = new Member();
      member.setMemberId(rs.getLong("member_id"));
      p.setMember(member);
      p.setCategory(rs.getString("category"));
      p.setTitle(rs.getString("title"));
      p.setGuideYn(rs.getString("guide_yn"));
      p.setNormalPrice(rs.getInt("normal_price"));
      p.setGuidePrice(rs.getInt("guide_price"));
      p.setSalesPrice(rs.getInt("sales_price"));
      p.setSalesGuidePrice(rs.getInt("sales_guide_price"));
      p.setTotalDay(rs.getInt("total_day"));
      p.setTotalTime(rs.getInt("total_time"));
      p.setReqMoney(rs.getInt("req_money"));
      p.setSleepInfo(rs.getString("sleep_info"));
      p.setTransportInfo(rs.getString("transport_info"));
      p.setFoodInfo(rs.getString("food_info"));
      p.setReqPeople(rs.getString("req_people"));
      p.setTarget(rs.getString("target"));
      p.setStucks(rs.getString("stucks"));
      p.setDescription(rs.getString("description"));
      p.setDetail(rs.getString("detail"));
      p.setFileName(rs.getString("file_name"));
      p.setFileType(rs.getString("file_type"));
      p.setFileSize(rs.getLong("file_size"));
      p.setFileData(rs.getBytes("file_data"));
      p.setPriceDetail(rs.getString("price_detail"));
      p.setGpriceDetail(rs.getString("gprice_detail"));
      p.setStatus(rs.getString("status"));
      p.setCreateDate(rs.getDate("create_date"));
      p.setUpdateDate(rs.getDate("update_date"));
      // productImages는 별도 로직 필요 (생략)
      return p;
    }
  }

  // insert
  @Override
  public Product insert(Product product) {
    StringBuffer sql = new StringBuffer();
    sql.append(" insert into product ( ");
    sql.append(" product_id, member_id, category, title, guide_yn, normal_price, ");
    sql.append(" guide_price, sales_price, sales_guide_price, total_day, total_time, ");
    sql.append(" req_money, sleep_info, transport_info, food_info, req_people, ");
    sql.append(" target, stucks, description, detail, file_name, file_data, file_type, file_size, ");
    sql.append(" price_detail, gprice_detail, status ");
    sql.append(" ) values ( ");
    sql.append(" product_product_id_seq.nextval, :memberId, :category, :title, :guideYn, :normalPrice, ");
    sql.append(" :guidePrice, :salesPrice, :salesGuidePrice, :totalDay, :totalTime, ");
    sql.append(" :reqMoney, :sleepInfo, :transportInfo, :foodInfo, :reqPeople, ");
    sql.append(" :target, :stucks, :description, :detail, :fileName, :fileData, :fileType, :fileSize, ");
    sql.append(" :priceDetail, :gpriceDetail, :status ");
    sql.append(" ) ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("memberId", product.getMember().getMemberId())
        .addValue("category", Optional.ofNullable(product.getCategory()).orElse(null))
        .addValue("title", product.getTitle())
        .addValue("guideYn", product.getGuideYn())
        .addValue("normalPrice", Optional.ofNullable(product.getNormalPrice()).orElse(0)) // ⭐⭐⭐ NullPointerException 해결 ⭐⭐⭐
        .addValue("guidePrice", Optional.ofNullable(product.getGuidePrice()).orElse(0)) // ⭐⭐⭐ NullPointerException 해결 ⭐⭐⭐
        .addValue("salesPrice", Optional.ofNullable(product.getSalesPrice()).orElse(0)) // ⭐⭐⭐ NullPointerException 해결 ⭐⭐⭐
        .addValue("salesGuidePrice", Optional.ofNullable(product.getSalesGuidePrice()).orElse(0)) // ⭐⭐⭐ NullPointerException 해결 ⭐⭐⭐
        .addValue("totalDay", Optional.ofNullable(product.getTotalDay()).orElse(0))
        .addValue("totalTime", Optional.ofNullable(product.getTotalTime()).orElse(0))
        .addValue("reqMoney", Optional.ofNullable(product.getReqMoney()).orElse(0))
        .addValue("sleepInfo", Optional.ofNullable(product.getSleepInfo()).orElse(null))
        .addValue("transportInfo", Optional.ofNullable(product.getTransportInfo()).orElse(null))
        .addValue("foodInfo", Optional.ofNullable(product.getFoodInfo()).orElse(null))
        .addValue("reqPeople", Optional.ofNullable(product.getReqPeople()).orElse(""))
        .addValue("target", Optional.ofNullable(product.getTarget()).orElse(null))
        .addValue("stucks", Optional.ofNullable(product.getStucks()).orElse(null))
        .addValue("description", Optional.ofNullable(product.getDescription()).orElse(null))
        .addValue("detail", Optional.ofNullable(product.getDetail()).orElse(null))
        .addValue("fileName", Optional.ofNullable(product.getFileName()).orElse(""))
        .addValue("fileData", Optional.ofNullable(product.getFileData()).orElse(new byte[0]))
        .addValue("fileType", Optional.ofNullable(product.getFileType()).orElse(""))
        .addValue("fileSize", Optional.ofNullable(product.getFileSize()).orElse(0L))
        .addValue("priceDetail", Optional.ofNullable(product.getPriceDetail()).orElse(null))
        .addValue("gpriceDetail", Optional.ofNullable(product.getGpriceDetail()).orElse(null))
        .addValue("status", product.getStatus());

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(sql.toString(), param, keyHolder, new String[]{"product_id"});
    long productId = keyHolder.getKey().longValue();
    product.setProductId(productId);

    return product;
  }

  // update
  @Override
  public Product update(Product product) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE product SET ")
        .append("member_id=:memberId, category=:category, title=:title, guide_yn=:guideYn, normal_price=:normalPrice, guide_price=:guidePrice, ")
        .append("sales_price=:salesPrice, sales_guide_price=:salesGuidePrice, total_day=:totalDay, total_time=:totalTime, req_money=:reqMoney, ")
        .append("sleep_info=:sleepInfo, transport_info=:transportInfo, food_info=:foodInfo, req_people=:reqPeople, target=:target, stucks=:stucks, ")
        .append("description=:description, detail=:detail, file_name=:fileName, file_type=:fileType, file_size=:fileSize, file_data=:fileData, ")
        .append("price_detail=:priceDetail, gprice_detail=:gpriceDetail, status=:status, create_date=:createDate, update_date=:updateDate ")
        .append("WHERE product_id=:productId");

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("productId", product.getProductId())
        .addValue("memberId", product.getMember().getMemberId())
        .addValue("category", product.getCategory())
        .addValue("title", product.getTitle())
        .addValue("guideYn", product.getGuideYn())
        .addValue("normalPrice", product.getNormalPrice())
        .addValue("guidePrice", product.getGuidePrice())
        .addValue("salesPrice", product.getSalesPrice())
        .addValue("salesGuidePrice", product.getSalesGuidePrice())
        .addValue("totalDay", product.getTotalDay())
        .addValue("totalTime", product.getTotalTime())
        .addValue("reqMoney", product.getReqMoney())
        .addValue("sleepInfo", product.getSleepInfo())
        .addValue("transportInfo", product.getTransportInfo())
        .addValue("foodInfo", product.getFoodInfo())
        .addValue("reqPeople", product.getReqPeople())
        .addValue("target", product.getTarget())
        .addValue("stucks", product.getStucks())
        .addValue("description", product.getDescription())
        .addValue("detail", product.getDetail())
        .addValue("fileName", product.getFileName())
        .addValue("fileType", product.getFileType())
        .addValue("fileSize", product.getFileSize())
        .addValue("fileData", product.getFileData())
        .addValue("priceDetail", product.getPriceDetail())
        .addValue("gpriceDetail", product.getGpriceDetail())
        .addValue("status", product.getStatus())
        .addValue("createDate", product.getCreateDate())
        .addValue("updateDate", product.getUpdateDate());

    jdbcTemplate.update(sql.toString(), params);
    return product;
  }

  // delete
  @Override
  public void delete(Long productId) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM product WHERE product_id = :productId");

    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);

    jdbcTemplate.update(sql.toString(), params);
  }

  // findById
  @Override
  public Optional<Product> findById(Long productId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM product WHERE product_id = :productId");

    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);

    List<Product> list = jdbcTemplate.query(sql.toString(), params, new ProductRowMapper());

    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  @Override
  public List<Product> findByMemberIdWithPaging(Long memberId, int page, int size) {
    int offset = (page - 1) * size;

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM product ")
        .append("WHERE member_id = :memberId ")
        .append("ORDER BY product_id DESC ")
        .append("OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
    // ↑ Oracle 12c 이상에서 사용 가능한 방식입니다.

    Map<String, Object> params = new HashMap<>();
    params.put("memberId", memberId);
    params.put("offset", offset);
    params.put("size", size);

    return jdbcTemplate.query(sql.toString(), params, new ProductRowMapper());
  }

  // 카테고리별 상품 목록
  @Override
  public List<Product> findByCategoryWithPaging(String category, int page, int size) {
    int offset = (page - 1) * size;

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM product WHERE category = :category AND status = '판매중' ");
    sql.append("ORDER BY product_id DESC OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");

    Map<String, Object> params = new HashMap<>();
    params.put("category", category);
    params.put("offset", offset);
    params.put("size", size);

    return jdbcTemplate.query(sql.toString(), params, new ProductRowMapper());
  }

  @Override
  public List<Product> findByMemberIdAndStatusWithPaging(Long memberId, String status, int page, int size) {
    int offset = (page - 1) * size;

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM product WHERE member_id = :memberId AND status = :status ");
    sql.append("ORDER BY product_id DESC OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");

    Map<String, Object> params = new HashMap<>();
    params.put("memberId", memberId);
    params.put("status", status);
    params.put("offset", offset);
    params.put("size", size);

    return jdbcTemplate.query(sql.toString(), params, new ProductRowMapper());
  }

  // findAllByPage (Oracle 기준 페이징 처리)
  @Override
  public List<Product> findAllByPage(int pageNumber, int pageSize) {
    int offset = (pageNumber - 1) * pageSize;

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * FROM product ORDER BY product_id DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");

    Map<String, Object> params = new HashMap<>();
    params.put("offset", offset);
    params.put("limit", pageSize);

    return jdbcTemplate.query(sql.toString(), params, new ProductRowMapper());
  }

  // countAll
  @Override
  public long countAll() {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(*) FROM product");

    return jdbcTemplate.queryForObject(sql.toString(), new HashMap<>(), Long.class);
  }

  // 카테고리별 상품 갯수
  @Override
  public long countByCategory(String category) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(*) FROM product WHERE category = :category AND status = '판매중'");

    Map<String, Object> params = new HashMap<>();
    params.put("category", category);

    return jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
  }

  // 로그인 한 사용자의 등록 상품 갯수
  @Override
  public long countByMemberIdAndStatus(Long memberId, String status) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(*) FROM product WHERE member_id = :memberId AND status = :status");

    Map<String, Object> params = new HashMap<>();
    params.put("memberId", memberId);
    params.put("status", status);

    return jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
  }

  @Override
  public long countByMemberId(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(*) FROM product WHERE member_id = :memberId");

    Map<String, Object> params = new HashMap<>();
    params.put("memberId", memberId);

    return jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
  }

  // ⭐⭐⭐ 새로 추가된 메서드들 구현부 ⭐⭐⭐

  /**
   * 새로운 상품 이미지 리스트를 저장합니다.
   * @param images 저장할 ProductImage 리스트
   */
  @Override
  public void saveImages(List<ProductImage> images) {
    String sql = "INSERT INTO product_image (image_id, product_id, file_name, file_size, mime_type, image_order, image_data, create_date, update_date) " +
        "VALUES (product_image_image_id_seq.nextval, :productId, :fileName, :fileSize, :mimeType, :imageOrder, :imageData, sysdate, sysdate)";

    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(images.toArray());
    jdbcTemplate.batchUpdate(sql, batch);
  }

  /**
   * 주어진 이미지 ID 리스트에 해당하는 이미지를 삭제합니다.
   * @param imageIds 삭제할 이미지 ID 리스트
   */
  @Override
  public void deleteImagesByIds(List<Long> imageIds) {
    if (imageIds.isEmpty()) return;

    String sql = "DELETE FROM product_image WHERE image_id IN (:imageIds)";
    Map<String, List<Long>> params = new HashMap<>();
    params.put("imageIds", imageIds);
    jdbcTemplate.update(sql, params);
  }

  /**
   * 특정 상품에 속한 이미지 중 가장 높은 이미지 순서(imageOrder)를 조회합니다.
   * @param productId 상품 ID
   * @return 가장 높은 imageOrder 값 또는 이미지가 없을 경우 null
   */
  @Override
  public Integer findMaxImageOrderByProductId(Long productId) {
    String sql = "SELECT MAX(image_order) FROM product_image WHERE product_id = :productId";
    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);

    return jdbcTemplate.queryForObject(sql, params, Integer.class);
  }
}