package com.KDT.mosi.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class PasswordVerifyInterceptor implements HandlerInterceptor {

  private static final long TTL = 5 * 60 * 1000L; // 5분

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    final String uri = req.getRequestURI();
    log.debug("🔎 Interceptor preHandle uri={}, method={}", uri, req.getMethod());

    // 보호 경로 선택은 WebMvcConfig에서 관리하므로 여기선 신선도만 판단
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

      // next 쿼리로 원래 경로 전달
      String q = req.getQueryString();
      String intended = (q == null) ? uri : (uri + "?" + q);
      String next = URLEncoder.encode(intended, StandardCharsets.UTF_8);
      res.sendRedirect("/members/verify-password?next=" + next);
      return false;
    }

    log.info("✅ 비밀번호 재인증 통과: uri={}", uri);
    return true;
  }
}
