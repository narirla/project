package com.KDT.mosi.domain.dto.chat;

// src/main/java/com/mosi/chat/dto/ChatMessageDto.java
import java.time.LocalDateTime;

public record ChatMessageDto(
    Long msgId,
    Long roomId,
    Long senderId,
    String content,
    LocalDateTime createdAt,
    boolean read
) {}
