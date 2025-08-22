package com.KDT.mosi.domain.dto.chat;
/**
 * 클라이언트 → 서버로 WebSocket 전송할 페이로드
 * NOTE: 테스트를 쉽게 하려고 senderId도 payload에 포함했지만,
 * 실제 운영에선 인증을 붙이고 서버 측에서 Principal로 식별하세요.
 */
public record ChatSendReq(
    Long roomId,
    Long senderId,
    String content,
    String clientMsgId  // 중복 전송 방지용 클라 생성 ID(임의 UUID)
) {}
