package com.KDT.mosi.domain.mypage.seller.svc;

import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDao;
import com.KDT.mosi.domain.mypage.seller.svc.SellerSVC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class SellerSVCImpl implements SellerSVC {

  private final SellerPageDao sellerPageDao;

  public SellerSVCImpl(SellerPageDao sellerPageDao) {
    this.sellerPageDao = sellerPageDao;
  }

  @Override
  @Transactional
  public void createSellerPage(SellerPage sellerPage) {
    sellerPageDao.insert(sellerPage);
  }

  @Override
  public Optional<SellerPage> getSellerPageByMemberId(Long memberId) {
    return sellerPageDao.findByMemberId(memberId);
  }

  @Override
  @Transactional
  public void updateSellerPage(SellerPage sellerPage) {
    sellerPageDao.update(sellerPage);
  }

  @Override
  @Transactional
  public void deleteSellerPage(Long pageId) {
    sellerPageDao.delete(pageId);
  }
}
