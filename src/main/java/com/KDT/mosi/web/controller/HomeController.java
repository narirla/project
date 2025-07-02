package com.KDT.mosi.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

  @GetMapping("/")
  public String home(HttpServletRequest request) {
    HttpSession session = request.getSession(false); // 기존 세션 조회

    if (session != null) {
      Object loginMember = session.getAttribute("loginMember");
      log.info("홈 접속 - 로그인 사용자 세션 정보: {}", loginMember);
    } else {
      log.info("홈 접속 - 세션 없음 (비로그인 상태)");
    }

    return "index"; // templates/index.html 렌더링
  }
}
