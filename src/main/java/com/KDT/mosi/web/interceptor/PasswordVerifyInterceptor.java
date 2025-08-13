package com.KDT.mosi.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class PasswordVerifyInterceptor implements HandlerInterceptor {

  // 비밀번호 재인증 유효시간(5분)
  private static final long TTL = 5 * 60 * 1000L;

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    final String uri = req.getRequestURI();
    log.debug("🔎 Interceptor preHandle uri={}, method={}", uri, req.getMethod());

    // 보호 대상 URL 검사
    final boolean needsAuth =
        "/members/password".equals(uri) ||                                   // ✅ 비밀번호 변경 페이지
            uri.matches("^/mypage/buyer/\\d+/edit(?:/.*)?$") ||                  // 구매자 수정
            uri.matches("^/mypage/seller/\\d+/edit(?:/.*)?$") ||                 // 판매자 수정
            uri.matches("^/members/\\d+/edit(?:/.*)?$");                         // (있으면) 일반 회원 수정

    if (!needsAuth) return true;

    HttpSession session = req.getSession(false);
    if (session == null) {
      log.warn("접근 차단: 세션 없음 uri={}", uri);
      res.sendRedirect("/login");
      return false;
    }

    Boolean verified = (Boolean) session.getAttribute("passwordVerified");
    Long verifiedAt = (Long) session.getAttribute("passwordVerifiedAt");
    boolean expired = verifiedAt == null || (System.currentTimeMillis() - verifiedAt) > TTL;

    if (verified == null || !verified || expired) {
      if (expired && verifiedAt != null) {
        log.info("비밀번호 인증 만료: uri={}, 경과={}ms", uri, System.currentTimeMillis() - verifiedAt);
        session.removeAttribute("passwordVerified");
        session.removeAttribute("passwordVerifiedAt");
      } else {
        log.info("비밀번호 인증 미실시: uri={}", uri);
      }

      //  GET일 때만 의도 경로 저장(POST 재전송 방지)
      if ("GET".equalsIgnoreCase(req.getMethod())) {
        String q = req.getQueryString();
        String intended = (q == null) ? uri : uri + "?" + q;
        session.setAttribute("INTENDED_URI", intended);
      }

      res.sendRedirect("/members/verify-password");
      return false;
    }
    log.info("✅ 비밀번호 재인증 통과: uri={}", uri);
    return true;
  }

}
