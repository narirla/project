package com.KDT.mosi.web.form.member;

<<<<<<< HEAD
import jakarta.validation.constraints.*;
=======
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
>>>>>>> feature/member
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class MemberJoinForm {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 12, message = "비밀번호는 8자 이상 12자 이내여야 합니다.")
  private String passwd;

<<<<<<< HEAD
=======
  private String confirmPasswd;

>>>>>>> feature/member
  @Pattern(regexp = "^(010-\\d{4}-\\d{4})?$", message = "전화번호 형식은 010-0000-0000입니다.")
  private String tel;

  @Size(max = 30, message = "닉네임은 최대 30자까지 입력 가능합니다.")
  private String nickname;

  @Pattern(regexp = "^(남자|여자)?$", message = "성별은 '남자' 또는 '여자'만 가능합니다.")
  private String gender;

  private String address;

<<<<<<< HEAD
  private MultipartFile picFile;

  // 선택한 역할 목록 (예: ["R01", "R02"])
  private List<String> roles;

  // 동의한 약관 ID 목록 (예: [1, 2])
  private List<Long> agreedTermsIds;
=======
  private String birthDate; // yyyy-MM-dd

  private MultipartFile picFile;

  private List<String> roles;

  private List<Long> agreedTermsIds;

  private String zonecode;
  private String detailAddress;
>>>>>>> feature/member
}
