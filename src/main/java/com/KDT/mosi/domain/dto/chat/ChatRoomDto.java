package com.KDT.mosi.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
  //채팅방 정보 캐리어(구매자가 문의하기 클릭하면 판매자에게 알람 알려주기 위함)
  private Long roomId;
  private Long buyerId;
  private Long sellerId;
  private Long productId;

}
