package com.KDT.mosi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MypageRedirectController {
  @GetMapping("/mypage/redirect")
  public String redirectToBuyerMypage() {
    return "redirect:/mypage/buyer";
  }
}
