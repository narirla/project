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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final MemberSVC memberSVC;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {
    // 1. 이메일로 사용자 정보 조회
    String email = authentication.getName();  // 로그인에 사용된 이메일
    Member member = memberSVC.findByEmail(email).orElseThrow();

    // 2. 세션에 사용자 정보 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", member);
    log.info("✅ 로그인 성공: 세션에 loginMember 저장 - {}", member.getEmail());

    // 3. 홈으로 리디렉션 (필요 시 redirect 파라미터 처리 가능)
    response.sendRedirect("/");
  }
}
