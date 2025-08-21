package com.KDT.mosi.web.controller.chat;

import com.KDT.mosi.domain.chat.svc.ChatService;
import com.KDT.mosi.domain.dto.chat.ChatMessageDto;
import com.KDT.mosi.domain.dto.chat.ReadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messaging;

  /**채팅 메시지 처리(저장 및 방 참여자에게 전송)
   * 클라이언트가 stompClient.send("/app/chat/rooms/{roomId}", {}, JSON) 호출하면
   * 이 메서드가 수신해서 DB 저장 → 브로드캐스트 처리.
   */
  @MessageMapping("/chat/rooms/{roomId}")
  public void onMessage(@DestinationVariable("roomId") Long roomId, ChatMessageDto req) {
    log.info("📩 onMessage called, roomId={}, senderId={}, content={}",
        roomId, req.senderId(), req.content());

    // (1) DB 저장
    long msgId = chatService.saveMessage(
        roomId,
        req.senderId(),
        req.content(),
        "client-" + System.currentTimeMillis() // 임시 clientMsgId
    );

    // (2) 응답 객체 생성 (DB 저장 결과 반영)
    ChatMessageDto res = new ChatMessageDto(
        msgId,
        roomId,
        req.senderId(),
        req.content(),
        LocalDateTime.now(), // 서버 기준 생성 시각
        false                 // 읽음 여부 기본값
    );

    // (3) 같은 방 구독 중인 모든 클라이언트에게 메시지 전송
    messaging.convertAndSend("/topic/chat/rooms/" + roomId, res);
    // convertAndSend : Object 타입 객체를 Message 타입으로 변환
  }

  /**
   * 읽음 이벤트 수신
   * 클라이언트가 stomp.send("/app/chat/rooms/{roomId}/read", {}, JSON) 호출하면 실행됨
   */
  @MessageMapping("/chat/rooms/{roomId}/read")
  public void onRead(@DestinationVariable("roomId") Long roomId, ReadEvent req) {
    log.info("👀 onRead called, roomId={}, readerId={}, lastReadMessageId={}",
        roomId, req.getReaderId(), req.getLastReadMessageId());

    // DB 반영은 생략, 그대로 브로드캐스트만
    messaging.convertAndSend("/topic/chat/rooms/" + roomId + "/read", req);
  }


}
