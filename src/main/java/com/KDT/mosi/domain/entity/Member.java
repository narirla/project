package com.KDT.mosi.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Member {

  private Long memberId;          //number(10),     -- 내부 관리 아이디
  private String email;           //varchar2(40)    -- 로그인 아이디
  private String name;            //varchar2(50)    -- 회원 이름
  private String passwd;          //varchar2(12)    -- 로그인 비밀번호
  private String tel;             //varchar2(13)    -- 연락처 ex)010-1234-5678
  private String nickname;        //varchar2(30)    -- 별칭
  private String gender;          //varchar2(6)     -- 성별
  private String  address;        //varchar2(200)   -- 주소
  private byte[] pic;             //blob            -- 사진
  private LocalDateTime create_date;     //timestamp  -- 생성일시
  private LocalDateTime update_date;     //timestamp  -- 수정일시

}