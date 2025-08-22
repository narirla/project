package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.dto.chat.ChatMessageResponse;
import com.KDT.mosi.domain.dto.chat.ChatRoomReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
  private final ChatRoomService service;

  /** 방보장 : 기존에 방 있는지 조회 */
  @PostMapping("/ensure")
  public Map<String, Object> ensure(@RequestBody ChatRoomReqDto chatRoomReq) {
    log.info("chatRoomReq={}", chatRoomReq);
    long roomId = service.ensure(chatRoomReq.getProductId(), chatRoomReq.getBuyerId(), chatRoomReq.getSellerId());
    return Map.of("roomId", roomId);
  }

  /** 이전 메시지 불러오기 */
  @GetMapping("/{roomId}/messages")
  public ResponseEntity<?> getMessages(
      @PathVariable("roomId") Long roomId,
      @RequestParam(name = "limit", defaultValue = "30") int limit
  ) {
    List<ChatMessageResponse> messages = service.findRecent(roomId);
    log.info("📨 getMessages 호출됨, roomId={}, limit={}", roomId, limit);

    if (messages == null || messages.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList()); // 빈 배열 반환
    }
    return ResponseEntity.ok(messages);
  }

}
