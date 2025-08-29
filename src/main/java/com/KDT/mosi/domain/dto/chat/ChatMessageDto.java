package com.KDT.mosi.domain.dto.chat;

// src/main/java/com/mosi/chat/dto/ChatMessageDto.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
  private Long msgId;
  private Long roomId;
  private Long senderId;
  private String content;
  private LocalDateTime createdAt;
  private boolean read;

  // 새로 추가
  private String type; // TEXT, IMAGE, END_REQUEST, END_CONFIRM, END_CANCEL
}



/**
 public record ChatMessageDto(
 Long msgId,
 Long roomId,
 Long senderId,
 String content,
 LocalDateTime createdAt,
 boolean read,
 String type
 ) {} */
