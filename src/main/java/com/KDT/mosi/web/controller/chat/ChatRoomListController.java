package com.KDT.mosi.web.controller.chat;


import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.dto.ChatRoomListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chatList")
public class ChatRoomListController {

  private final ChatRoomService chatRoomService;


  @GetMapping
  public String chatList(Model model) {
    // DB에서 채팅방 목록 조회 후 model에 담음
//    List<ChatRoomList> rooms = chatRoomListSVC.findAll(sellerId);
//    model.addAttribute("rooms", rooms);

    // templates/chat/chatList.html 로 이동
    return "chat/chatList_seller";
  }

//  /** 판매자 채팅방 목록 페이지 진입 */
//  @GetMapping
//  public String roomListPage(@RequestParam("sellerId") Long sellerId, Model model) {
//    List<ChatRoomListDto> rooms = chatRoomService.findBySellerId(sellerId);
//    model.addAttribute("rooms", rooms);
//    model.addAttribute("sellerId", sellerId);
//    return "chat/chatList_seller"; // templates/chat/roomList.html
//  }
//
  /** Ajax/REST 요청용 API */
  @GetMapping("/api")
  @ResponseBody
  public List<ChatRoomListDto> roomListApi(@RequestParam("sellerId") long sellerId) {
    return chatRoomService.findBySellerId(sellerId);
  }

}
