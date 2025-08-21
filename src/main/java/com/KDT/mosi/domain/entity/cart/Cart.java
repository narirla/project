package com.KDT.mosi.domain.entity.cart;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
  private Long totalPrice = 0L;
  
  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;
  
  @UpdateTimestamp
  private LocalDateTime updatedAt;
}