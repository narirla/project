package com.KDT.mosi.domain.dto.chat;


import lombok.Data;

@Data
public class ChatRoomReqDto {
  private Long productId;
  private Long buyerId;
  private Long sellerId;
}
