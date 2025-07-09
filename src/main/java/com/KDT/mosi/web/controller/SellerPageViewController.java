package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.svc.SellerSVC;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SellerPageViewController {

  private final SellerSVC sellerSVC;

  public SellerPageViewController(SellerSVC sellerSVC) {
    this.sellerSVC = sellerSVC;
  }

  @GetMapping("/mypage/seller/{memberId}/view")
  public String viewSellerPage(@PathVariable Long memberId, Model model) {
    SellerPage sellerPage = sellerSVC.getSellerPageByMemberId(memberId).orElse(null);
    model.addAttribute("sellerPage", sellerPage);
    return "mypage/sellerpage/view"; // templates/mypage/seller/view.html
  }

  @GetMapping("/seller-page/create-form")
    public String createForm() {
      return "mypage/sellerpage/create";
    }
  }


