package com.KDT.mosi.web.controller.password;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class ChangePasswordController {

  /** 비밀번호 재인증 유효시간(밀리초). 현재 5분. */
  private static final long PASSWORD_VERIFY_TTL_MS = 5 * 60 * 1000L;

  /** 세션 키(표준) */
  private static final String SK_PWD_VERIFIED         = "PWD_VERIFIED";
  private static final String SK_PWD_VERIFIED_AT      = "PWD_VERIFIED_AT";
  private static final String SK_PWD_VERIFY_EXPIRES_AT= "PWD_VERIFY_EXPIRES_AT";
  /** 세션 키(구버전 호환) */
  private static final String SK_OLD_VERIFIED         = "passwordVerified";
  private static final String SK_OLD_VERIFIED_AT      = "passwordVerifiedAt";
  private static final String SK_OLD_EXPIRES_AT       = "passwordVerifyExpireAt";

  /**
   * 새 비밀번호 서버검증용 정규식
   * - 8~12자 + 대문자/소문자/숫자/특수문자 각각 1+ 포함
   * 정규식은 매 요청마다 컴파일 비용이 있으므로 Pattern 캐시.
   */
  private static final Pattern PW_PATTERN = Pattern.compile(
      "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\\\",.<>/?\\\\|`~]).{8,12}$"
  );

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;
  private final SellerPageSVC sellerPageSVC;

  /**
   * 비밀번호 변경 폼(GET)
   * - 로그인 확인
   * - 최근 재인증(fresh) 판정(표준 키 우선, 구 키 대체)
   * - 미신선 → /members/verify-password?next=현재경로 로 리다이렉트
   * - 신선 → 공통모델 주입 + 폼 렌더
   */
  @GetMapping("/password")
  public String changePasswordForm(HttpSession session,
                                   HttpServletRequest request,
                                   Model model) {
    // 1) 로그인 체크
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    // 2) 재인증 신선도 판정
    boolean fresh = isPasswordVerifiedFresh(session, System.currentTimeMillis());

    // 3) 미신선 → 재인증 페이지로(next=현재경로)
    if (!fresh) {
      String uri  = request.getRequestURI();              // 예: /members/password
      String qs   = request.getQueryString();             // 예: a=b (없으면 null)
      String next = (qs == null) ? uri : uri + "?" + qs;
      return "redirect:/members/verify-password?next=" +
          URLEncoder.encode(next, StandardCharsets.UTF_8);
    }

    // 4) 화면 렌더링
    injectCommonModel(request, session, model, login);
    model.addAttribute("passwordForm", new PasswordForm());
    return "member/PasswordForm"; // templates/member/PasswordForm.html
  }

  /**
   * 비밀번호 변경 처리(POST)
   * 서버 검증:
   *  - 길이/공백/조합/확인값 일치
   *  - "최근 재인증" 또는 "현재 비번 일치" 중 하나 충족
   *  - 이전 비번 재사용 금지
   * 성공 시:
   *  - 비번 해시 저장
   *  - 재인증 플래그 제거 후 세션 무효화
   *  - /login?changed=1 리다이렉트
   */
  @PostMapping("/password")
  public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                               BindingResult bindingResult,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model) {
    // 1) 로그인 체크
    Member login = (Member) session.getAttribute("loginMember");
    if (login == null) return "redirect:/login";

    // 2) 최신 멤버 조회
    Member dbMember = memberSVC.findById(login.getMemberId()).orElseThrow();

    // 3) 입력 정리
    String currentPw = safe(form.getCurrentPassword());
    String newPw     = safe(form.getNewPassword());
    String confirmPw = safe(form.getConfirmPassword());

    // 4) 서버측 검증
    if (newPw.length() < 8 || newPw.length() > 12) {
      bindingResult.rejectValue("newPassword", "length", "새 비밀번호는 8~12자여야 합니다.");
    }
    if (newPw.chars().anyMatch(Character::isWhitespace)) {
      bindingResult.rejectValue("newPassword", "space", "공백은 사용할 수 없습니다.");
    }
    if (!PW_PATTERN.matcher(newPw).matches()) {
      bindingResult.rejectValue("newPassword", "combo", "대/소문자·숫자·특수문자를 모두 포함해야 합니다.");
    }
    if (!newPw.equals(confirmPw)) {
      bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호 확인이 일치하지 않습니다.");
    }

    // 5) 본인 확인: 최근 재인증 OR 현재 비밀번호 일치
    boolean verifiedRecently = isPasswordVerifiedFresh(session, System.currentTimeMillis());
    boolean currentOk = !currentPw.isEmpty() && passwordEncoder.matches(currentPw, dbMember.getPasswd());
    if (!(verifiedRecently || currentOk)) {
      bindingResult.rejectValue("currentPassword", "mismatch", "현재 비밀번호가 일치하지 않습니다.");
    }

    // 6) 이전 비밀번호 재사용 금지
    if (!newPw.isEmpty() && passwordEncoder.matches(newPw, dbMember.getPasswd())) {
      bindingResult.rejectValue("newPassword", "reused", "이전 비밀번호는 사용할 수 없습니다.");
    }

    // 7) 에러 → 동일 화면 재렌더링(공통모델 재주입 필수)
    if (bindingResult.hasErrors()) {
      injectCommonModel(request, session, model, login);
      return "member/PasswordForm";
    }

    // 8) 비밀번호 변경 반영
    String encoded = passwordEncoder.encode(newPw);
    dbMember.setPasswd(encoded);
    memberSVC.modify(dbMember.getMemberId(), dbMember);
    log.info("✅ 비밀번호 변경 완료: memberId={}", login.getMemberId());

    // 9) 재인증 플래그 제거 + 세션 무효화(로그아웃)
    clearPasswordVerifyFlags(session);
    session.invalidate();

    // 10) 로그인 페이지로
    return "redirect:/login?changed=1";
  }

  /** 재인증 '신선함' 판단(표준키 우선, 구키 대체). */
  private boolean isPasswordVerifiedFresh(HttpSession session, long nowMillis) {
    // 표준 키
    Boolean vStd = asBoolean(session.getAttribute(SK_PWD_VERIFIED));
    Long    expStd = asLong(session.getAttribute(SK_PWD_VERIFY_EXPIRES_AT));
    Long    atStd  = asLong(session.getAttribute(SK_PWD_VERIFIED_AT));

    if (Boolean.TRUE.equals(vStd)) {
      if (expStd != null) return nowMillis <= expStd;
      if (atStd  != null) return nowMillis - atStd <= PASSWORD_VERIFY_TTL_MS;
    }

    // 구 키(호환)
    Boolean vOld = asBoolean(session.getAttribute(SK_OLD_VERIFIED));
    Long    expOld = asLong(session.getAttribute(SK_OLD_EXPIRES_AT));
    Long    atOld  = asLong(session.getAttribute(SK_OLD_VERIFIED_AT));

    if (Boolean.TRUE.equals(vOld)) {
      if (expOld != null) return nowMillis <= expOld;
      if (atOld  != null) return nowMillis - atOld <= PASSWORD_VERIFY_TTL_MS;
    }

    return false;
  }

  /** 세션의 재인증 플래그(신·구 키) 제거 */
  private void clearPasswordVerifyFlags(HttpSession session) {
    // 표준 키
    session.removeAttribute(SK_PWD_VERIFIED);
    session.removeAttribute(SK_PWD_VERIFIED_AT);
    session.removeAttribute(SK_PWD_VERIFY_EXPIRES_AT);
    // 구 키(호환)
    session.removeAttribute(SK_OLD_VERIFIED);
    session.removeAttribute(SK_OLD_VERIFIED_AT);
    session.removeAttribute(SK_OLD_EXPIRES_AT);
  }

  /**
   * 헤더/사이드바 공통 모델 주입
   * - activePath: "/members/password" 고정(사이드바 활성)
   * - currentPath: 헤더 활성
   * - loginRole: BUYER/SELLER 헤더 분기
   * - member, sellerPage: 항상 존재(없으면 null)
   */
  private void injectCommonModel(HttpServletRequest request, HttpSession session, Model model, Member login) {
    model.addAttribute("activePath", "/members/password"); // 사이드바 활성 기준
    model.addAttribute("currentPath", request.getRequestURI());

    String role = (String) session.getAttribute("loginRole");
    if (role == null) role = "BUYER";
    model.addAttribute("loginRole", role);

    model.addAttribute("member", login);

    // SELLER일 때만 조회, 그 외엔 명시적으로 null 주입
    if ("SELLER".equals(role)) {
      Optional<?> sp = sellerPageSVC.findByMemberId(login.getMemberId());
      model.addAttribute("sellerPage", sp.orElse(null));
    } else {
      model.addAttribute("sellerPage", null);
    }
  }

  /** 안전 trim */
  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private static Boolean asBoolean(Object o) {
    return (o instanceof Boolean b) ? b : null;
  }

  private static Long asLong(Object o) {
    return (o instanceof Long l) ? l : null;
  }
}
