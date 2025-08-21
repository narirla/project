package com.KDT.mosi.web.form.member;

import com.KDT.mosi.web.validation.MaxAge;
import com.KDT.mosi.web.validation.MinAge;
import com.KDT.mosi.web.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 회원가입 폼
 * - 가입 시 사용되는 필드 정보를 담은 폼 객체
 */
@Data
public class MemberJoinForm {

  /** 이메일 (필수, 중복 확인 대상) */
  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  /** 이름 (필수) */
  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  /** 비밀번호 (필수, 8~12자) */
  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 12, message = "비밀번호는 8자 이상 12자 이내여야 합니다.")
  @ValidPassword(message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 모두 포함해야 하며 동일 문자를 3회 이상 반복할 수 없습니다.")
  private String passwd;

  /** 비밀번호 확인 */
  private String confirmPasswd;

  /** 전화번호 (필수, 하이픈 포함) */
  // ✅ 변경: 프론트(010/011/016/017/018/019 지원)와 정합성 맞춤
  //  - UI는 010-1234-5678 형태로 제출하므로 하이픈 필수
  //  - 필수 입력이면 @NotBlank 추가
  @NotBlank(message = "전화번호는 필수 입력값입니다.")
  @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "전화번호 형식은 010-0000-0000과 같이 입력하세요.")
  private String tel;

  /** 닉네임 (2~30자, 중복 확인 대상) */
  @Size(min = 2, max = 30, message = "닉네임은 최소 2자, 최대 30자까지 입력 가능합니다.")
  private String nickname;

  /** 성별 ('남자' | '여자' | '비공개') */
  // ✅ 변경: '비공개' 선택 허용. 선택항목이면 ? 유지(빈 값 허용)
  @Pattern(regexp = "^(남자|여자|비공개)?$", message = "성별은 '남자', '여자', '비공개' 중 하나여야 합니다.")
  private String gender;

  /** 기본 주소 */
  private String address;

  // 선택 입력: 값이 있을 때만 나이 검증
  @MinAge(value = 14, message = "만 14세 이상만 가입할 수 있습니다.", pattern = "yyyy-MM-dd", zoneId = "Asia/Seoul")
  @MaxAge(value = 120, message = "생년월일을 다시 확인해주세요.", pattern = "yyyy-MM-dd", zoneId = "Asia/Seoul")
  private String birthDate;


  /** 프로필 이미지 파일 */
  private MultipartFile picFile;

  /** 선택한 역할 ID 목록 (예: R01, R02) */
  private List<String> roles;

  /** 동의한 약관 ID 목록 */
  private List<Long> agreedTermsIds;

  /** 우편번호 */
  private String zonecode;

  /** 상세 주소 */
  private String detailAddress;
}
