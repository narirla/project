package com.KDT.mosi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
  @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_product_id_seq", allocationSize = 1)
  @Column(name = "PRODUCT_ID")
  private Long productId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "NAME", nullable = false, length = 100)
  private String name;

  @Column(name = "CATEGORY", length = 30)
  private String category;

  @Column(name = "PRICE")
  private Integer price;

  @Column(name = "STATUS", length = 15)
  private String status;

  @Column(name = "DESCRIPTION", length = 500)
  private String description;

  @Column(name = "DETAIL", length = 1000)
  private String detail;

  @Column(name = "GUIDE_YN", length = 1)
  private String guideYn;

  @Column(name = "REQ_MONEY")
  private Integer reqMoney;

  @Column(name = "REQ_PEOPLE")
  private Integer reqPeople;

  @Column(name = "AGE")
  private Integer age;

  @Column(name = "FOOD_INFO", length = 100)
  private String foodInfo;

  @Column(name = "SLEEP_INFO", length = 100)
  private String sleepInfo;

  @Column(name = "STORE_INFO", length = 10)
  private String storeInfo;

  @Column(name = "PROMO_YN", length = 1)
  private String promoYn;

  @Column(name = "TRANSPORT_INFO", length = 100)
  private String transportInfo;

  @Column(name = "CREATE_DATE")
  private LocalDateTime createDate;

  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;

}
