package com.KDT.mosi.domain.entity.cart;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "CART_ITEMS")
@Data
@NoArgsConstructor
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_items_seq")
  @SequenceGenerator(name = "cart_items_seq", sequenceName = "CART_ITEMS_SEQ", allocationSize = 1)
  private Long cartItemId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CART_ID")
  private Cart cart;

  private Long buyerId;
  private Long sellerId;
  private Long productId;
  private String optionType;
  private Integer quantity;
  private Integer originalPrice;
  private Integer salePrice;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}