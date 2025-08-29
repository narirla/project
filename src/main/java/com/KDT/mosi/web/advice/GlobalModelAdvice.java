package com.KDT.mosi.web.advice;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

  private final SellerPageSVC sellerPageSVC;

  @ModelAttribute
  public void addGlobalAttributes(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");

    // 로그인한 경우만 실행
    if (loginMember != null) {
      sellerPageSVC.findByMemberId(loginMember.getMemberId()).ifPresent(sellerPage -> {
        model.addAttribute("sellerPage", sellerPage);

        // 프로필 이미지도 Base64로 변환해서 추가
        if (sellerPage.getImage() != null && sellerPage.getImage().length > 0) {
          String base64SellerImage =
              "data:image/png;base64," + Base64.getEncoder().encodeToString(sellerPage.getImage());
          model.addAttribute("sellerImage", base64SellerImage);
        } else {
          model.addAttribute("sellerImage", null);
        }
      });
    }
  }
}
