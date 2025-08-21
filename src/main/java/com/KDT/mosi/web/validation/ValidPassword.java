package com.KDT.mosi.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class) // 검증기 연결
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // 필드·메서드 파라미터에 사용
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

  // 🔹 기본 메시지 (국제화 키 사용 가능)
  String message() default "{password.invalid}";

  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  // 🔹 추가 속성 (기본값 설정)
  int min() default 8;        // 최소 길이
  int max() default 12;       // 최대 길이
  boolean allowWhitespace() default false; // 공백 허용 여부
}
