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

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class ChangePasswordController {

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;

  /** 비밀번호 변경 폼 진입 */
  @GetMapping("/password")
  public String changePasswordForm(HttpSession session, RedirectAttributes ra) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";
    return "member/changePassword"; // /templates/member/changePassword.html
  }

  /** 비밀번호 변경 처리 */
  @PostMapping("/password")
  public String changePassword(@RequestParam("currentPw") String currentPw,
                               @RequestParam("newPw") String newPw,
                               @RequestParam("confirmPw") String confirmPw,
                               HttpSession session,
                               RedirectAttributes ra) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    // 0) 입력 검증
    if (!newPw.equals(confirmPw)) {
      ra.addFlashAttribute("error", "새 비밀번호 확인이 일치하지 않습니다.");
      return "redirect:/members/password";
    }
    if (newPw.length() < 8) { // 예시 정책: 8자 이상
      ra.addFlashAttribute("error", "새 비밀번호는 8자 이상이어야 합니다.");
      return "redirect:/members/password";
    }

    // 1) 최근 재인증(5분 이내) 또는 currentPw 대조
    Boolean verified = (Boolean) session.getAttribute("passwordVerified");
    Long verifiedAt = (Long) session.getAttribute("passwordVerifiedAt");
    boolean verifiedRecently = verified != null && verified
        && verifiedAt != null
        && (System.currentTimeMillis() - verifiedAt) <= (5 * 60 * 1000L);

    String encoded = memberSVC.findById(login.getMemberId())
        .orElseThrow()
        .getPasswd();

    boolean currentOk = passwordEncoder.matches(currentPw, encoded);

    if (!(verifiedRecently || currentOk)) {
      ra.addFlashAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
      return "redirect:/members/password";
    }

    // 2) 새 비밀번호가 현재 비밀번호와 동일한지 체크(권장)
    if (passwordEncoder.matches(newPw, encoded)) {
      ra.addFlashAttribute("error", "현재 비밀번호와 다른 새 비밀번호를 사용하세요.");
      return "redirect:/members/password";
    }

    // 3) 변경
    memberSVC.updatePasswd(login.getMemberId(), passwordEncoder.encode(newPw));
    log.info("✅ 비밀번호 변경 완료: memberId={}", login.getMemberId());

    // 4) 재인증 플래그 제거 + 재로그인 요구(권장)
    session.removeAttribute("passwordVerified");
    session.removeAttribute("passwordVerifiedAt");
    session.invalidate(); // 보안상 세션 무효화 권장

    ra.addFlashAttribute("msg", "비밀번호가 변경되었습니다. 다시 로그인해 주세요.");
    return "redirect:/login";
  }
}
