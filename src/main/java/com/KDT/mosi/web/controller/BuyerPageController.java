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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/buyer")
public class BuyerPageController {

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
  public String view(@PathVariable Long memberId, Model model) {
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
  public ResponseEntity<byte[]> image(@PathVariable Long memberId) {
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
  public String editForm(@PathVariable Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    // 🔽 member 정보도 조회
    Optional<Member> optionalMember = memberSVC.findById(memberId);
    if (optionalMember.isEmpty()) {
      return "error/404";
    }
    Member member = optionalMember.get();

    return buyerPageSVC.findByMemberId(memberId)
        .map(entity -> {
          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(entity.getPageId());
          form.setMemberId(entity.getMemberId());
          form.setIntro(entity.getIntro());

          // ✅ 수정된 닉네임 설정
          form.setNickname(entity.getNickname() != null ? entity.getNickname() : member.getNickname());

          // 🔽 추가: member 정보로부터 tel 세팅
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
          // 마이페이지 정보가 없을 경우 새로 생성
          BuyerPage newPage = new BuyerPage();
          newPage.setMemberId(memberId);
          newPage.setNickname(member.getNickname());
          Long pageId = buyerPageSVC.create(newPage);

          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(pageId);
          form.setMemberId(memberId);

          // 🔽 추가: 새 폼에도 기본 member 정보 세팅
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
      @PathVariable Long memberId,
      @Valid @ModelAttribute("form") BuyerPageUpdateForm form,
      BindingResult bindingResult
  ) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    log.info("🔐 Authentication: {}", auth);
    log.info("🔐 Principal: {}", auth.getPrincipal());

    Long loginMemberId = getLoginMemberId(); // 로그인 회원 ID 확인
    log.info("🔍 로그인 사용자 ID = {}", loginMemberId);
    log.info("📥 전달받은 form = {}", form);

    if (!loginMemberId.equals(form.getMemberId())) {
      log.warn("🚫 접근 차단: 로그인 ID({}) ≠ 폼의 회원ID({})", loginMemberId, form.getMemberId());
      return "error/403";
    }

    if (bindingResult.hasErrors()) {
      log.warn("❌ 유효성 검증 실패:");
      bindingResult.getFieldErrors().forEach(error ->
          log.warn("    🔸 {}: {}", error.getField(), error.getDefaultMessage())
      );
      return "mypage/buyerpage/editBuyerPage";
    }

    // 닉네임 중복 검사
    String currentNickname = buyerPageSVC.findByMemberId(memberId)
        .map(BuyerPage::getNickname)
        .orElse(null);
    String requestedNickname = form.getNickname();

    log.info("📌 현재 닉네임(DB): {}", currentNickname);
    log.info("📌 요청된 닉네임: {}", requestedNickname);

    if (requestedNickname != null && !requestedNickname.equals(currentNickname)) {
      if (memberSVC.isExistNickname(requestedNickname)) {
        log.warn("❌ 닉네임 중복: {}", requestedNickname);
        bindingResult.rejectValue("nickname", "duplicate", "이미 사용 중인 닉네임입니다.");
        return "mypage/buyerpage/editBuyerPage";
      }
    }

    // BuyerPage 업데이트
    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setPageId(form.getPageId());
    buyerPage.setMemberId(form.getMemberId());
    buyerPage.setNickname(requestedNickname);
    buyerPage.setIntro(form.getIntro());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      try {
        buyerPage.setImage(form.getImageFile().getBytes());
      } catch (IOException e) {
        log.error("이미지 처리 실패", e);
      }
    }

    int updated = buyerPageSVC.update(form.getPageId(), buyerPage);
    log.info("🔄 BuyerPage 수정 결과: {}건", updated);

    // Member 정보 수정
    Member member = new Member();
    member.setMemberId(memberId);
    member.setName(form.getName());
    member.setTel(form.getTel());
    member.setZonecode(form.getZonecode());
    member.setAddress(form.getAddress());
    member.setDetailAddress(form.getDetailAddress());

    String noti = form.getNotification();
    if (!"Y".equals(noti)) {
      noti = "N";
    }
    member.setNotification(noti);

    if (form.getPasswd() != null && !form.getPasswd().trim().isEmpty()) {
      member.setPasswd(form.getPasswd());
    }

    memberSVC.modify(memberId, member);
    log.info("✅ Member 정보 수정 완료: {}", member);

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
