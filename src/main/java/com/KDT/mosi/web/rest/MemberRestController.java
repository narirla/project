package com.KDT.mosi.web.rest;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members") // ✅ REST API 경로는 보안 및 명확성 측면에서 /api prefix 사용 권장
@RequiredArgsConstructor
public class MemberRestController {

  private final MemberSVC memberSVC;
  private final PasswordEncoder passwordEncoder;

  /**
   * ✅ 현재 비밀번호 검증 API
   * - 로그인된 사용자의 현재 비밀번호와 DB 비밀번호 비교
   * - 입력된 평문 비밀번호(passwd)와 암호화된 비밀번호 비교
   *
   * @param passwd 클라이언트에서 입력한 비밀번호 (평문)
   * @param session 로그인 세션
   * @return true: 일치함, false: 로그인 안됨 또는 일치하지 않음
   */
  @GetMapping("/passwordCheck")
  public boolean checkCurrentPassword(@RequestParam String passwd, HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) return false;

    String encodedPw = memberSVC.findPasswdById(loginMember.getMemberId());
    return passwordEncoder.matches(passwd, encodedPw);
  }

  /**
   * ✅ 비밀번호 변경 API
   * - 현재 비밀번호 일치 여부 확인
   * - 유효성 검사를 통과한 새 비밀번호로 암호화 후 저장
   *
   * @param currentPasswd 현재 비밀번호 (평문)
   * @param newPasswd 새 비밀번호 (평문)
   * @param session 로그인 세션
   * @return 성공: 200 OK + 메시지 / 실패: 400 또는 401 상태와 에러 메시지
   */
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      @RequestParam String currentPasswd,
      @RequestParam String newPasswd,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
    }

    // 현재 비밀번호 확인
    String encodedPw = memberSVC.findPasswdById(loginMember.getMemberId());
    if (!passwordEncoder.matches(currentPasswd, encodedPw)) {
      return ResponseEntity.badRequest().body("현재 비밀번호가 일치하지 않습니다.");
    }

    // 새 비밀번호 유효성 검사
    if (newPasswd == null || newPasswd.length() < 8) {
      return ResponseEntity.badRequest().body("새 비밀번호는 8자 이상이어야 합니다.");
    }

    // 비밀번호 암호화 후 저장
    String newEncodedPw = passwordEncoder.encode(newPasswd);
    memberSVC.updatePasswd(loginMember.getMemberId(), newEncodedPw);

    return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
  }
}
