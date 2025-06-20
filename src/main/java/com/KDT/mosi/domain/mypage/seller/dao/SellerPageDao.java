package com.KDT.mosi.domain.mypage.seller.dao;

import com.KDT.mosi.domain.entity.SellerPage;
import java.util.Optional;

public interface SellerPageDao {
  void insert(SellerPage sellerPage);
  Optional<SellerPage> findByMemberId(Long memberId);
  void update(SellerPage sellerPage);
  void delete(Long pageId);
}
