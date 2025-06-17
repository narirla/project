package com.KDT.mosi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "PRODUCT_IMAGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_image_seq")
  @SequenceGenerator(name = "product_image_seq", sequenceName = "product_image_id_seq", allocationSize = 1)
  @Column(name = "IMAGE_ID")
  private Long imageId;

  @Column(name = "PRODUCT_ID", nullable = false)
  private Long productId;

  @Lob
  @Column(name = "IMAGE_DATA", nullable = false)
  private byte[] imageData;

  @Column(name = "IMAGE_ORDER")
  private Integer imageOrder;

  @Column(name = "FILE_NAME", length = 255)
  private String fileName;

  @Column(name = "FILE_SIZE")
  private Long fileSize;

  @Column(name = "MIME_TYPE", length = 50)
  private String mimeType;

  @Column(name = "UPLOAD_TIME")
  private Timestamp uploadTime;
}