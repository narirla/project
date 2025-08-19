package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductImageDAOImpl implements ProductImageDAO {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ProductImageDAOImpl(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private final RowMapper<ProductImage> rowMapper = (rs, rowNum) -> {
    ProductImage pi = new ProductImage();
    pi.setImageId(rs.getLong("IMAGE_ID"));

    // productId 가져와 Product 객체 세팅
    Long productId = rs.getLong("PRODUCT_ID");
    Product product = new Product();
    product.setProductId(productId);
    pi.setProduct(product);

    pi.setImageData(rs.getBytes("IMAGE_DATA"));
    pi.setImageOrder(rs.getInt("IMAGE_ORDER"));
    pi.setFileName(rs.getString("FILE_NAME"));
    pi.setFileSize(rs.getLong("FILE_SIZE"));
    pi.setMimeType(rs.getString("MIME_TYPE"));
    pi.setUploadTime(rs.getDate("UPLOAD_TIME"));
    return pi;
  };

  @Override
  public List<ProductImage> findByProductId(Long productId) {
    String sql = "SELECT * FROM PRODUCT_IMAGE WHERE PRODUCT_ID = :productId ORDER BY IMAGE_ORDER";
    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);
    return jdbcTemplate.query(sql, params, rowMapper);
  }

  @Override
  public int insert(ProductImage productImage) {
    String sql = "INSERT INTO product_image (" +
        "image_id, product_id, image_data, image_order, file_name, file_size, mime_type, upload_time) " +
        "VALUES (PRODUCT_IMAGE_IMAGE_ID_SEQ.NEXTVAL, :productId, :imageData, :imageOrder, :fileName, :fileSize, :mimeType, SYSDATE)";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    MapSqlParameterSource params = new MapSqlParameterSource();

    // ⭐⭐⭐ 최종 해결책: MapSqlParameterSource로 수동 매핑 및 NULL 값 처리 ⭐⭐⭐
    // product 객체에서 productId를 직접 가져와서 설정합니다.
    params.addValue("productId", productImage.getProduct().getProductId());

    // NOT NULL 컬럼들에 NULL이 삽입되지 않도록 처리합니다.
    params.addValue("imageData", productImage.getImageData());
    params.addValue("imageOrder", productImage.getImageOrder());
    params.addValue("fileName", productImage.getFileName() != null ? productImage.getFileName() : "");
    params.addValue("fileSize", productImage.getFileSize() != null ? productImage.getFileSize() : 0L);
    params.addValue("mimeType", productImage.getMimeType() != null ? productImage.getMimeType() : "");

    return jdbcTemplate.update(sql, params, keyHolder, new String[]{"image_id"});
  }

  @Override
  public int delete(Long imageId) {
    String sql = "DELETE FROM PRODUCT_IMAGE WHERE IMAGE_ID = :imageId";
    Map<String, Object> params = new HashMap<>();
    params.put("imageId", imageId);
    return jdbcTemplate.update(sql, params);
  }

  @Override
  public int deleteByProductId(Long productId) {
    String sql = "DELETE FROM PRODUCT_IMAGE WHERE PRODUCT_ID = :productId";
    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);
    return jdbcTemplate.update(sql, params);
  }

  @Override
  public Optional<ProductImage> findById(Long imageId) {
    String sql = "SELECT * FROM PRODUCT_IMAGE WHERE IMAGE_ID = :imageId";
    Map<String, Object> params = new HashMap<>();
    params.put("imageId", imageId);

    List<ProductImage> results = jdbcTemplate.query(sql, params, rowMapper);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }
}