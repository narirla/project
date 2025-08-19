package com.KDT.mosi.domain.dto;


import lombok.Data;

@Data
public class ChatRoomReqDto {
  private Long productId;
  private Long buyerId;
  private Long sellerId;
}
