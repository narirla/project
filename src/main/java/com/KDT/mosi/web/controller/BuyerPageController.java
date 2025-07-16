package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.buyer.svc.BuyerPageSVC;
import com.KDT.mosi.web.form.mypage.buyerpage.BuyerPageUpdateForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/buyer")
public class  BuyerPageController {

  private final BuyerPageSVC buyerPageSVC;
  private final MemberSVC memberSVC;

  // 🔒 로그인한 회원 ID 가져오기
  private Long getLoginMemberId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Object principal = auth.getPrincipal();

    if (principal instanceof com.KDT.mosi.security.CustomUserDetails userDetails) {
      return userDetails.getMember().getMemberId();
    }

    throw new IllegalStateException("로그인 사용자 정보를 확인할 수 없습니다.");
  }


  // ✅ 마이페이지 조회
  @GetMapping("/{memberId}")
  public String view(@PathVariable("memberId") Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    Optional<Member> optionalMember = memberSVC.findById(memberId);
    Optional<BuyerPage> optionalBuyerPage = buyerPageSVC.findByMemberId(memberId);

    if (optionalMember.isPresent()) {
      Member member = optionalMember.get();
      model.addAttribute("member", member);

      if (optionalBuyerPage.isPresent()) {
        BuyerPage page = optionalBuyerPage.get();

        // 🔽 BuyerPage에만 존재하는 필드만 갱신
        page.setMemberId(member.getMemberId());
        page.setTel(member.getTel());

        member.setNickname(page.getNickname());

        model.addAttribute("buyerPage", page);
        model.addAttribute("member", member);
        return "mypage/buyerpage/viewBuyerPage";
      } else {
        return "redirect:/mypage/buyer/" + memberId + "/edit";
      }

    } else {
      return "error/404";
    }
  }


  // ✅ 프로필 이미지 조회
  @GetMapping("/{memberId}/image")
  public ResponseEntity<byte[]> image(@PathVariable("memberId") Long memberId) {
    Optional<BuyerPage> optional = buyerPageSVC.findByMemberId(memberId);

    if (optional.isPresent() && optional.get().getImage() != null) {
      byte[] image = optional.get().getImage();
      MediaType mediaType = MediaType.IMAGE_JPEG;

      try {
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
        if (contentType != null) {
          mediaType = MediaType.parseMediaType(contentType);
        }
      } catch (IOException e) {
        log.warn("이미지 content type 분석 실패, 기본 JPEG 사용");
      }

      return ResponseEntity.ok()
          .contentType(mediaType)
          .body(image);
    }

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, "/img/default-profile.png")
        .build();
  }

  // ✅ 수정 폼
  @GetMapping("/{memberId}/edit")
  public String editForm(@PathVariable("memberId") Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    // 1. member 먼저 조회
    Optional<Member> optionalMember = memberSVC.findById(memberId);
    if (optionalMember.isEmpty()) {
      return "error/403";
    }
    Member member = optionalMember.get();

    // ✅ 핵심 포인트: 먼저 등록 (템플릿 파싱 전에 반드시 model에 존재해야 함)
    model.addAttribute("member", member);

    // 2. buyerPage 있으면 form 구성
    return buyerPageSVC.findByMemberId(memberId)
        .map(entity -> {
          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(entity.getPageId());
          form.setMemberId(entity.getMemberId());
          form.setIntro(entity.getIntro());
          form.setNickname(entity.getNickname() != null ? entity.getNickname() : member.getNickname());
          form.setTel(member.getTel());
          form.setName(member.getName());
          form.setZonecode(member.getZonecode());
          form.setAddress(member.getAddress());
          form.setDetailAddress(member.getDetailAddress());
          form.setNotification(member.getNotification());

          model.addAttribute("form", form);
          return "mypage/buyerpage/editBuyerPage";
        })
        .orElseGet(() -> {
          BuyerPage newPage = new BuyerPage();
          newPage.setMemberId(memberId);
          newPage.setNickname(member.getNickname());
          Long pageId = buyerPageSVC.create(newPage);

          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(pageId);
          form.setMemberId(memberId);
          form.setNickname(member.getNickname());
          form.setTel(member.getTel());
          form.setName(member.getName());
          form.setZonecode(member.getZonecode());
          form.setAddress(member.getAddress());
          form.setDetailAddress(member.getDetailAddress());
          form.setNotification(member.getNotification());

          model.addAttribute("form", form);
          return "mypage/buyerpage/editBuyerPage";
        });
  }


  // ✅ 마이페이지 수정 처리
  @PostMapping("/{memberId}")
  public String update(
      @PathVariable("memberId") Long memberId,
      @Valid @ModelAttribute("form") BuyerPageUpdateForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    // ───────────────────────────
    // 1. 권한·세션 검증
    // ───────────────────────────
    Long loginMemberId = getLoginMemberId();          // 로그인 사용자 ID
    if (!loginMemberId.equals(memberId)) {            // URL 과 세션 ID 불일치
      log.warn("🚫 접근 차단: loginId={} ≠ urlId={}", loginMemberId, memberId);
      return "error/403";
    }

    // ───────────────────────────
    // 2. 검증 오류 처리
    // ───────────────────────────
    if (bindingResult.hasErrors()) {
      bindingResult.getFieldErrors()
          .forEach(e -> log.warn("❌ {} : {}", e.getField(), e.getDefaultMessage()));
      return "mypage/buyerpage/editBuyerPage";
    }

    // ───────────────────────────
    // 3. 닉네임 중복 검사
    // ───────────────────────────
    String currentNickname   = buyerPageSVC.findByMemberId(memberId)
        .map(BuyerPage::getNickname)
        .orElse(null);
    String requestedNickname = form.getNickname();

    if (requestedNickname != null && !requestedNickname.equals(currentNickname) &&
        memberSVC.isExistNickname(requestedNickname)) {
      bindingResult.rejectValue("nickname", "duplicate", "이미 사용 중인 닉네임입니다.");
      return "mypage/buyerpage/editBuyerPage";
    }

    // ───────────────────────────
    // 4. BuyerPage 갱신
    // ───────────────────────────
    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setPageId(form.getPageId());
    buyerPage.setMemberId(memberId);
    buyerPage.setNickname(requestedNickname);
    buyerPage.setIntro(form.getIntro());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      try {
        buyerPage.setImage(form.getImageFile().getBytes());
      } catch (IOException e) {
        log.error("이미지 처리 실패", e);
      }
    }
    buyerPageSVC.update(form.getPageId(), buyerPage);

    // ───────────────────────────
    // 5. Member 기본 정보 갱신
    // ───────────────────────────
    Member member = new Member();
    member.setMemberId(memberId);
    member.setName(form.getName());
    member.setTel(form.getTel());
    member.setZonecode(form.getZonecode());
    member.setAddress(form.getAddress());
    member.setDetailAddress(form.getDetailAddress());
    member.setNotification("Y".equals(form.getNotification()) ? "Y" : "N");

    if (form.getPasswd() != null && !form.getPasswd().isBlank()) {
      member.setPasswd(form.getPasswd());
    }
    memberSVC.modify(memberId, member);

    // ───────────────────────────
    // 6. 완료 후 리다이렉트
    // ───────────────────────────
    redirectAttributes.addFlashAttribute("msg", "마이페이지 정보가 성공적으로 수정되었습니다.");
    return "redirect:/mypage/buyer/" + memberId;
  }


  // ✅ 기본 진입 시 로그인한 회원의 마이페이지로 리다이렉트
  @GetMapping
  public String buyerMypageHome(Model model) {
    Long loginMemberId = getLoginMemberId();
    model.addAttribute("memberId", loginMemberId);

    memberSVC.findById(loginMemberId).ifPresent(member -> model.addAttribute("member", member));

    return "mypage/buyerpage/buyerMypageHome";
  }
}
