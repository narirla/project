package com.KDT.mosi.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadEvent {
  private Long roomId;            // 채팅방 ID
  private Long readerId;          // 읽은 사람 ID
  private Long lastReadMessageId; // 마지막 읽은 메시지 ID
}