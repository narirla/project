package com.KDT.mosi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "PRODUCT_COURSE_POINT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCoursePoint {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_course_point_seq")
  @SequenceGenerator(name = "product_course_point_seq", sequenceName = "product_course_point_id_seq", allocationSize = 1)
  @Column(name = "COURSE_POINT_ID")
  private Long coursePointId;

  @Column(name = "PRODUCT_ID", nullable = false)
  private Long productId;

  @Column(name = "POINT_ORDER", nullable = false)
  private Integer pointOrder;

  @Column(name = "LATITUDE", precision = 9, scale = 6, nullable = false)
  private BigDecimal latitude;

  @Column(name = "LONGITUDE", precision = 9, scale = 6, nullable = false)
  private BigDecimal longitude;

  @Column(name = "DESCRIPTION", length = 500)
  private String description;

  @Column(name = "CREATED_AT")
  private Timestamp createdAt;
}