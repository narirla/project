package com.KDT.mosi.web.common;

import com.KDT.mosi.domain.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice  // 모든 컨트롤러에서 공통 적용
public class GlobalControllerAdvice {

  @ModelAttribute("loginMember")
  public Member loginMember(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      return (Member) session.getAttribute("loginMember");
    }
    return null;  // 로그인 안된 경우 null 반환
  }
}
