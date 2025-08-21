package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.dto.chat.ChatRoomListDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatRoomListController {

  private final ChatRoomService chatRoomService;

  /** 판매자 채팅방 목록 페이지 */
  @GetMapping
  public String roomListPage(HttpSession session, Model model) {
    log.info("session.memberId값={}", session.getAttribute("loginMemberId"));
    // 세션에서 로그인 회원 ID 꺼내기
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      return "redirect:/login"; // 로그인 안 됐으면 로그인 페이지로
    }

    List<ChatRoomListDto> rooms = chatRoomService.findBySellerId(memberId);

    model.addAttribute("rooms", rooms);
    model.addAttribute("memberId", memberId); // → HTML에서 data-member-id 로 내려줌

    return "chat/chatList_seller";
  }

  /** Ajax/REST 요청 */
  @GetMapping("/api")
  @ResponseBody
  public List<ChatRoomListDto> roomListApi(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.findBySellerId(memberId);

    // 로그는 필요한 정보만 추려서 찍기
    rooms.forEach(room ->
        log.info("📋 [판매자:{}] 채팅방ID={}, 상품ID={}, 구매자={}, 마지막메시지={}, 이미지크기={}",
            memberId,
            room.getRoomId(),
            room.getProductId(),
            room.getBuyerId(),
            room.getLastMessage(),
            room.getProductImage() != null ? room.getProductImage().length : 0)
    );

    return rooms;
  }

}


