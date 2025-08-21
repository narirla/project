package com.KDT.mosi.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatPopupDto {
  //채팅방 팝업 시 채팅방에 띄울 기본 정보들
  private Long roomId;            // 방번호
  private Long buyerId;           // 구매자 ID
  private Long sellerId;          // 판매자 ID
  private String buyerNickname;   // 구매자 닉네임
  private String sellerNickname;  // 판매자 닉네임
  private byte[] productImage;    // 상품 썸네일
  private String productTitle;    // 상품명
  private Long productPrice;      // 상품 가격
}