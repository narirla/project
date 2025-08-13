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
import jakarta.servlet.http.HttpSession;
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


  /**
   * 회원가입 폼 화면
   * - 역할 및 약관 정보도 함께 조회하여 뷰에 전달
   */
  @GetMapping("/join")
  public String joinForm(Model model) {
    model.addAttribute("form", new MemberJoinForm());
    model.addAttribute("roles", roleDAO.findAll());
    model.addAttribute("terms", termsSVC.findAll());
    return "member/joinForm";
  }

  /**
   * 회원가입 처리
   * - 비밀번호 확인, 이메일 중복, 약관, 역할, 이미지 등 포함
   */
  @PostMapping("/join")
  public String join(
      @Valid @ModelAttribute("form") MemberJoinForm form,
      BindingResult bindingResult,
      HttpServletRequest request,
      Model model  // ✅ nickname 전달을 위한 Model 추가
  ) {
    // 비밀번호 확인
    if (!form.getPasswd().equals(form.getConfirmPasswd())) {
      bindingResult.rejectValue("confirmPasswd", "mismatch", "비밀번호가 일치하지 않습니다.");
    }

    // 유효성 검증 실패 시
    if (bindingResult.hasErrors()) {
      return "member/joinForm";
    }

    // 이메일 중복 확인
    if (memberSVC.isExistEmail(form.getEmail())) {
      bindingResult.rejectValue("email", "duplicated", "이미 사용 중인 이메일입니다.");
      return "member/joinForm";  // ✅ 오류: 원래 joinSuccess 반환하던 부분 수정함
    }

    // Member 객체 구성
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

    // 생년월일
    if (form.getBirthDate() != null) {
      try {
        member.setBirthDate(LocalDate.parse(form.getBirthDate()));
      } catch (DateTimeException e) {
        log.warn("생년월일 파싱 실패", e);
      }
    }

    // 프로필 이미지
    MultipartFile file = form.getPicFile();
    if (file != null && !file.isEmpty()) {
      try {
        member.setPic(file.getBytes());
      } catch (IOException e) {
        log.error("프로필 이미지 변환 실패", e);
      }
    }

    // 역할과 약관 ID 목록
    List<String> roles = form.getRoles() != null ? form.getRoles() : new ArrayList<>();
    if (roles.isEmpty()) {
      roles.add("R01");  // ✅ 기본 역할 부여
    }
    List<Long> terms = form.getAgreedTermsIds() != null ? form.getAgreedTermsIds() : new ArrayList<>();

    // 회원가입 처리
    try {
      Long savedId = memberSVC.join(member, roles, terms);

      // 2️⃣ BuyerPage 저장
      BuyerPage buyerPage = new BuyerPage();
      buyerPage.setMemberId(savedId);
      buyerPage.setNickname(member.getNickname());
      buyerPage.setTel(member.getTel());
      buyerPage.setZonecode(member.getZonecode());
      buyerPage.setAddress(member.getAddress());
      buyerPage.setDetailAddress(member.getDetailAddress());

      buyerPageSVC.create(buyerPage);

      // 환영 페이지로 닉네임 전달
      model.addAttribute("nickname", form.getNickname());
      return "member/joinSuccess";  // ✅ joinSuccess.html 로 이동
    } catch (Exception e) {
      log.error("회원가입 처리 중 오류 발생", e);
      bindingResult.reject("joinFail", "회원가입 중 문제가 발생했습니다.");
      return "member/joinForm";
    }
  }


  /**
   * 이메일 중복 확인 API
   * - 프론트엔드 비동기 요청 처리용
   */
  @GetMapping("/emailCheck")
  public ResponseEntity<Map<String, Boolean>> emailCheck(@RequestParam("email") String email) {
    boolean exists = memberSVC.isExistEmail(email);
    Map<String, Boolean> result = new HashMap<>();
    result.put("exists", exists);
    return ResponseEntity.ok(result);
  }

  /**
   * 회원 정보 조회 페이지 (디버깅/확인용)
   */
  @GetMapping("/{id}")
  public String view(@PathVariable("id") Long id, Model model) {
    memberSVC.findById(id).ifPresent(member -> model.addAttribute("member", member));
    return "member/viewMember";
  }

  /**
   * 프로필 이미지 다운로드
   */
  @GetMapping("/{id}/pic")
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

  /**
   * 회원 정보 수정 폼
   * - 본인 인증 및 기존 정보 조회 후 폼 전달
   */
  @GetMapping("/{id}/edit")
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

  /**
   * 회원 정보 수정 처리
   * - 비밀번호 변경 포함, 본인 인증 필수
   */
  @PostMapping("/{id}/edit")
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
      try {
        member.setBirthDate(LocalDate.parse(form.getBirthDate()));
      } catch (DateTimeException e) {
        log.warn("생년월일 파싱 실패", e);
      }
    }

    MultipartFile file = form.getPicFile();
    if (file != null && !file.isEmpty()) {
      try {
        member.setPic(file.getBytes());
      } catch (IOException e) {
        log.error("프로필 이미지 변환 실패", e);
      }
    }

    memberSVC.modify(id, member);

    // R01 = 구매자
    if (memberSVC.hasRole(id, "R01")) {
      return "redirect:/mypage/buyer";
    }

    return "redirect:/members/" + id;
  }

  /**
   * 닉네임 중복 확인 API
   * - 프론트엔드에서 blur 이벤트로 호출됨
   */
  @GetMapping("/nicknameCheck")
  @ResponseBody
  public ResponseEntity<Boolean> nicknameCheck(@RequestParam("nickname") String nickname) {
    boolean exist = memberSVC.isExistNickname(nickname);
    return ResponseEntity.ok(exist);  // true = 중복, false = 사용 가능
  }

  /**
   *
   * @param id
   * @param request
   * @return
   */
  @PostMapping("/{id}/delete")
  public String deleteMember(@PathVariable("id") Long id, HttpServletRequest request) {
    Long loginMemberId = getLoginMemberId(request);
    log.info("🟢 [탈퇴 요청 진입] memberId = {}", id);
    if (loginMemberId == null || !loginMemberId.equals(id)) {
      log.warn("⛔ [탈퇴 실패] 로그인 ID 불일치 또는 비로그인");
      return "error/403";
    }

    try {
      // ✅ 0. 회원-역할 매핑 삭제
      memberSVC.deleteMemberRoles(id);   // 👉 memberRoleDAO.deleteByMemberId(id) 호출

      // 1. 회원 탈퇴 처리
      memberSVC.deleteById(id);
      log.info("✅ [회원 DB 삭제 완료] memberId = {}", id);

      // 2. 마이페이지 정보 삭제
      buyerPageSVC.deleteByMemberId(id);
      log.info("✅ [마이페이지 삭제 완료] memberId = {}", id);

      // 3. 세션 무효화
      request.getSession().invalidate();
      log.info("✅ [마이페이지 삭제 완료] memberId = {}", id);

      log.info("➡️ [리다이렉트] /members/goodbye");
      return "redirect:/members/goodbye";
    } catch (Exception e) {
      log.error("❌ [탈퇴 처리 중 예외 발생]", e);
      e.printStackTrace();  // 또는 log.error("회원 탈퇴 실패", e);
      throw e;  // 개발 단계에서는 그대로 다시 던져도 됨
    }
  }


  /**
   * 현재 로그인한 회원의 ID를 반환
   * - 세션에서 loginMember 객체를 꺼내어 ID를 반환
   */
  private Long getLoginMemberId(HttpServletRequest request) {
    Member loginMember = (Member) request.getSession().getAttribute("loginMember");
    return loginMember != null ? loginMember.getMemberId() : null;
  }

  /**
   * 회원탈퇴
   * @return
   */
  @GetMapping("/goodbye")
  public String goodbyePage() {
    return "member/goodbye";
  }


  /**
   *  비밀번호 일치 여부 확인 API (판매자 마이페이지 수정용)
   * - 세션의 loginMember 정보 기반으로 입력된 비밀번호가 맞는지 확인
   * - 사용 예: 기존 비밀번호 확인 필드에서 비동기 호출
   */
  @GetMapping("/passwordCheck")
  @ResponseBody
  public boolean passwordCheck(@RequestParam("passwd") String passwd, HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return false; // 비로그인 상태
    }

    return memberSVC.findById(loginMember.getMemberId())
        .map(member -> passwordEncoder.matches(passwd, member.getPasswd()))
        .orElse(false);
  }



}
