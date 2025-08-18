package com.KDT.mosi.domain.entity.cart;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CART")
@Data
@NoArgsConstructor
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_seq")
  @SequenceGenerator(name = "cart_seq", sequenceName = "CART_SEQ", allocationSize = 1)
  private Long cartId;

  private Long buyerId;
  private Integer totalPrice = 0;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}