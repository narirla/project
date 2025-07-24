package com.KDT.mosi.web.form.mypage.sellerpage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SellerPageUpdateForm {

  // ✅ 페이지 ID (필수)
  @NotNull(message = "페이지 ID는 필수입니다.")
  private Long pageId;

  // ✅ 회원 ID (필수)
  @NotNull(message = "회원 ID는 필수입니다.")
  private Long memberId;

  // ✅ 닉네임 (2자 이상 30자 이하)
  @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이내여야 합니다.")
  private String nickname;

  // ✅ 새 비밀번호
  private String passwd;

  // ✅ 새 비밀번호 확인
  private String confirmPasswd;

  // ✅ 현재 비밀번호 (변경 시 확인용)
  private String currentPasswd;

  // ✅ 전화번호
  private String tel;

  // ✅ 자기소개 (최대 150자)
  @Size(max = 150, message = "자기소개는 150자 이내로 입력해주세요.")
  private String intro;

  // ✅ DB 저장용 이미지 (byte[])
  private byte[] image;

  // ✅ 우편번호
  private String zonecode;

  // ✅ 기본주소
  private String address;

  // ✅ 상세주소
  private String detailAddress;

  // ✅ 마케팅 수신 동의 여부
  private String notification;

  // ✅ 업로드한 프로필 이미지 파일 (UI와 연동)
  private MultipartFile imageFile;

  // ✅ 최근 판매 상품명
  private String recentProduct;

  // ✅ 총 판매 건수
  private Integer salesCount;

  // ✅ 프로필 이미지 삭제 여부 체크박스 (true: 삭제 요청)
  private Boolean deleteImage;
}
