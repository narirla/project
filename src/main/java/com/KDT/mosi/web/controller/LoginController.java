package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.web.form.member.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

  private final MemberSVC memberSVC;
  private final BCryptPasswordEncoder passwordEncoder;

  // 로그인 폼 요청
  @GetMapping
  public String loginForm(
      @RequestParam(value = "redirect", required = false) String redirect,
      Model model) {
    model.addAttribute("form", new LoginForm());
    model.addAttribute("redirect", redirect);
    return "login/loginForm";
  }


  // 로그아웃
  @PostMapping("/logout")
  public String logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return "redirect:/";
  }
}
