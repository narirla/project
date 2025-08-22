package com.KDT.mosi.web.login;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final MemberSVC memberSVC;
  private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    // 1) 로그인 사용자 이메일로 Member 조회
    String email = authentication.getName();
    Member member = memberSVC.findByEmail(email).orElseThrow();

    // 2) 세션 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", member);
    session.setAttribute("loginMemberId", member.getMemberId());

    // 3) 보유 역할 조회(예: ["R01","R02"]) → 표시용으로 정규화
    List<String> roles = memberSVC.findRolesByMemberId(member.getMemberId());
    List<String> normRoles = roles.stream()
        .map(r -> switch (r) {
          case "R01" -> "BUYER";
          case "R02" -> "SELLER";
          default -> r;
        })
        .toList();
    session.setAttribute("loginRoles", normRoles);

    // 4) 기본 표시 역할은 항상 BUYER
    session.setAttribute("loginRole", "BUYER");

    log.info("✅ 로그인 성공: {}, Roles(norm)={}", member.getEmail(), normRoles);

    // ✅ 5) 로그인 직전 요청 URL로 복귀 (복잡한 체크 로직 제거)
    SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
    if (savedRequest != null) {
      String redirectUrl = savedRequest.getRedirectUrl();
      log.info("↩ 이전 요청 URL로 이동: {}", redirectUrl);
      redirectStrategy.sendRedirect(request, response, redirectUrl);
    } else {
      log.info("기본 페이지(/)로 이동");
      redirectStrategy.sendRedirect(request, response, "/");
    }
  }
}