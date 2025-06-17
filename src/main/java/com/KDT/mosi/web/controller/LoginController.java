package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.web.form.member.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class LoginController {

  private final MemberSVC memberSVC;

  //로그인 폼
  @GetMapping({"/login", "/user/login"})
  public String loginForm(Model model){
    model.addAttribute("form", new LoginForm());
    return "login/loginForm";  // templates/login/loginForm.html
  }

  //로그인 처리
  @PostMapping("/login")
  public String login(
      @Valid @ModelAttribute("form") LoginForm loginForm,
      BindingResult bindingResult,
      HttpServletRequest request
  ){
    if (bindingResult.hasErrors()){
      return "login/loginForm";
    }

    Optional<Member> optional = memberSVC.findByEmail(loginForm.getEmail());

    if (optional.isEmpty() || !optional.get().getPasswd().equals(loginForm.getPasswd())){
      bindingResult.reject("loginFail", "이메일 또는 비밀번호가 올바르지 않습니다.");
      return "login/loginForm";
    }

    //로그인 성공 -> 세션 생성
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", optional.get());

    return "redirect:/";
  }

  //로그아웃 처리
  @PostMapping("/logout")
  public String logout(HttpServletRequest request){
    HttpSession session = request.getSession(false);
    if (session != null){
      session.invalidate();   //세션 제거
    }
    return "redirect:/";
  }

}
