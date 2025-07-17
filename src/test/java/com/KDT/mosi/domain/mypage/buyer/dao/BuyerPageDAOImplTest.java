package com.KDT.mosi.domain.mypage.buyer.dao;

import com.KDT.mosi.domain.entity.BuyerPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@Slf4j
class BuyerPageDAOImplTest {

  @Autowired
  BuyerPageDAO buyerPageDAO;

  @Test
  @DisplayName("구매자 회원정보 수정")
  void updateById() {

    Long pageId = 3L;
    BuyerPage buyerPage = new BuyerPage();

    buyerPage.setMemberId(66L);
    buyerPage.setIntro("클레오");
    buyerPage.setNickname("세상에서 제일가는");
    buyerPage.setDetailAddress("107-502");

    int row = buyerPageDAO.updateById(pageId, buyerPage);

    Optional<BuyerPage> optBuyer = buyerPageDAO.findById(pageId);
    BuyerPage modifiedBuyerPage = optBuyer.orElseThrow();


  }

}