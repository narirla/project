package com.KDT.mosi.domain.entity.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ORDER_ITEMS")
@Data
@NoArgsConstructor
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq")
  @SequenceGenerator(name = "order_items_seq", sequenceName = "ORDER_ITEMS_SEQ", allocationSize = 1)
  private Long orderItemId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_ID")
  private Order order;

  private Long productId;
  private Long sellerId;
  private Integer quantity;
  private Integer originalPrice;
  private Integer salePrice;
  private String request;
  private String optionType;
  private String reviewed;
}