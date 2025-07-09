package com.KDT.mosi.domain.member.dao;

import com.KDT.mosi.domain.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class MemberDAOTest {

  @Autowired
  MemberDAO memberDAO;

  @Test
  @DisplayName("회원 저장")
  void save() {
    // given
    Member member = new Member();
    member.setEmail("testuser@mosi.com");
    member.setPasswd("1234");
    member.setName("테스트사용자");
    member.setTel("010-0000-1234");
    member.setNickname("테유");
    member.setGender("남자");
    member.setAddress("서울시 강남구");
    member.setBirthDate(LocalDate.of(1995, 3, 15));
    member.setZonecode("06000");
    member.setDetailAddress("테스트상세주소");

    // when
    Long savedId = memberDAO.save(member);

    // then
    Optional<Member> finded = memberDAO.findById(savedId);
    assertThat(finded).isPresent();
    assertThat(finded.get().getEmail()).isEqualTo("testuser@mosi.com");
    assertThat(finded.get().getNickname()).isEqualTo("테유");
  }

  @Test
  @DisplayName("이메일로 회원 조회")
  void findByEmail() {
    // given
    Member member = new Member();
    member.setEmail("emailtest@mosi.com");
    member.setPasswd("1234");
    member.setName("이메일테스트");
    member.setTel("010-0000-0000");
    member.setNickname("이메일닉네임");
    member.setGender("남자");
    member.setAddress("서울시 테스트구");
    member.setBirthDate(LocalDate.of(1990, 1, 1));
    member.setZonecode("12345");
    member.setDetailAddress("상세주소1");

    Long savedId = memberDAO.save(member);

    // when
    Optional<Member> found = memberDAO.findByEmail("emailtest@mosi.com");

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getMemberId()).isEqualTo(savedId);
    assertThat(found.get().getEmail()).isEqualTo("emailtest@mosi.com");
  }

  @Test
  @DisplayName("회원 ID로 조회")
  void findById() {
    // given
    Member member = new Member();
    member.setEmail("idtest@mosi.com");
    member.setPasswd("1234");
    member.setName("아이디테스트");
    member.setTel("010-1111-1111");
    member.setNickname("아이디닉네임");
    member.setGender("여자");
    member.setAddress("서울시 테스트로");
    member.setBirthDate(LocalDate.of(1995, 5, 5));
    member.setZonecode("54321");
    member.setDetailAddress("상세주소2");

    Long savedId = memberDAO.save(member);

    // when
    Optional<Member> found = memberDAO.findById(savedId);

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("idtest@mosi.com");
    assertThat(found.get().getNickname()).isEqualTo("아이디닉네임");
  }

  @Test
  @DisplayName("이메일 존재 여부 확인")
  void isExistEmail() {
    // given
    Member member = new Member();
    member.setEmail("exist@mosi.com");
    member.setPasswd("1234");
    member.setName("존재테스트");
    member.setTel("010-2222-2222");
    member.setNickname("존재닉네임");
    member.setGender("남자");
    member.setAddress("서울시 존재구");
    member.setBirthDate(LocalDate.of(2000, 12, 31));
    member.setZonecode("67890");
    member.setDetailAddress("상세주소3");

    memberDAO.save(member);

    // when
    boolean exists = memberDAO.isExistEmail("exist@mosi.com");
    boolean notExists = memberDAO.isExistEmail("nonexist@mosi.com");

    // then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("회원 정보 수정")
  void update() {
    // given - 회원 저장
    Member member = new Member();
    member.setEmail("updateTest@mosi.com");
    member.setPasswd("1111");
    member.setTel("010-1111-1111");
    member.setName("업뎃전");
    member.setNickname("before");
    member.setGender("남자");
    member.setAddress("서울시");
    member.setZonecode("11111");
    member.setDetailAddress("상세주소");
    member.setBirthDate(LocalDate.of(1990, 1, 1));

    Long savedId = memberDAO.save(member);

    // when - 회원 정보 수정
    Member updateParam = new Member();
    updateParam.setMemberId(savedId);
    updateParam.setEmail("updateTest@mosi.com");  // email은 고정
    updateParam.setPasswd("2222");
    updateParam.setTel("010-2222-2222");
    updateParam.setName("업뎃후");
    updateParam.setNickname("after");
    updateParam.setGender("여자");
    updateParam.setAddress("부산시");
    updateParam.setZonecode("22222");
    updateParam.setDetailAddress("수정된 주소");
    updateParam.setBirthDate(LocalDate.of(2000, 2, 2));

    int updatedCnt = memberDAO.update(updateParam);

    // then - 수정 결과 확인
    assertThat(updatedCnt).isEqualTo(1);

    Optional<Member> updated = memberDAO.findById(savedId);
    assertThat(updated).isPresent();
    assertThat(updated.get().getNickname()).isEqualTo("after");
    assertThat(updated.get().getTel()).isEqualTo("010-2222-2222");
    assertThat(updated.get().getGender()).isEqualTo("여자");
    assertThat(updated.get().getAddress()).isEqualTo("부산시");
  }


  @Test
  @DisplayName("비밀번호 재설정")
  void updatePassword() {
    // given
    Member member = new Member();
    member.setEmail("pwreset@mosi.com");
    member.setPasswd("oldpass");
    member.setTel("010-1234-0000");
    member.setName("이름");
    member.setNickname("닉네임");
    member.setGender("남자");
    member.setAddress("서울시");
    member.setZonecode("12345");
    member.setDetailAddress("주소 상세");
    member.setBirthDate(LocalDate.of(1990, 1, 1));

    Long savedId = memberDAO.save(member);

    // when - 비밀번호 재설정
    String newPassword = "newpass123";
    int updated = memberDAO.updatePassword("pwreset@mosi.com", newPassword);

    // then
    assertThat(updated).isEqualTo(1);

    Optional<Member> updatedMember = memberDAO.findById(savedId);
    assertThat(updatedMember).isPresent();
    assertThat(updatedMember.get().getPasswd()).isEqualTo(newPassword);
  }

  @Test
  @DisplayName("전화번호로 이메일 찾기")
  void findEmailByTel() {
    // given
    Member member = new Member();
    member.setEmail("teluser@mosi.com");
    member.setPasswd("1234");
    member.setName("전화테스트");
    member.setTel("010-1234-5678");
    member.setNickname("전화유저");
    member.setGender("남자");
    member.setBirthDate(LocalDate.of(1990, 5, 20));
    member.setZonecode("12345");
    member.setAddress("서울시 강남구");
    member.setDetailAddress("역삼동");

    Long savedId = memberDAO.save(member);

    // when
    Optional<String> foundEmail = memberDAO.findEmailByTel("010-1234-5678");

    // then
    assertThat(foundEmail).isPresent();
    assertThat(foundEmail.get()).isEqualTo("teluser@mosi.com");
  }


}