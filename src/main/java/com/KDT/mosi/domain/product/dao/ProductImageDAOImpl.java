package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductImageDAOImpl implements ProductImageDAO {

  private final NamedParameterJdbcTemplate template;

  private RowMapper<ProductImage> imageMapper = (rs, rowNum) -> {
    ProductImage image = new ProductImage();
    image.setImageId(rs.getLong("image_id"));
    image.setProductId(rs.getLong("product_id"));
    image.setImageData(rs.getBytes("image_data"));
    image.setImageOrder(rs.getInt("image_order"));
    image.setFileName(rs.getString("file_name"));
    image.setFileSize(rs.getLong("file_size"));
    image.setMimeType(rs.getString("mime_type"));
    Timestamp uploadTime = rs.getTimestamp("upload_time");
    image.setUploadTime(uploadTime != null ? uploadTime : null);
    return image;
  };

  @Override
  public ProductImage save(ProductImage productImage) {
    String sql = "INSERT INTO product_image (" +
        "image_id, product_id, image_data, image_order, file_name, file_size, mime_type, upload_time) " +
        "VALUES (PRODUCT_IMAGE_ID_SEQ.nextval, :productId, :imageData, :imageOrder, :fileName, :fileSize, :mimeType, :uploadTime)";
    SqlParameterSource param = new BeanPropertySqlParameterSource(productImage);
    template.update(sql, param);
    return productImage;
  }

  @Override
  public List<ProductImage> findByProductId(Long productId) {
    String sql = "SELECT * FROM product_image WHERE product_id = :productId ORDER BY image_order";
    return template.query(sql, Map.of("productId", productId), imageMapper);
  }

  @Override
  public Optional<ProductImage> findById(Long imageId) {
    String sql = "SELECT * FROM product_image WHERE image_id = :imageId";
    try {
      ProductImage image = template.queryForObject(sql, Map.of("imageId", imageId), imageMapper);
      return Optional.of(image);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public int update(ProductImage productImage) {
    String sql = "UPDATE product_image SET " +
        "product_id = :productId, image_data = :imageData, image_order = :imageOrder, file_name = :fileName, " +
        "file_size = :fileSize, mime_type = :mimeType, upload_time = :uploadTime WHERE image_id = :imageId";
    SqlParameterSource param = new BeanPropertySqlParameterSource(productImage);
    return template.update(sql, param);
  }

  @Override
  public void deleteById(Long imageId) {
    String sql = "DELETE FROM product_image WHERE image_id = :imageId";
    template.update(sql, Map.of("imageId", imageId));
  }

  @Override
  public void deleteByProductId(Long productId) {
    String sql = "DELETE FROM product_image WHERE product_id = :productId";
    template.update(sql, Map.of("productId", productId));
  }
}