package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.BuyerPage;
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

  // ✅ 마이페이지 조회
  @GetMapping("/{memberId}")
  public String view(@PathVariable Long memberId, Model model) {
    Optional<BuyerPage> optional = buyerPageSVC.findByMemberId(memberId);
    if (optional.isPresent()) {
      model.addAttribute("buyerPage", optional.get());
      return "mypage/buyer/view";
    } else {
      return "redirect:/mypage/buyer/add";
    }
  }

  // ✅ 등록 폼
  @GetMapping("/add")
  public String addForm(Model model) {
    model.addAttribute("form", new BuyerPageSaveForm());
    return "mypage/buyer/add";
  }

  // ✅ 등록 처리
  @PostMapping("/add")
  public String add(@Valid @ModelAttribute("form") BuyerPageSaveForm form,
                    BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "mypage/buyer/add";
    }

    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setMemberId(form.getMemberId());
    buyerPage.setIntro(form.getIntro());
    buyerPage.setRecentOrder(form.getRecentOrder());
    buyerPage.setPoint(form.getPoint());

    // 이미지 처리
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

  //이미지 추가
  @GetMapping("/{memberId}/image")
  public ResponseEntity<byte[]> image(@PathVariable Long memberId) {
    // DB에서 BuyerPage 조회
    Optional<BuyerPage> optional = buyerPageSVC.findByMemberId(memberId);

    // 이미지가 존재하는 경우
    if (optional.isPresent() && optional.get().getImage() != null) {
      byte[] image = optional.get().getImage();
      MediaType mediaType = MediaType.IMAGE_JPEG; // 기본값

      try {
        // 이미지 MIME 타입 자동 감지
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
        if (contentType != null) {
          mediaType = MediaType.parseMediaType(contentType);
        }
      } catch (IOException e) {
        log.warn("이미지 content type 분석 실패, 기본 JPEG 사용");
      }

      // 이미지 데이터와 MIME 타입을 포함하여 응답
      return ResponseEntity.ok()
          .contentType(mediaType)
          .body(image);
    }

    // 이미지가 없을 경우: 기본 이미지로 리디렉션
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, "/images/default-profile.png")
        .build();
  }


  // ✅ 수정 폼
  @GetMapping("/{memberId}/edit")
  public String editForm(@PathVariable Long memberId, Model model) {
    Optional<BuyerPage> optional = buyerPageSVC.findByMemberId(memberId);
    if (optional.isPresent()) {
      BuyerPage entity = optional.get();

      // Entity → UpdateForm 변환
      BuyerPageUpdateForm form = new BuyerPageUpdateForm();
      form.setPageId(entity.getPageId());
      form.setMemberId(entity.getMemberId());
      form.setIntro(entity.getIntro());
      form.setRecentOrder(entity.getRecentOrder());
      form.setPoint(entity.getPoint());

      model.addAttribute("form", form);
      return "mypage/buyer/edit";
    } else {
      return "redirect:/mypage/buyer/add";
    }
  }

  // ✅ 수정 처리
  @PostMapping("/{memberId}")
  public String update(@PathVariable Long memberId,
                       @Valid @ModelAttribute("form") BuyerPageUpdateForm form,
                       BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "mypage/buyer/edit";
    }

    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setPageId(form.getPageId());
    buyerPage.setMemberId(form.getMemberId());
    buyerPage.setIntro(form.getIntro());
    buyerPage.setRecentOrder(form.getRecentOrder());
    buyerPage.setPoint(form.getPoint());

    // 이미지 처리
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

  // ✅ 삭제
  @PostMapping("/{pageId}/del")
  public String delete(@PathVariable Long pageId) {
    buyerPageSVC.delete(pageId);
    return "redirect:/";
  }
}
