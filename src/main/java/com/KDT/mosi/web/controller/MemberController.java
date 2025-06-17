package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.terms.svc.TermsSVC;
import com.KDT.mosi.web.form.member.MemberJoinForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;
  private final RoleDAO roleDAO;       // ✅ 역할 리스트용 DAO
  private final TermsSVC termsSVC;     // ✅ 약관 리스트용 SVC

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
      BindingResult bindingResult
  ) {
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
    member.setPasswd(form.getPasswd());
    member.setTel(form.getTel());
    member.setNickname(form.getNickname());
    member.setGender(form.getGender());
    member.setAddress(form.getAddress());

    MultipartFile file = form.getPicFile();
    if (file != null && !file.isEmpty()) {
      try {
        member.setPic(file.getBytes());
      } catch (IOException e) {
        log.error("프로필 이미지 변환 실패", e);
      }
    }

    // ✅ 역할 + 약관 동의까지 포함하여 회원 등록
    Long savedId = memberSVC.join(member, form.getRoles(), form.getAgreedTermsIds());

    return "redirect:/members/" + savedId;
  }

  // ✅ 회원 정보 조회 (디버깅용)
  @GetMapping("/{id}")
  public String view(@PathVariable Long id, Model model) {
    memberSVC.findById(id).ifPresent(member -> model.addAttribute("member", member));
    return "member/view";
  }
}
