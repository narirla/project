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

/***
 * 로그인 성공 시 처리 핸들러
 * - 세션에 로그인 회원 정보 저장
 * - 직전 접근 요청(SavedRequest)이 존재하면 해당 URL로 리디렉션
 * - 없으면 기본 경로(/)로 이동
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final MemberSVC memberSVC;

  // 리디렉션 전략 객체(Spring 제공)
  private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  /**
   * 로그인 성공 시 실행되는 메서드
   * @param request 클라이언트의 HTTP 요청 객체
   * @param response 서버의 HTTP 응답 객체
   * @param authentication Spring Security의 인증 객체. 로그인한 사용자 정보를 포함함
   * @throws IOException 입출력 오류 발생 시
   * @throws ServletException 서블릿 처리 중 예외 발생 시
   */
  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    //  1. 로그인된 사용자의 이메일로 Member 엔티티 조회
    String email = authentication.getName();
    Member member = memberSVC.findByEmail(email).orElseThrow();   //예외 발생 시 서버 오류 처리됨

    //  2. 세션 생성 및 로그인 정보 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", member);               // 전체 회원 정보 저장
    session.setAttribute("loginMemberId", member.getMemberId()); // 회원 ID 별도 저장

    //  3. 회원 역할(Role) 리스트 조회 → 다중 역할 대응
    List<String> roles = memberSVC.findRolesByMemberId(member.getMemberId());
    session.setAttribute("loginRoles", roles);  // ✅ 다중 역할 저장

    //  4.기본 역할은 무조건 BUYER로 설정
    if (roles.contains("BUYER")) {
      session.setAttribute("loginRole", "BUYER");
    } else if (!roles.isEmpty()){
      session.setAttribute("loginRole", roles.get(0));
    }

    log.info("✅ 로그인 성공: 세션에 loginMember & loginRoles 저장 - {}, Roles: {}",
        member.getEmail(), roles);

    // 5. 로그인 직전 요청했던 URL(SavedRequest) 정보 조회
    SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request,response);

    if (savedRequest != null){
      //  로그인 전 요청한 URL이 존재하는 경우 → 해당 페이지로 리디렉션
      String redirectUrl = savedRequest.getRedirectUrl();
      log.info(" 이전 요청 URL로 이동: {}", redirectUrl);
      redirectStrategy.sendRedirect(request, response, redirectUrl);
    } else {
      // ️ 직접 로그인한 경우 → 기본 페이지(/)로 이동
      log.info("이전 요청 없음, 기본 페이지(/)로 이동");
      redirectStrategy.sendRedirect(request, response, "/");
    }
  }
}
