package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatService;
import com.KDT.mosi.domain.dto.ChatSendReq;
import com.KDT.mosi.domain.dto.ChatSendRes;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messaging;

  /**
   * 클라가 stomp.send("/app/chat.send", {}, JSON) 하면 이 메서드가 "수신"한다.
   * 여기서 DB 저장 → 브로드캐스트 순서로 처리.
   */
  @MessageMapping("/chat.send")
  public void onMessage(ChatSendReq req){
    // ★ 데모 편의를 위해 senderId를 payload에서 받지만,
    // 실제 운영에선 인증(Principal)으로 식별해야 안전합니다.
    long msgId = chatService.saveMessage(req.roomId(), req.senderId(), req.content(), req.clientMsgId());

    var res = new ChatSendRes(
        msgId,
        req.roomId(),
        req.senderId(),
        req.content(),
        OffsetDateTime.now().toString(),
        req.clientMsgId()
    );

    // 같은 방(roomId)을 구독 중인 모든 클라이언트에게 전송
    messaging.convertAndSend("/topic/room/" + req.roomId(), res);
  }
}
