package com.KDT.mosi.domain.entity.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
  @SequenceGenerator(name = "orders_seq", sequenceName = "ORDERS_SEQ", allocationSize = 1)
  private Long orderId;

  private String orderCode;
  private Long buyerId;
  private Long totalPrice;
  private String specialRequest;
  private LocalDateTime orderDate;
  private String status;
}