package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.member.dao.MemberRoleDAO;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import com.KDT.mosi.domain.terms.dao.TermsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class MemberSVCImplTest {

  private MemberDAO memberDAO = Mockito.mock(MemberDAO.class);
  private MemberRoleDAO memberRoleDAO = Mockito.mock(MemberRoleDAO.class);
  private TermsDAO termsDAO = Mockito.mock(TermsDAO.class);
  private BuyerPageDAO buyerPageDAO = Mockito.mock(BuyerPageDAO.class);
  private MemberSVC memberSVC;


  @BeforeEach
  void init() {
    memberSVC = new MemberSVCImpl(memberDAO, termsDAO, buyerPageDAO, memberRoleDAO);
  }

  @Test
  @DisplayName("회원 저장 (단일)")
  void joinMember() {
    // given
    Member member = new Member();
    member.setEmail("svc@mosi.com");
    member.setPasswd("1234");
    member.setName("서비스유저");
    given(memberDAO.save(member)).willReturn(100L);

    // when
    Long savedId = memberSVC.join(member);

    // then
    assertThat(savedId).isEqualTo(100L);
    then(memberDAO).should().save(member);
  }

  @Test
  @DisplayName("이메일 중복 확인")
  void isExistEmail() {
    // given
    given(memberDAO.findByEmail("dup@mosi.com"))
        .willReturn(Optional.of(new Member()));

    // when
    boolean exists = memberSVC.isExistEmail("dup@mosi.com");

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("비밀번호 재설정")
  void resetPassword() {
    // given
    given(memberDAO.updatePassword("reset@mosi.com", "newpass"))
        .willReturn(1);

    // when
    boolean result = memberSVC.resetPassword("reset@mosi.com", "newpass");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("이메일로 회원 조회")
  void findByEmail() {
    // given
    Member member = new Member();
    member.setEmail("svc@mosi.com");
    given(memberDAO.findByEmail("svc@mosi.com"))
        .willReturn(Optional.of(member));

    // when
    Optional<Member> found = memberSVC.findByEmail("svc@mosi.com");

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("svc@mosi.com");
  }


  @Test
  @DisplayName("회원 탈퇴 시 BuyerPage 먼저 삭제")
  void deleteMemberWithBuyerPage() {
    // given
    Long memberId = 10L;
    given(buyerPageDAO.deleteByMemberId(memberId)).willReturn(1);
    given(memberDAO.deleteById(memberId)).willReturn(1);

    // when
    int deleted = memberSVC.deleteById(memberId);

    // then
    assertThat(deleted).isEqualTo(1);
    then(buyerPageDAO).should().deleteByMemberId(memberId); // ✅ 선행 삭제 확인
    then(memberDAO).should().deleteById(memberId);          // ✅ 회원 삭제 확인
  }
}
