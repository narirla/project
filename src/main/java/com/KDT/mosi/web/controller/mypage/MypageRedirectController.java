package com.KDT.mosi.web.controller.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 구매자 마이페이지 기본 경로 리디렉션 컨트롤러
 */
@Controller
@RequestMapping("/mypage")
public class MypageRedirectController {
  @GetMapping
  public String redirectToBuyerMypage() {
    return "redirect:/mypage/buyer";
  }
}
