package com.KDT.mosi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
  private Long roomId;
  private Long buyerId;
  private Long sellerId;
  private Long productId;
  private String status;
  private LocalDateTime createdAt;
  // 필요하다면 buyerNickname, productTitle 등도 포함 가능
}

