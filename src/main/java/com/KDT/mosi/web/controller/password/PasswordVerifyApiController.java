package com.KDT.mosi.web.controller.password;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class PasswordVerifyApiController {

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/password-check")
  public Map<String, Object> passwordCheck(@RequestParam("password") String password,
                                           HttpSession session) {
    Member login = (Member) session.getAttribute("loginMember");
    boolean ok = (login != null) &&
        memberSVC.findById(login.getMemberId())
            .map(m -> passwordEncoder.matches(password, m.getPasswd()))
            .orElse(false);
    return Map.of("ok", ok);
  }
}
