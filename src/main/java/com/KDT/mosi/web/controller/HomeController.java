package com.KDT.mosi.web.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class HomeController {

  @GetMapping("/")
  public String home(HttpSession session,
                     @RequestParam(value = "role", required = false) String role,
                     Model model) {

    if (role != null) {
      session.setAttribute("loginRole", role);
      log.info("✅ 메인화면 접속 시 role 파라미터 반영: {}", role);
    }

    String loginRole = (String) session.getAttribute("loginRole");
    log.info("✅ 현재 세션 loginRole: {}", loginRole);

    model.addAttribute("loginRole", loginRole); // ✅ index.html에서 바로 사용
    return "index";
  }


}
