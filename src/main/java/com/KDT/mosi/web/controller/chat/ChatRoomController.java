package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.dto.ChatMessageDto;
import com.KDT.mosi.domain.dto.ChatRoomReqDto;
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
@RequestMapping("/api/chat")
public class ChatRoomController {
  private final ChatRoomService service;

  /** 기존에 방 있는지 조회 */
  @PostMapping("/rooms/ensure")
  public Map<String, Object> ensure(@RequestBody ChatRoomReqDto chatRoomReq) {
    log.info("chatRoomReq={}", chatRoomReq);
    long roomId = service.ensure(chatRoomReq.getProductId(), chatRoomReq.getBuyerId(), chatRoomReq.getSellerId());
    return Map.of("roomId", roomId);
  }

  /** 이전 메시지 불러오기 */
  @GetMapping("/api/chat/rooms/{roomId}/messages")
  public ResponseEntity<?> getMessages(
      @PathVariable Long roomId,
      @RequestParam(defaultValue = "30") int limit
  ) {
    List<ChatMessageDto> messages = service.findRecent(roomId, limit);
    if (messages == null || messages.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList()); // 빈 배열 반환
    }
    return ResponseEntity.ok(messages);
  }



//  @GetMapping("/api/chat/rooms/{roomId}/messages")
//  public List<ChatMessageDto> history(
//      @PathVariable Long roomId,
//      @RequestParam(defaultValue = "30") int limit,
//      @RequestParam(required=false) Long beforeId
//  ) {
//    return service.findRecent(roomId, limit, beforeId);
//  }


}
