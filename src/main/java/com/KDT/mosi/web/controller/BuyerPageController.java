package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.buyer.svc.BuyerPageSVC;
import com.KDT.mosi.web.form.mypage.buyerpage.BuyerPageSaveForm;
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
    String loginEmail = auth.getName();
    return memberSVC.findByEmail(loginEmail).orElseThrow().getMemberId();
  }

  // ✅ 마이페이지 조회
  @GetMapping("/{memberId}")
  public String view(@PathVariable Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    return buyerPageSVC.findByMemberId(memberId)
        .map(page -> {
          model.addAttribute("buyerPage", page);
          return "mypage/buyerpage/viewBuyerPage";
        })
        .orElse("redirect:/mypage/buyer/add");
  }

  // ✅ 등록 폼
  @GetMapping("/add")
  public String addForm(Model model) {
    Long loginMemberId = getLoginMemberId();

    // 이미 등록된 경우, 수정 화면으로 유도
    if (buyerPageSVC.findByMemberId(loginMemberId).isPresent()) {
      return "redirect:/mypage/buyer/" + loginMemberId + "/edit";
    }

    BuyerPageSaveForm form = new BuyerPageSaveForm();
    form.setMemberId(loginMemberId);
    model.addAttribute("form", form);
    return "mypage/buyerpage/addBuyerPage";
  }


  // ✅ 등록 처리
  @PostMapping("/add")
  public String add(@Valid @ModelAttribute("form") BuyerPageSaveForm form,
                    BindingResult bindingResult) {
    Long loginMemberId = getLoginMemberId();

    if (!loginMemberId.equals(form.getMemberId())) {
      return "error/403";
    }

    if (bindingResult.hasErrors()) {
      return "mypage/buyerpage/addBuyerPage";
    }

    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setMemberId(form.getMemberId());
    buyerPage.setIntro(form.getIntro());
    buyerPage.setRecentOrder(form.getRecentOrder());
    buyerPage.setPoint(form.getPoint());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      try {
        buyerPage.setImage(form.getImageFile().getBytes());
      } catch (IOException e) {
        log.error("이미지 처리 실패", e);
      }
    }

    buyerPageSVC.create(buyerPage);
    return "redirect:/mypage/buyer/" + form.getMemberId();
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
        .header(HttpHeaders.LOCATION, "/images/default-profile.png")
        .build();
  }

  // ✅ 수정 폼
  @GetMapping("/{memberId}/edit")
  public String editForm(@PathVariable Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    return buyerPageSVC.findByMemberId(memberId)
        .map(entity -> {
          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(entity.getPageId());
          form.setMemberId(entity.getMemberId());
          form.setNickname(entity.getNickname());
          form.setIntro(entity.getIntro());

          model.addAttribute("form", form);
          return "mypage/buyerpage/editBuyerPage";
        })
        .orElse("redirect:/mypage/buyer/add");
  }

  // ✅ 수정 처리
  @PostMapping("/{memberId}")
  public String update(@PathVariable Long memberId,
                       @Valid @ModelAttribute("form") BuyerPageUpdateForm form,
                       BindingResult bindingResult) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    if (bindingResult.hasErrors()) {
      return "mypage/buyerpage/editBuyerPage";
    }

    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setPageId(form.getPageId());
    buyerPage.setMemberId(form.getMemberId());
    buyerPage.setNickname(form.getNickname());
    buyerPage.setIntro(form.getIntro());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      try {
        buyerPage.setImage(form.getImageFile().getBytes());
      } catch (IOException e) {
        log.error("이미지 처리 실패", e);
      }
    }

    buyerPageSVC.update(form.getPageId(), buyerPage);
    return "redirect:/mypage/buyer/" + memberId;
  }

  // ✅ 삭제 처리
  @PostMapping("/{pageId}/del")
  public String delete(@PathVariable Long pageId) {
    Long loginMemberId = getLoginMemberId();

    // 🔒 본인 확인: pageId → memberId 조회 후 비교
    Optional<BuyerPage> optional = buyerPageSVC.findById(pageId);
    if (optional.isEmpty() || !optional.get().getMemberId().equals(loginMemberId)) {
      return "error/403";
    }

    buyerPageSVC.delete(pageId);
    return "redirect:/";
  }
}
