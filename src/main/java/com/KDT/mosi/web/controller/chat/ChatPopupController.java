package com.KDT.mosi.web.controller.chat;

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

  @GetMapping("/popup")
  public String chatPopup(@RequestParam("roomId") Long roomId, Principal principal, Model model) {

    // 로그인한 사용자 ID (예시: principal에서 이메일 → memberId 조회)
    Long senderId = memberSVC.findByEmail(principal.getName()).orElseThrow().getMemberId();

    model.addAttribute("roomId", roomId);
    model.addAttribute("senderId", senderId);
    return "chat/popup_real.html"; // templates/chat/popup_real.html
  }
}
