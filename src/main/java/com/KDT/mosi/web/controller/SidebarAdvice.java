package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;

@ControllerAdvice
@RequiredArgsConstructor
public class SidebarAdvice {

  private final SellerPageSVC sellerPageSVC;

  @ModelAttribute
  public void addSidebarInfo(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    String loginRole = (String) session.getAttribute("loginRole");

    model.addAttribute("loginMember", loginMember);
    model.addAttribute("loginRole", loginRole);

    if (loginMember != null) {
      // 구매자 이미지 (Member.pic)
      if ("BUYER".equals(loginRole) && loginMember.getPic() != null) {
        String buyerImage = "data:image/png;base64," +
            Base64.getEncoder().encodeToString(loginMember.getPic());
        model.addAttribute("sidebarImage", buyerImage);
      }

      // 판매자 이미지 (SellerPage.image)
      if ("SELLER".equals(loginRole)) {
        sellerPageSVC.findByMemberId(loginMember.getMemberId()).ifPresent(sellerPage -> {
          byte[] imageBytes = sellerPage.getImage();
          if (imageBytes != null && imageBytes.length > 0) {
            String sellerImage = "data:image/png;base64," +
                Base64.getEncoder().encodeToString(imageBytes);
            model.addAttribute("sidebarImage", sellerImage);
          }
          model.addAttribute("sellerNickname", sellerPage.getNickname());
        });
      }
    }
  }
}
