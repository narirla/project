package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.ProductCoursePoint;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductCoursePointDAOImpl implements ProductCoursePointDAO {

  private final NamedParameterJdbcTemplate template;

  private RowMapper<ProductCoursePoint> coursePointMapper = (rs, rowNum) -> {
    ProductCoursePoint point = new ProductCoursePoint();
    point.setCoursePointId(rs.getLong("course_point_id"));
    point.setProductId(rs.getLong("product_id"));
    point.setPointOrder(rs.getInt("point_order"));
    point.setLatitude(rs.getBigDecimal("latitude"));
    point.setLongitude(rs.getBigDecimal("longitude"));
    point.setDescription(rs.getString("description"));
    Timestamp createdAt = rs.getTimestamp("created_at");
    point.setCreatedAt(createdAt != null ? createdAt : null);
    return point;
  };

  @Override
  public ProductCoursePoint save(ProductCoursePoint coursePoint) {
    String sql = "INSERT INTO product_course_point (" +
        "course_point_id, product_id, point_order, latitude, longitude, description, created_at) " +
        "VALUES (PRODUCT_COURSE_POINT_ID_SEQ.nextval, :productId, :pointOrder, :latitude, :longitude, :description, :createdAt)";

    SqlParameterSource param = new BeanPropertySqlParameterSource(coursePoint);
    template.update(sql, param);
    return coursePoint;
  }

  @Override
  public List<ProductCoursePoint> findByProductId(Long productId) {
    String sql = "SELECT * FROM product_course_point WHERE product_id = :productId ORDER BY point_order";
    return template.query(sql, Map.of("productId", productId), coursePointMapper);
  }

  @Override
  public Optional<ProductCoursePoint> findById(Long coursePointId) {
    String sql = "SELECT * FROM product_course_point WHERE course_point_id = :id";
    try {
      ProductCoursePoint point = template.queryForObject(sql, Map.of("id", coursePointId), coursePointMapper);
      return Optional.of(point);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public int update(ProductCoursePoint coursePoint) {
    String sql = "UPDATE product_course_point SET " +
        "product_id = :productId, point_order = :pointOrder, latitude = :latitude, longitude = :longitude, " +
        "description = :description, created_at = :createdAt WHERE course_point_id = :coursePointId";

    SqlParameterSource param = new BeanPropertySqlParameterSource(coursePoint);
    return template.update(sql, param);
  }

  @Override
  public void deleteById(Long coursePointId) {
    String sql = "DELETE FROM product_course_point WHERE course_point_id = :id";
    template.update(sql, Map.of("id", coursePointId));
  }

  @Override
  public void deleteByProductId(Long productId) {
    String sql = "DELETE FROM product_course_point WHERE product_id = :productId";
    template.update(sql, Map.of("productId", productId));
  }
}