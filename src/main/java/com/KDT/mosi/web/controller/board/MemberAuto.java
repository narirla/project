package com.KDT.mosi.web.controller.board;

import com.KDT.mosi.web.form.login.LoginMember;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dev")
@RequiredArgsConstructor
public class MemberAuto {

  @GetMapping("/test-login")
  public String testLogin(
      HttpSession session,
      Model model
  ) {
    // 예시로 Member 엔티티를 직접 new 하지 않고, LoginMember DTO에 id만 담음
    LoginMember loginMember = new LoginMember(1L,"a","minjun");
    // 세션에 저장
    session.setAttribute("loginMember", loginMember);
    return "redirect:/";
  }
}
