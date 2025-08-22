package com.KDT.mosi.domain.dto.chat;

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

  // 화면 표시용 추가 필드
  private String buyerNickname;
  private String productTitle;
  private byte[] productImage;
  private String lastMessage;

  // 🔔 새 메시지 여부
  private boolean hasNew;
}

