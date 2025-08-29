package com.KDT.mosi.web.controller.chat;

import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.chat.svc.ChatService;
import com.KDT.mosi.domain.dto.chat.ChatMessageDto;
import com.KDT.mosi.domain.dto.chat.ChatMessageResponse;
import com.KDT.mosi.domain.dto.chat.ReadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

  private final ChatService chatService;
  private final ChatRoomService chatRoomService;
  private final SimpMessagingTemplate messaging;

  /**채팅 메시지 처리(저장 및 방 참여자에게 전송)
   * 클라이언트가 stompClient.send("/app/chat/rooms/{roomId}", {}, JSON) 호출하면
   * 이 메서드가 수신해서 DB 저장 → 브로드캐스트 처리.
   */
  @MessageMapping("/chat/rooms/{roomId}")
  public void onMessage(@DestinationVariable("roomId") Long roomId, ChatMessageDto req) {
    log.info("📩 onMessage: roomId={}, type={}, senderId={}, content={}",
        roomId, req.getType(), req.getSenderId(), req.getContent());

    if ("TEXT".equals(req.getType()) || "IMAGE".equals(req.getType())) {

// ✅ 상태 체크 → CLOSED면 ACTIVE로 바꿔주기
      if (chatRoomService.isClosed(roomId)) {
        chatRoomService.reopenRoom(roomId);
      }

      // (1) DB 저장
      Long msgId = chatService.saveMessage(
          roomId,
          req.getSenderId(),
          req.getContent(),
          "client-" + System.currentTimeMillis()
      );

      // (2) 저장한 메시지 조회
      ChatMessageResponse res = chatService.findMessageWithMember(msgId);


      // (3) 방 참가자에게 전송 (팝업창 열고 있는 경우)
      messaging.convertAndSend("/topic/chat/rooms/" + roomId, res);

      // (4) 📢 추가: 목록 갱신용 push (판매자/구매자 모두)
      Long sellerId = chatRoomService.getSellerId(roomId);
      Long buyerId  = chatRoomService.getBuyerId(roomId);

      if (sellerId != null) {
        messaging.convertAndSend("/topic/chat/rooms/" + sellerId, res);
      }
      if (buyerId != null) {
        messaging.convertAndSend("/topic/chat/rooms/buyer/" + buyerId, res);
      }

    } else if ("END_REQUEST".equals(req.getType())) {
      // ✅ senderId가 이 방의 판매자인지 확인
      if (!chatRoomService.isSeller(roomId, req.getSenderId())) {
        log.warn("🚫 비정상 종료 요청 차단: roomId={}, senderId={}", roomId, req.getSenderId());
        return; // 구매자거나 잘못된 요청이면 무시
      }

      // 👉 종료 요청은 DB 저장 X, 정상적인 판매자 요청이면 브로드캐스트
      messaging.convertAndSend("/topic/chat/rooms/" + roomId, req);

    } else if ("END_CONFIRM".equals(req.getType())) {
      // 👉 종료 확정: DB status → CLOSED
      log.info("📢 END_CONFIRM 수신: roomId={}, senderId={}", roomId, req.getSenderId());
      chatRoomService.closeRoom(roomId);

      // 👉 방 참가자 모두에게 알림
      log.info("📢 END_CONFIRM 브로드캐스트: roomId={}, senderId={}", roomId, req.getSenderId());
      messaging.convertAndSend("/topic/chat/rooms/" + roomId, req);

    } else if ("END_CANCEL".equals(req.getType())) {
      // 👉 종료 거절: 그냥 알림만
      messaging.convertAndSend("/topic/chat/rooms/" + roomId, req);
    }
  }






  /*
  @MessageMapping("/chat/rooms/{roomId}")
  public void onMessage(@DestinationVariable("roomId") Long roomId, ChatMessageDto req) {
    log.info("📩 onMessage called, roomId={}, senderId={}, content={}",
        roomId, req.senderId(), req.content());

    // (1) DB 저장
    Long msgId = chatService.saveMessage(
        roomId,
        req.senderId(),
        req.content(),
        "client-" + System.currentTimeMillis() // 임시 clientMsgId
    );


    // (2) 방금 저장한 메시지를 JOIN해서 조회 (닉네임/프로필 포함)
    ChatMessageResponse res = chatService.findMessageWithMember(msgId);

    // ✅ res 값 로깅
    log.info("✅ ChatMessageResponse 저장 완료: msgId={}, roomId={}, senderId={}, nickname={}, profileImage={}, content={}, createdAt={}",
        res.msgId(),
        res.roomId(),
        res.senderId(),
        res.seller_nickname(),
        res.seller_profileImage(),
        res.content(),
        res.createdAt()
    );

    // (3) 같은 방 구독 중인 모든 클라이언트에게 메시지 전송
    messaging.convertAndSend("/topic/chat/rooms/" + roomId, res);
    // convertAndSend: Object 타입 객체를 Message 타입으로 변환
  }
   */

  /**
   * 읽음 이벤트 수신
   * 클라이언트가 stomp.send("/app/chat/rooms/{roomId}/read", {}, JSON) 호출하면 실행됨
   */
  @MessageMapping("/chat/rooms/{roomId}/read")
  public void onRead(
      @DestinationVariable("roomId") Long roomId,
      ReadEvent req
  ) {
    log.info("👀 onRead called, roomId={}, readerId={}, lastReadMessageId={}",
        roomId, req.getReaderId(), req.getLastReadMessageId());

    // 1) DB 반영 (읽음 처리)
    int updated = chatService.markAsRead(
        roomId,
        req.getReaderId(),
        req.getLastReadMessageId()
    );
    log.info("📖 읽음 처리 완료: {}건 업데이트됨", updated);

    // 2) 그대로 브로드캐스트 (상대 클라이언트에게 전달)
    messaging.convertAndSend(
        "/topic/chat/rooms/" + roomId + "/read",
        req
    );
  }



}
