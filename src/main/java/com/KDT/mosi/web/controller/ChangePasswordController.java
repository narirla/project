package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.web.form.member.PasswordForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class ChangePasswordController {

  private static final long PASSWORD_VERIFY_TTL_MS = 5 * 60 * 1000L;

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;

  /** 비밀번호 변경 폼 진입 */
  @GetMapping("/password")
  public String changePasswordForm(HttpSession session,
                                   HttpServletRequest request,
                                   Model model) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    // (선택) 최근 재인증이 없다면 여기서 막고 재인증 페이지로 보낼 수도 있음
    // Boolean verified = (Boolean) session.getAttribute("passwordVerified");
    // Long verifiedAt = (Long) session.getAttribute("passwordVerifiedAt");
    // if (verified == null || !verified || verifiedAt == null ||
    //     System.currentTimeMillis() - verifiedAt > PASSWORD_VERIFY_TTL_MS) {
    //   session.setAttribute("INTENDED_URI", request.getRequestURI());
    //   return "redirect:/members/verify-password";
    // }

    injectCommonModel(request, session, model, login);
    model.addAttribute("passwordForm", new PasswordForm());
    return "member/changePassword"; // 템플릿 파일명과 1:1 일치
  }

  /** 비밀번호 변경 처리 */
  @PostMapping("/password")
  public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                               BindingResult bindingResult,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    Member dbMember = memberSVC.findById(login.getMemberId()).orElseThrow();

    String currentPw = safe(form.getCurrentPassword());
    String newPw     = safe(form.getNewPassword());
    String confirmPw = safe(form.getConfirmPassword());

    // 0) 입력 기본 검증 (필요 시 Bean Validation 추가)
    if (newPw.length() < 8) {
      bindingResult.rejectValue("newPassword", "length", "새 비밀번호는 8자 이상이어야 합니다.");
    }
    if (!newPw.equals(confirmPw)) {
      bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호 확인이 일치하지 않습니다.");
    }

    // 1) 최근 재인증 or 현재 비밀번호 대조(둘 중 하나 충족)
    Boolean verified = (Boolean) session.getAttribute("passwordVerified");
    Long verifiedAt = (Long) session.getAttribute("passwordVerifiedAt");
    boolean verifiedRecently = verified != null && verified &&
        verifiedAt != null &&
        (System.currentTimeMillis() - verifiedAt) <= PASSWORD_VERIFY_TTL_MS;

    boolean currentOk = !currentPw.isEmpty() && passwordEncoder.matches(currentPw, dbMember.getPasswd());
    if (!(verifiedRecently || currentOk)) {
      bindingResult.rejectValue("currentPassword", "mismatch", "현재 비밀번호가 일치하지 않습니다.");
    }

    // 2) 이전 비밀번호 재사용 금지
    if (!newPw.isEmpty() && passwordEncoder.matches(newPw, dbMember.getPasswd())) {
      bindingResult.rejectValue("newPassword", "reused", "이전 비밀번호는 사용할 수 없습니다.");
    }

    if (bindingResult.hasErrors()) {
      // ⬇️ 재렌더링 시에도 헤더/사이드바 모델 주입 필요
      injectCommonModel(request, session, model, login);
      return "member/changePassword";
    }

    // 3) 변경 반영
    dbMember.setPasswd(passwordEncoder.encode(newPw));
    memberSVC.modify(dbMember.getMemberId(), dbMember);
    log.info("✅ 비밀번호 변경 완료: memberId={}", login.getMemberId());

    // 4) 재인증 플래그 제거 + 세션 무효화
    session.removeAttribute("passwordVerified");
    session.removeAttribute("passwordVerifiedAt");
    session.invalidate();

    ra.addFlashAttribute("msg", "비밀번호가 변경되었습니다. 다시 로그인해 주세요.");
    return "redirect:/login";
  }

  // 공통 모델 주입: 헤더/사이드바에서 사용
  private void injectCommonModel(HttpServletRequest request, HttpSession session, Model model, Member login) {
    model.addAttribute("currentPath", request.getRequestURI());
    String role = (String) session.getAttribute("loginRole");
    if (role == null) role = "BUYER";
    model.addAttribute("loginRole", role);
    model.addAttribute("member", login);
    // SELLER 인 경우 sellerSidebar가 필요하면 아래 주입 (서비스 주입 후 사용)
    // sellerPageSVC.findByMemberId(login.getMemberId())
    //              .ifPresent(p -> model.addAttribute("sellerPage", p));
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
