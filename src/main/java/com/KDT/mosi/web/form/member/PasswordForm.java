package com.KDT.mosi.web.form.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordForm {
  @NotBlank(message = "현재 비밀번호를 입력하세요.")
  private String currentPassword;

  @NotBlank(message = "새 비밀번호를 입력하세요.")
  @Size(min=8, max=12, message="새 비밀번호는 8~12자여야 합니다.")
  private String newPassword;

  @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
  private String confirmPassword;
}
