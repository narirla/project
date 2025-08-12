package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class PasswordVerificationController {

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;

  // 비밀번호 입력 폼
  @GetMapping("/verify-password")
  public String verifyPasswordForm(HttpSession session){
    log.debug("GET /members/verify-password 요청 수신");
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      log.warn("비밀번호 인증 폼 접근 거부: 로그인 세션 없음");
      return "redirect:/login";
    }
    log.debug("비밀번호 인증 폼 진입 허용: memberId={}, email={}",
        loginMember.getMemberId(), loginMember.getEmail());
    return "member/verifyPassword"; // /templates/member/verifyPassword.html
  }

  // 비밀번호 검증 처리(동기)
  @PostMapping("/verify-password")
  public String verifyPassword(@RequestParam("password") String password,
                               HttpSession session,
                               RedirectAttributes ra) {
    log.debug("POST /members/verify-password 요청 수신(동기)");
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      log.warn("비밀번호 인증 실패(동기): 로그인 세션 없음");
      return "redirect:/login";
    }

    log.info("비밀번호 인증 시도(동기): memberId={}, email={}",
        loginMember.getMemberId(), loginMember.getEmail());

    Optional<Member> opt = memberSVC.findById(loginMember.getMemberId());
    if (opt.isEmpty()){
      log.warn("비밀번호 인증 실패(동기): DB에서 회원 정보 없음 - memberId={}", loginMember.getMemberId());
      ra.addFlashAttribute("error","회원 정보를 찾을 수 없습니다.");
      return "redirect:/login";
    }

    String encodePw = opt.get().getPasswd();
    boolean ok = passwordEncoder.matches(password, encodePw);
    log.info("비밀번호 인증 결과(동기): memberId={}, ok={}", loginMember.getMemberId(), ok);

    // 동기 검증 성공 분기 내
    if (ok){
      session.setAttribute("passwordVerified", true);
      session.setAttribute("passwordVerifiedAt", System.currentTimeMillis());
      log.debug("세션에 passwordVerified=true, passwordVerifiedAt 저장 완료");

      String intended = (String) session.getAttribute("INTENDED_URI");
      session.removeAttribute("INTENDED_URI");

      String fallback = "/members/" + loginMember.getMemberId() + "/edit";
      return "redirect:" + (intended != null ? intended : fallback);
    }
    else {
      log.warn("비밀번호 불일치(동기): memberId={}", loginMember.getMemberId());
      ra.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
      return "redirect:/members/verify-password";
    }
  }

  // 비밀번호 검증(AJAX)
  @PostMapping("/api/verify-password")
  @ResponseBody
  public Map<String, Object> verifyPasswordAjax(@RequestParam("password") String password,
                                                HttpSession session) {
    log.debug("POST /members/api/verify-password 요청 수신(AJAX)");
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      log.warn("비밀번호 인증 실패(AJAX): 로그인 세션 없음");
      return Map.of("result","NG", "reason", "NO_LOGIN");
    }

    log.info("비밀번호 인증 시도(AJAX): memberId={}, email={}",
        loginMember.getMemberId(), loginMember.getEmail());

    Optional<Member> opt = memberSVC.findById(loginMember.getMemberId());
    if (opt.isEmpty()) {
      log.warn("비밀번호 인증 실패(AJAX): DB에서 회원 정보 없음 - memberId={}", loginMember.getMemberId());
      return Map.of("result","NG", "reason", "NO_MEMBER");
    }

    boolean ok = passwordEncoder.matches(password, opt.get().getPasswd());
    log.info("비밀번호 인증 결과(AJAX): memberId={}, ok={}", loginMember.getMemberId(), ok);

    if (ok) {
      session.setAttribute("passwordVerified", true);
      session.setAttribute("passwordVerifiedAt", System.currentTimeMillis());
      String intended = (String) session.getAttribute("INTENDED_URI");
      session.removeAttribute("INTENDED_URI");
      return Map.of("result","OK","intended", intended);
    }
    else {
      log.warn("비밀번호 불일치(AJAX): memberId={}", loginMember.getMemberId());
      return Map.of("result","NG", "reason", "MISMATCH");
    }
  }
}
