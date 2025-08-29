package com.KDT.mosi.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column(length = 30, nullable = false)
  private String category;

  @Column(length = 90) // 임시저장 시 NULL 허용을 위해 nullable = false 제거
  private String title;

  @Column(name = "guide_yn", length = 1) // nullable = false 제거
  private String guideYn;

  @Column(name = "normal_price") // nullable = false 제거
  private Integer normalPrice;

  @Column(name = "guide_price") // nullable = false 제거
  private Integer guidePrice;

  @Column(name = "sales_price") // nullable = false 제거
  private Integer salesPrice;

  @Column(name = "sales_guide_price") // nullable = false 제거
  private Integer salesGuidePrice;

  @Column(name = "total_day") // nullable = false 제거
  private Integer totalDay;

  @Column(name = "total_time") // nullable = false 제거
  private Integer totalTime;

  @Column(name = "req_money") // nullable = false 제거
  private Integer reqMoney;

  @Column(name = "sleep_info", length=1) // nullable = false 제거
  private String sleepInfo;

  @Column(name = "transport_info", length=45)
  private String transportInfo;

  @Column(name = "food_info", length=1) // nullable = false 제거
  private String foodInfo;

  @Column(name = "req_people") // nullable = false 제거
  private String reqPeople;

  @Column(name = "target") // nullable = false 제거
  private String target;

  @Column(length=90)
  private String stucks;

  @Column(length=1500) // nullable = false 제거
  private String description;

  @Column(length=3000) // nullable = false 제거
  private String detail;

  @Column(name = "file_name", length=255) // nullable = false 제거
  private String fileName;

  @Column(name = "file_type", length=50) // nullable = false 제거
  private String fileType;

  @Column(name = "file_size") // nullable = false 제거
  private Long fileSize;

  @Lob
  @Column(name="file_data") // nullable = false 제거
  private byte[] fileData;

  @Column(name="price_detail", length=450) // nullable = false 제거
  private String priceDetail;

  @Column(name="gprice_detail", length=450) // nullable = false 제거
  private String gpriceDetail;

  @Column(length=12, nullable=false)
  private String status;

  @Temporal(TemporalType.DATE)
  @Column(name="create_date", nullable=false)
  private Date createDate;

  @Temporal(TemporalType.DATE)
  @Column(name="update_date")
  private Date updateDate;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductImage> productImages;
}