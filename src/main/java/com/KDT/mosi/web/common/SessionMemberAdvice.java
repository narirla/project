package com.KDT.mosi.web.common;

import com.KDT.mosi.domain.entity.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SessionMemberAdvice {

  @ModelAttribute("loginMember")
  public Member loginMember(HttpSession session) {
    Member member = (Member) session.getAttribute("loginMember");
    System.out.println("🟡 [SessionMemberAdvice] session loginMember = " + member);
    return member;
  }
}



