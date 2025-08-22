//채팅방 팝업 시 상대방 닉네임, 프로필 사진 가져오는 용도

package com.KDT.mosi.domain.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long msgId,
    Long roomId,
    Long senderId,
    String content,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    boolean read,
    String seller_nickname,     // member 테이블 조인
    String buyer_nickname,     // member 테이블 조인
    byte[] seller_profileImage,  // member 테이블 조인
    byte[] buyer_profileImage  // member 테이블 조인
) {}
