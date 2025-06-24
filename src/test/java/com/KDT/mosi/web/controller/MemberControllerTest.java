package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.terms.svc.TermsSVC;
import com.KDT.mosi.web.form.member.MemberEditForm;
import com.KDT.mosi.web.form.member.MemberJoinForm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;


class MemberControllerTest {

  private MemberSVC memberSVC;
  private RoleDAO roleDAO;
  private TermsSVC termsSVC;
  private BCryptPasswordEncoder encoder;
  private MemberController memberController;

  @BeforeEach
  void setUp() {
    memberSVC = Mockito.mock(MemberSVC.class);
    roleDAO = Mockito.mock(RoleDAO.class);
    termsSVC = Mockito.mock(TermsSVC.class);
    encoder = Mockito.mock(BCryptPasswordEncoder.class);
    memberController = new MemberController(memberSVC, roleDAO, termsSVC, encoder);

    // SecurityContext 인증 주입
    String testEmail = "test@mosi.com";
    Authentication auth = new UsernamePasswordAuthenticationToken(testEmail, null, new ArrayList<>());
    SecurityContextHolder.getContext().setAuthentication(auth);

    // memberSVC.findByEmail(...) 결과 설정
    Member mockMember = new Member();
    mockMember.setMemberId(1L);
    mockMember.setEmail(testEmail);
    given(memberSVC.findByEmail(testEmail)).willReturn(Optional.of(mockMember));
  }


  @Test
  @DisplayName("회원가입 폼 요청")
  void joinForm() {
    Model model = Mockito.mock(Model.class);
    String view = memberController.joinForm(model);
    assertThat(view).isEqualTo("member/joinForm");
  }

  @Test
  @DisplayName("회원가입 처리")
  void join() {
    MemberJoinForm form = new MemberJoinForm();
    form.setEmail("test@mosi.com");
    form.setPasswd("1234");
    form.setConfirmPasswd("1234");

    BindingResult result = Mockito.mock(BindingResult.class);
    given(result.hasErrors()).willReturn(false);

    given(memberSVC.isExistEmail("test@mosi.com")).willReturn(false);

    // 기대하는 저장 ID를 명시
    given(memberSVC.join(any(Member.class), anyList(), anyList()))
        .willReturn(100L); // 원하는 리다이렉트 ID

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    String view = memberController.join(form, result, request);

    // 기대하는 리다이렉트 주소로 검증
    assertThat(view).isEqualTo("redirect:/members/100");
  }


  @Test
  @DisplayName("이메일 중복 확인")
  void emailCheck() {
    // given
    String testEmail = "test@mosi.com";
    given(memberSVC.isExistEmail(testEmail)).willReturn(true); // 또는 false

    // when
    ResponseEntity<Map<String, Boolean>> response = memberController.emailCheck(testEmail);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("exists")).isTrue(); // 또는 isFalse()로 반대 테스트 가능
  }



  @Test
  @DisplayName("회원 조회 화면")
  void view() {
    Long memberId = 1L;
    Model model = Mockito.mock(Model.class);
    Member member = new Member();
    member.setMemberId(memberId);
    member.setEmail("user@mosi.com");

    given(memberSVC.findById(memberId)).willReturn(Optional.of(member));

    String view = memberController.view(memberId, model);
    assertThat(view).isEqualTo("member/viewMember");
  }

  @Test
  @DisplayName("프로필 이미지 다운로드")
  void downloadProfilePic() {
    // 해당 메서드는 ResponseEntity를 반환하므로,
    // 단위 테스트보단 통합 테스트에 적합
    // 최소한 NotNull 확인 정도만 가능
    var response = memberController.downloadProfilePic(1L);
    assertThat(response).isNotNull();
  }

  @Test
  @DisplayName("회원 정보 수정 폼")
  void editForm() {
    Long memberId = 1L;
    Model model = Mockito.mock(Model.class);
    Member member = new Member();
    member.setMemberId(memberId);
    given(memberSVC.findById(memberId)).willReturn(Optional.of(member));

    String view = memberController.editForm(memberId, model);
    assertThat(view).isEqualTo("member/editMember");
  }

  @Test
  @DisplayName("회원 정보 수정 처리")
  void edit() {
    // SecurityContext 설정
    Authentication auth = new UsernamePasswordAuthenticationToken("test@mosi.com", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    // given
    MemberEditForm form = new MemberEditForm();
    form.setPasswd("1234");
    form.setConfirmPasswd("1234");
    form.setName("홍길동");
    form.setTel("010-1234-5678");
    form.setNickname("길동이");
    form.setGender("남");
    form.setAddress("서울시 강남구");
    form.setZonecode("12345");
    form.setDetailAddress("101동 202호");

    BindingResult result = Mockito.mock(BindingResult.class);
    given(result.hasErrors()).willReturn(false);

    Member member = new Member();
    member.setMemberId(1L);
    member.setEmail("test@mosi.com");

    given(memberSVC.findByEmail("test@mosi.com")).willReturn(Optional.of(member));
    given(memberSVC.findById(1L)).willReturn(Optional.of(member));
    given(memberSVC.hasRole(1L, "R01")).willReturn(false);

    // when
    String view = memberController.edit(1L, form, result, Mockito.mock(Model.class));

    // then
    assertThat(view).isEqualTo("redirect:/members/1");

    // 인증정보 제거
    SecurityContextHolder.clearContext();
  }


  @Test
  @DisplayName("프로필 이미지 없음 → 404 반환")
  void downloadProfilePic_notFound() {
    Long memberId = 2L;
    Member member = new Member();
    member.setMemberId(memberId);
    member.setPic(null); // 이미지 없음

    given(memberSVC.findById(memberId)).willReturn(Optional.of(member));

    var response = memberController.downloadProfilePic(memberId);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }


}
