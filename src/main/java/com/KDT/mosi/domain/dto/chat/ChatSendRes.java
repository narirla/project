package com.KDT.mosi.domain.dto.chat;

public record ChatSendRes(
    Long msgId,
    Long roomId,
    Long senderId,
    String content,
    String createdAt,   // ISO-8601 문자열
    String clientMsgId
) {}
