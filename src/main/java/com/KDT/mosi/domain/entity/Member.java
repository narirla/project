package com.KDT.mosi.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
public class Member implements Serializable {
  private Long memberId;
  private String email;
  private String name;
  private String passwd;
  private String tel;
  private String nickname;
  private String gender;
  private String address;
  private LocalDate birthDate;   // ✅ 생년월일 추가
  private byte[] pic;
  private Timestamp createDate;
  private Timestamp updateDate;
  private String zonecode;
  private String detailAddress;
  private String notification;   // 알림 설정 (Y: 수신, N: 수신 안 함)

}
