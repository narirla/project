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
import java.util.List;

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

    // ✅ 1. 로그인 이메일로 Member 조회
    String email = authentication.getName();
    Member member = memberSVC.findByEmail(email).orElseThrow();

    // ✅ 2. 세션 생성 및 로그인 정보 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", member);
    session.setAttribute("loginMemberId", member.getMemberId());

    // ✅ 3. ROLE 리스트 조회 (BUYER, SELLER 둘 다 가능)
    List<String> roles = memberSVC.findRolesByMemberId(member.getMemberId());
    session.setAttribute("loginRoles", roles);  // ✅ 다중 역할 저장

    // ✅ 4. 필요 시 기본 역할(첫 번째 값)만 따로 저장
    if (!roles.isEmpty()) {
      session.setAttribute("loginRole", roles.get(0));  // 기본값: 구매자(BUYER)
    }

    log.info("✅ 로그인 성공: 세션에 loginMember & loginRoles 저장 - {}, Roles: {}",
        member.getEmail(), roles);

    // ✅ 5. 로그인 후 메인 페이지로 이동
    response.sendRedirect("/");
  }
}
