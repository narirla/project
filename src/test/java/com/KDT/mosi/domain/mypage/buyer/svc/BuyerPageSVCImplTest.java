package com.KDT.mosi.domain.mypage.buyer.svc;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class BuyerPageSVCImplTest {

  private BuyerPageDAO buyerPageDAO;
  private BuyerPageSVC buyerPageSVC;

  @BeforeEach
  void setup() {
    buyerPageDAO = Mockito.mock(BuyerPageDAO.class);
    buyerPageSVC = new BuyerPageSVCImpl(buyerPageDAO);
  }

  @Test
  @DisplayName("마이페이지 저장")
  void saveBuyerPage() {
    // given
    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setMemberId(1L);
    given(buyerPageDAO.save(buyerPage)).willReturn(10L);

    // when
    Long pageId = buyerPageSVC.create(buyerPage);

    // then
    assertThat(pageId).isEqualTo(10L);
    then(buyerPageDAO).should().save(buyerPage);
  }

  @Test
  @DisplayName("회원 ID로 마이페이지 조회")
  void findByMemberId() {
    // given
    BuyerPage page = new BuyerPage();
    page.setPageId(10L);
    page.setMemberId(1L);
    given(buyerPageDAO.findByMemberId(1L)).willReturn(Optional.of(page));

    // when
    Optional<BuyerPage> found = buyerPageSVC.findByMemberId(1L);

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getPageId()).isEqualTo(10L);
  }

  @Test
  @DisplayName("마이페이지 수정")
  void updateBuyerPage() {
    // given
    BuyerPage updated = new BuyerPage();
    updated.setIntro("자기소개 수정됨");
    given(buyerPageDAO.updateById(10L, updated)).willReturn(1);

    // when
    int result = buyerPageSVC.update(10L, updated);

    // then
    assertThat(result).isEqualTo(1);
  }

  @Test
  @DisplayName("마이페이지 삭제")
  void deleteBuyerPage() {
    // given
    given(buyerPageDAO.deleteByMemberId(1L)).willReturn(1);

    // when
    int deleted = buyerPageSVC.deleteByMemberId(1L);

    // then
    assertThat(deleted).isEqualTo(1);
  }

  @Test
  @DisplayName("마이페이지 ID로 조회")
  void findByPageId() {
    // given
    BuyerPage page = new BuyerPage();
    page.setPageId(10L);
    given(buyerPageDAO.findById(10L)).willReturn(Optional.of(page));

    // when
    Optional<BuyerPage> found = buyerPageSVC.findById(10L);

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getPageId()).isEqualTo(10L);
  }
}
