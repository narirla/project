package com.KDT.mosi.domain.mypage.buyer.svc;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerPageSVCImpl implements BuyerPageSVC{

  private final BuyerPageDAO buyerPageDAO;

  @Override
  public Long create(BuyerPage buyerPage) {
    return buyerPageDAO.save(buyerPage);
  }

  @Override
  public Optional<BuyerPage> findByMemberId(Long memberId) {
    return buyerPageDAO.findByMemberId(memberId);
  }

  @Override
  public int update(Long pageId, BuyerPage buyerPage) {
    return buyerPageDAO.updateById(pageId, buyerPage);
  }

  @Override
  public int delete(Long pageId) {
    return buyerPageDAO.deleteById(pageId);
  }
<<<<<<< HEAD
=======

  @Override
  public Optional<BuyerPage> findById(Long pageId) {
    return buyerPageDAO.findById(pageId);
  }

>>>>>>> feature/member
}
