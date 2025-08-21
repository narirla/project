package com.KDT.mosi.web.controller.member;

import com.KDT.mosi.domain.member.svc.MemberSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberApiController {

  private final MemberSVC memberSVC;

  // 📌 이메일 중복 확인
  @GetMapping("/check-email")
  public Map<String, Boolean> checkEmail(@RequestParam("email") String email) {
    boolean exists = memberSVC.isExistEmail(email);
    Map<String, Boolean> response = new HashMap<>();
    response.put("exists", exists);
    return response;
  }
}