package com.KDT.mosi.web.controller.chat;

// src/main/java/com/mosi/chat/web/ChatHistoryController.java

import com.KDT.mosi.domain.chat.dao.ChatMessageDao;
import com.KDT.mosi.domain.dto.ChatMessageDto;
import com.KDT.mosi.web.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms/{roomId}/messages")
public class ChatHistoryController {

  private final ChatMessageDao dao;

  @GetMapping
  public PageResponse<ChatMessageDto> list(
      @PathVariable long roomId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size
  ){
    long total = dao.countByRoom(roomId);
    List<ChatMessageDto> content = dao.findPageByRoom(roomId, page * size, size);
    return PageResponse.of(content, page, size, total);
  }
}

