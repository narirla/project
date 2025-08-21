//채팅방 팝업 시 상대방 닉네임, 프로필 사진 가져오는 용도

package com.KDT.mosi.domain.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long msgId,
    Long roomId,
    Long senderId,
    String content,
    LocalDateTime createdAt,
    boolean read,
    String nickname,     // member 테이블 조인
    byte[] profileImage  // member 테이블 조인
) {}
