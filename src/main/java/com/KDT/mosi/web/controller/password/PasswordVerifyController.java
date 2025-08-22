package com.KDT.mosi.web.controller.password;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.web.form.member.PasswordForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class PasswordVerifyController {

  // 재인증 TTL(5분) — ChangePasswordController와 동일 값 유지
  private static final long PASSWORD_VERIFY_TTL_MS = 5 * 60 * 1000L;

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;
  private final SellerPageSVC sellerPageSVC;

  // B안: Bean 주입 없이 로컬 인스턴스 사용
  private final RequestCache requestCache = new HttpSessionRequestCache();

  /** 재인증 폼 GET */
  @GetMapping("/verify-password")
  public String verifyForm(@RequestParam(required=false, name="next", defaultValue="/members/password") String next,
                           HttpSession session,
                           HttpServletRequest request,
                           Model model) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    injectCommonModelForVerify(session, request, model, login);
    model.addAttribute("passwordForm", new PasswordForm());
    model.addAttribute("next", sanitizeNext(next));
    return "member/verifyPassword";
  }

  /** 재인증 처리 POST: 성공 시 next로 리다이렉트 */
  @PostMapping("/verify-password")
  public String verify(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                       BindingResult bindingResult,
                       @RequestParam(required=false, name="next", defaultValue="/members/password") String next,
                       HttpSession session,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       Model model) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    Member dbMember = memberSVC.findById(login.getMemberId()).orElseThrow();
    boolean ok = form.getCurrentPassword()!=null && passwordEncoder.matches(form.getCurrentPassword(), dbMember.getPasswd());
    if (!ok) {
      bindingResult.reject("mismatch", "비밀번호가 일치하지 않습니다.");
      injectCommonModelForVerify(session, request, model, login);
      model.addAttribute("next", sanitizeNext(next));
      return "member/verifyPassword";
    }

    long now = System.currentTimeMillis();

    // 표준/구버전 키 모두 세팅(호환)
    session.setAttribute("PWD_VERIFIED", true);
    session.setAttribute("PWD_VERIFIED_AT", now);
    session.setAttribute("PWD_VERIFY_EXPIRES_AT", now + PASSWORD_VERIFY_TTL_MS);
    session.setAttribute("passwordVerified", true);
    session.setAttribute("passwordVerifiedAt", now);
    session.setAttribute("passwordVerifyExpireAt", now + PASSWORD_VERIFY_TTL_MS);

    // SavedRequest 제거: 예기치 않은 리다이렉트 방지
    requestCache.removeRequest(request, response);

    return "redirect:" + sanitizeNext(next);
  }

  /** 현재 비밀번호 확인 API (단일 경로/GET) */
  @GetMapping("/password/check")                                 // [변경] 단일 경로 + GET
  @ResponseBody
  public Map<String, Object> checkCurrentPassword(
      @RequestParam("currentPassword") String currentPassword,   // [변경] 파라미터명 통일
      HttpSession session
  ) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return Map.of("ok", false);

    String raw = currentPassword == null ? "" : currentPassword.trim();
    boolean ok = !raw.isEmpty() && passwordEncoder.matches(raw, login.getPasswd());

    return Map.of("ok", ok);
  }

  // 공통 주입(verify 화면 전용)
  private void injectCommonModelForVerify(HttpSession session, HttpServletRequest request, Model model, Member login) {
    model.addAttribute("member", login);
    model.addAttribute("activePath", "/members/verify-password");
    model.addAttribute("currentPath", request.getRequestURI());
    Object role = session.getAttribute("loginRole");
    if ("SELLER".equals(role)) {
      model.addAttribute("sellerPage", sellerPageSVC.findByMemberId(login.getMemberId()).orElse(null));
    } else {
      model.addAttribute("sellerPage", null);
    }
  }

  /** 오픈 리다이렉트 방지: 루트 시작 상대경로만 허용 */
  private String sanitizeNext(String next) {
    if (next == null || next.isBlank()) return "/members/password";  // [변경] 기본 이동처 고정
    String n = next.trim();
    int comma = n.indexOf(',');
    if (comma != -1) n = n.substring(0, comma).trim();
    while (!n.isEmpty() && (n.endsWith(".") || n.endsWith(";"))) {
      n = n.substring(0, n.length() - 1).trim();
    }
    if (n.startsWith("http://") || n.startsWith("https://") || n.startsWith("//")) return "/members/password";
    if (!n.startsWith("/")) return "/members/password";
    return n;
  }
}
