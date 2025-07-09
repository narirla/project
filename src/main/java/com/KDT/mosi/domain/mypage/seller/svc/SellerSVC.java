package com.KDT.mosi.domain.mypage.seller.svc;

import com.KDT.mosi.domain.entity.SellerPage;

import java.util.Optional;

public interface SellerSVC {
  void createSellerPage(SellerPage sellerPage);

  Optional<SellerPage> getSellerPageByMemberId(Long memberId);

  void updateSellerPage(SellerPage sellerPage);

  void deleteSellerPage(Long pageId);
}