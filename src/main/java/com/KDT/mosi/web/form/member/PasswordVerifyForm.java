package com.KDT.mosi.web.form.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordVerifyForm {

  @NotBlank(message = "비밀번호를 입력하세요.")
  private String currentPassword;
}
