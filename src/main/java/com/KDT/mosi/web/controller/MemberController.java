package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.buyer.svc.BuyerPageSVC;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.terms.svc.TermsSVC;
import com.KDT.mosi.web.form.member.MemberEditForm;
import com.KDT.mosi.web.form.member.MemberJoinForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 회원 관련 웹 컨트롤러
 * - 회원가입, 정보 조회, 수정, 중복 확인 등을 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;
  private final RoleDAO roleDAO;
  private final TermsSVC termsSVC;
  private final BCryptPasswordEncoder passwordEncoder;
  private final BuyerPageSVC buyerPageSVC;
  private final SellerPageSVC sellerPageSVC;

  /** 회원가입 폼 화면 */
  @GetMapping("/join")
  public String joinForm(Model model) {
    model.addAttribute("form", new MemberJoinForm());
    model.addAttribute("roles", roleDAO.findAll());
    model.addAttribute("terms", termsSVC.findAll());
    return "member/joinForm";
  }

  /** 회원가입 처리 */
  @PostMapping("/join")
  public String join(
      @Valid @ModelAttribute("form") MemberJoinForm form,
      BindingResult bindingResult,
      HttpServletRequest request,
      Model model
  ) {
    // 비밀번호 확인
    if (!form.getPasswd().equals(form.getConfirmPasswd())) {
      bindingResult.rejectValue("confirmPasswd", "mismatch", "비밀번호가 일치하지 않습니다.");
    }
    if (bindingResult.hasErrors()) {
      return "member/joinForm";
    }

    // 이메일 중복
    if (memberSVC.isExistEmail(form.getEmail())) {
      bindingResult.rejectValue("email", "duplicated", "이미 사용 중인 이메일입니다.");
      return "member/joinForm";
    }

    // Member 구성
    Member member = new Member();
    member.setEmail(form.getEmail());
    member.setName(form.getName());
    member.setPasswd(passwordEncoder.encode(form.getPasswd()));
    member.setTel(form.getTel());
    member.setNickname(form.getNickname());
    member.setGender(form.getGender());
    member.setAddress(form.getAddress());
    member.setZonecode(form.getZonecode());
    member.setDetailAddress(form.getDetailAddress());

    if (form.getBirthDate() != null) {
      try { member.setBirthDate(LocalDate.parse(form.getBirthDate())); }
      catch (DateTimeException e) { log.warn("생년월일 파싱 실패", e); }
    }

    MultipartFile file = form.getPicFile();
    if (file != null && !file.isEmpty()) {
      try { member.setPic(file.getBytes()); }
      catch (IOException e) { log.error("프로필 이미지 변환 실패", e); }
    }

    List<String> roles = form.getRoles() != null ? form.getRoles() : new ArrayList<>();
    if (roles.isEmpty()) roles.add("R01"); // 기본 BUYER
    List<Long> terms = form.getAgreedTermsIds() != null ? form.getAgreedTermsIds() : new ArrayList<>();

    try {
      Long savedId = memberSVC.join(member, roles, terms);

      // BuyerPage 기본 생성
      BuyerPage buyerPage = new BuyerPage();
      buyerPage.setMemberId(savedId);
      buyerPage.setNickname(member.getNickname());
      buyerPage.setTel(member.getTel());
      buyerPage.setZonecode(member.getZonecode());
      buyerPage.setAddress(member.getAddress());
      buyerPage.setDetailAddress(member.getDetailAddress());
      buyerPageSVC.create(buyerPage);

      model.addAttribute("nickname", form.getNickname());
      return "member/joinSuccess";
    } catch (Exception e) {
      log.error("회원가입 처리 중 오류", e);
      bindingResult.reject("joinFail", "회원가입 중 문제가 발생했습니다.");
      return "member/joinForm";
    }
  }

  /** 이메일 중복 확인 API */
  @GetMapping("/emailCheck")
  public ResponseEntity<Map<String, Boolean>> emailCheck(@RequestParam("email") String email) {
    boolean exists = memberSVC.isExistEmail(email);
    Map<String, Boolean> result = new HashMap<>();
    result.put("exists", exists);
    return ResponseEntity.ok(result);
  }

  /** 회원 정보 조회 (정규식 적용) */
  @GetMapping("/{id:\\d+}")
  public String view(@PathVariable("id") Long id, Model model) {
    memberSVC.findById(id).ifPresent(member -> model.addAttribute("member", member));
    return "member/viewMember";
  }

  /** 프로필 이미지 다운로드 (정규식 적용) */
  @GetMapping("/{id:\\d+}/pic")
  @ResponseBody
  public ResponseEntity<byte[]> downloadProfilePic(@PathVariable Long id) {
    return memberSVC.findById(id)
        .filter(member -> member.getPic() != null)
        .map(member -> {
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.IMAGE_JPEG);
          return new ResponseEntity<>(member.getPic(), headers, HttpStatus.OK);
        })
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  /** 회원 정보 수정 폼 (정규식 적용) */
  @GetMapping("/{id:\\d+}/edit")
  public String editForm(@PathVariable("id") Long id, Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loginEmail = auth.getName();
    Member loginMember = memberSVC.findByEmail(loginEmail).orElseThrow();

    if (!loginMember.getMemberId().equals(id)) {
      return "error/403";
    }

    return memberSVC.findById(id).map(member -> {
      MemberEditForm form = new MemberEditForm();
      form.setEmail(member.getEmail());
      form.setName(member.getName());
      form.setTel(member.getTel());
      form.setNickname(member.getNickname());
      form.setGender(member.getGender());
      form.setAddress(member.getAddress());
      form.setZonecode(member.getZonecode());
      form.setDetailAddress(member.getDetailAddress());
      if (member.getBirthDate() != null) {
        form.setBirthDate(member.getBirthDate().toString());
      }

      model.addAttribute("form", form);
      model.addAttribute("memberId", id);
      return "member/editMember";
    }).orElse("redirect:/");
  }

  /** 회원 정보 수정 처리 (이미 정규식 적용되어 있던 부분 유지) */
  @PostMapping("/{id:\\d+}/edit")
  public String edit(
      @PathVariable("id") Long id,
      @Valid @ModelAttribute("form") MemberEditForm form,
      BindingResult bindingResult,
      Model model
  ) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loginEmail = auth.getName();
    Member loginMember = memberSVC.findByEmail(loginEmail).orElseThrow();

    if (!loginMember.getMemberId().equals(id)) {
      return "error/403";
    }

    if (form.getPasswd() != null && !form.getPasswd().isBlank()) {
      if (!form.getPasswd().equals(form.getConfirmPasswd())) {
        bindingResult.rejectValue("confirmPasswd", "mismatch", "비밀번호가 일치하지 않습니다.");
      }
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("memberId", id);
      return "member/editMember";
    }

    Member member = memberSVC.findById(id).orElseThrow();
    member.setName(form.getName());
    member.setTel(form.getTel());
    member.setNickname(form.getNickname());
    member.setGender(form.getGender());
    member.setAddress(form.getAddress());
    member.setZonecode(form.getZonecode());
    member.setDetailAddress(form.getDetailAddress());

    if (form.getPasswd() != null && !form.getPasswd().isBlank()) {
      member.setPasswd(passwordEncoder.encode(form.getPasswd()));
    }

    if (form.getBirthDate() != null && !form.getBirthDate().isBlank()) {
      try { member.setBirthDate(LocalDate.parse(form.getBirthDate())); }
      catch (DateTimeException e) { log.warn("생년월일 파싱 실패", e); }
    }

    MultipartFile file = form.getPicFile();
    if (file != null && !file.isEmpty()) {
      try { member.setPic(file.getBytes()); }
      catch (IOException e) { log.error("프로필 이미지 변환 실패", e); }
    }

    memberSVC.modify(id, member);

    // R01 = 구매자
    if (memberSVC.hasRole(id, "R01")) {
      return "redirect:/mypage/buyer";
    }
    return "redirect:/members/" + id;
  }

  /** 닉네임 중복 확인 API */
  @GetMapping("/nicknameCheck")
  @ResponseBody
  public ResponseEntity<Boolean> nicknameCheck(@RequestParam("nickname") String nickname) {
    boolean exist = memberSVC.isExistNickname(nickname);
    return ResponseEntity.ok(exist);  // true = 중복, false = 사용 가능
  }

  /** 회원 탈퇴 (정규식 적용) */
  @PostMapping("/{id:\\d+}/delete")
  public String deleteMember(@PathVariable("id") Long id, HttpServletRequest request) {
    Long loginMemberId = getLoginMemberId(request);
    log.info("🟢 [탈퇴 요청 진입] memberId = {}", id);
    if (loginMemberId == null || !loginMemberId.equals(id)) {
      log.warn("⛔ [탈퇴 실패] 로그인 ID 불일치 또는 비로그인");
      return "error/403";
    }

    try {
      // 0. 회원-역할 매핑 삭제
      memberSVC.deleteMemberRoles(id);

      // 1. 회원 삭제
      memberSVC.deleteById(id);
      log.info("✅ [회원 DB 삭제 완료] memberId = {}", id);

      // 2. 마이페이지 정보 삭제
      buyerPageSVC.deleteByMemberId(id);
      log.info("✅ [마이페이지 삭제 완료] memberId = {}", id);

      // 3. 세션 무효화
      request.getSession().invalidate();

      log.info("➡️ [리다이렉트] /members/goodbye");
      return "redirect:/members/goodbye";
    } catch (Exception e) {
      log.error("❌ [탈퇴 처리 중 예외 발생]", e);
      throw e;
    }
  }

  /** 현재 로그인한 회원의 ID 반환 */
  private Long getLoginMemberId(HttpServletRequest request) {
    Member loginMember = (Member) request.getSession().getAttribute("loginMember");
    return loginMember != null ? loginMember.getMemberId() : null;
  }

  /** 회원탈퇴 완료 페이지 */
  @GetMapping("/goodbye")
  public String goodbyePage() {
    return "member/goodbye";
  }


}
