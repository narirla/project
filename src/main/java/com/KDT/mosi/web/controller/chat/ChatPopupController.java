package com.KDT.mosi.web.controller.chat;

import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.member.svc.MemberSVCImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatPopupController {

  private final MemberSVCImpl memberSVC;
  private final ChatRoomService chatRoomService;

  @GetMapping("/popup")
  public String chatPopup(@RequestParam("roomId") Long roomId,
                          Principal principal,
                          Model model) {

    // 로그인 사용자 id
    Long senderId = memberSVC.findByEmail(principal.getName())
        .orElseThrow()
        .getMemberId();

    // roomId로 팝업에 필요한 정보 조회
    var popupInfo = chatRoomService.getPopupInfo(roomId);

    // 상품 이미지 (BLOB → Base64)
    String productThumbBase64 = null;
    if (popupInfo.getProductImage() != null) {
      productThumbBase64 = "data:image/jpeg;base64," +
          java.util.Base64.getEncoder().encodeToString(popupInfo.getProductImage());
    }

    // 상대방 닉네임 (내가 buyer면 partner는 seller, 반대면 buyer)
    String partnerNickname = popupInfo.getBuyerNickname();
    if (popupInfo.getBuyerId().equals(senderId)) {
      partnerNickname = popupInfo.getSellerNickname();
    }

    // 모델에 담기
    model.addAttribute("roomId", popupInfo.getRoomId());
    model.addAttribute("senderId", senderId);
    model.addAttribute("partnerNickname", partnerNickname);
    model.addAttribute("productTitle", popupInfo.getProductTitle());
    model.addAttribute("productPrice", popupInfo.getProductPrice());
    model.addAttribute("productThumbBase64", productThumbBase64);

    log.info("📩 popup opened: roomId={}, sender={}, partner={}",
        roomId, senderId, partnerNickname);

    return "chat/popup"; // ✅ templates/chat/popup.html
  }
}
