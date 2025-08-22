package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class SidebarController {

  private final SellerPageSVC sellerPageSVC;

  /**
   *
   * @param session
   * @return
   */
  @ModelAttribute("sellerSidebar")
  public SellerPage loadSellerSidebar(HttpSession session){
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) return null;

    return sellerPageSVC.findByMemberId(loginMember.getMemberId())
        .orElse(null);
  }

}
