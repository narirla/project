package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ChatRoomController {
  private final ChatRoomService service;

  @PostMapping("/ensure")
  public Map<String, Object> ensure(@RequestParam("productId") long productId,
                                    @RequestParam("buyerId") long buyerId,
                                    @RequestParam("sellerId") long sellerId) {
    long roomId = service.ensure(productId, buyerId, sellerId);
    return Map.of("roomId", roomId);
  }
}
