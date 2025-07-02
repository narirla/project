package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.member.svc.MemberSVC;
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

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;
  private final RoleDAO roleDAO;       // ✅ 역할 리스트용 DAO
  private final TermsSVC termsSVC;     // ✅ 약관 리스트용 SVC
  private final BCryptPasswordEncoder passwordEncoder;


  // ✅ 회원가입 폼
  @GetMapping("/join")
  public String joinForm(Model model) {
    model.addAttribute("form", new MemberJoinForm());

    // 역할(R01, R02 등) 목록 조회
    model.addAttribute("roles", roleDAO.findAll());

    // 약관 목록 조회
    model.addAttribute("terms", termsSVC.findAll());

    return "member/joinForm"; // Thymeleaf 템플릿 파일
  }

  // ✅ 회원가입 처리
  @PostMapping("/join")
  public String join(
      @Valid @ModelAttribute("form") MemberJoinForm form,
      BindingResult bindingResult,
      HttpServletRequest request
  ) {

    // 비밀번호 확인 검증 추가 위치
    if (!form.getPasswd().equals(form.getConfirmPasswd())) {
      bindingResult.rejectValue("confirmPasswd", "mismatch", "비밀번호가 일치하지 않습니다.");
    }

    // 유효성 검사 실패 시
    if (bindingResult.hasErrors()) {
      return "member/joinForm";
    }

    // 이메일 중복 체크
    if (memberSVC.isExistEmail(form.getEmail())) {
      bindingResult.rejectValue("email", "duplicated", "이미 사용 중인 이메일입니다.");
      return "member/joinForm";
    }

    // MemberJoinForm → Member 변환
    Member member = new Member();
    member.setEmail(form.getEmail());
    member.setName(form.getName());
    member.setPasswd(passwordEncoder.encode(form.getPasswd()));
    member.setTel(form.getTel());
    member.setNickname(form.getNickname());
    member.setGender(form.getGender());
    member.setAddress(form.getAddress());

    if (form.getBirthDate() != null) {
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

    // ✅ NULL 방어 처리 추가
    List<String> roles = form.getRoles() != null ? form.getRoles() : new ArrayList<>();
    List<Long> terms = form.getAgreedTermsIds() != null ? form.getAgreedTermsIds() : new ArrayList<>();

    // ✅ 예외 발생 시 로그 출력
    try {
      Long savedId = memberSVC.join(member, roles, terms);
      return "redirect:/members/" + savedId;
    } catch (Exception e) {
      log.error("회원가입 처리 중 오류 발생", e);
      bindingResult.reject("joinFail", "회원가입 중 문제가 발생했습니다.");
      return "member/joinForm";
    }
  }


  @GetMapping("/emailCheck")
  public ResponseEntity<Map<String, Boolean>> emailCheck(@RequestParam("email") String email) {
    boolean exists = memberSVC.isExistEmail(email);
    Map<String, Boolean> result = new HashMap<>();
    result.put("exists", exists);  // 자바스크립트에서 result.exists로 접근 가능
    return ResponseEntity.ok(result);
  }


  // ✅ 회원 정보 조회 (디버깅용)
  @GetMapping("/{id}")
  public String view(@PathVariable Long id, Model model) {
    memberSVC.findById(id).ifPresent(member -> model.addAttribute("member", member));
    return "member/viewMember";
  }

  // ✅ 프로필 이미지 출력
  @GetMapping("/{id}/pic")
  @ResponseBody
  public ResponseEntity<byte[]> downloadProfilePic(@PathVariable Long id) {
    return memberSVC.findById(id)
        .filter(member -> member.getPic() != null)
        .map(member -> {
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.IMAGE_JPEG); // 또는 PNG 등 확장자에 맞게 조정 가능
          return new ResponseEntity<>(member.getPic(), headers, HttpStatus.OK);
        })
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  // ✅ 회원 수정 폼
  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id, Model model) {

    // 로그인 사용자 이메일 가져오기
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loginEmail = auth.getName();

    // 로그인 사용자의 memberId 가져오기
    Member loginMember = memberSVC.findByEmail(loginEmail).orElseThrow();

    // 본인 확인
    if (!loginMember.getMemberId().equals(id)) {
      return "error/403"; // 또는 예외 throw
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



  // ✅ 회원 정보 수정 처리
  @PostMapping("/{id}/edit")
  public String edit(
      @PathVariable Long id,
      @Valid @ModelAttribute("form") MemberEditForm form,
      BindingResult bindingResult,
      Model model
  ) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loginEmail = auth.getName();
    Member loginMember = memberSVC.findByEmail(loginEmail).orElseThrow();

    // 본인 확인
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

    // 회원 정보 업데이트
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

    // ✅ 구매자인 경우 마이페이지로 리다이렉트
    if (memberSVC.hasRole(id, "R01")) { // R01 = 구매자
      return "redirect:/mypage/buyer";
    }

    // 기본 리다이렉트
    return "redirect:/members/" + id;
  }







}
