package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.member.svc.MemberSVC;
import jakarta.validation.constraints.Email;
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
@RequestMapping("/find")
public class FindController {

  private final MemberSVC memberSVC;
  private final BCryptPasswordEncoder passwordEncoder;

  // ✅ 아이디 찾기 폼
  @GetMapping("/id")
  public String findIdForm() {
    return "member/findIdForm";
  }

  // ✅ 아이디 찾기 처리
  @PostMapping("/id")
  public String findId(
      @RequestParam(required = false) String tel,
      @RequestParam(required = false) @Email String email,
      Model model
  ) {
    String foundEmail = null;

    if (tel != null && !tel.isBlank()) {
      foundEmail = memberSVC.findEmailByTel(tel);
    } else if (email != null && !email.isBlank()) {
      foundEmail = memberSVC.findEmailByEmail(email);
    }

    model.addAttribute("foundEmail", foundEmail);
    return "member/findIdResult";
  }

  // ✅ 비밀번호 재설정 요청 폼 (링크 진입)
  @GetMapping("/pw/reset")
  public String resetPwForm(@RequestParam("email") String email, Model model) {
    model.addAttribute("email", email);
    return "member/resetPwForm";
  }

  // ✅ 비밀번호 재설정 처리
  @PostMapping("/pw/reset")
  public String resetPw(
      @RequestParam("email") String email,
      @RequestParam("newPw") String newPw,
      @RequestParam("newPwConfirm") String newPwConfirm,
      Model model
  ) {
    if (!newPw.equals(newPwConfirm)) {
      model.addAttribute("email", email);
      model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
      return "member/resetPwForm";
    }

    // ✅ 비밀번호 암호화 여기서 수행!
    String encodedPw = passwordEncoder.encode(newPw);

    // ✅ 암호화된 비밀번호로 업데이트
    boolean result = memberSVC.resetPassword(email, encodedPw);
    model.addAttribute("resetSuccess", result);

    return "member/resetPwResult";
  }



}
