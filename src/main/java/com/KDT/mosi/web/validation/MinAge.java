package com.KDT.mosi.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinAgeValidatorForString.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {
  String message() default "만 {value}세 이상이어야 합니다.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  int value();                        // 최소 연령
  String pattern() default "yyyy-MM-dd"; // String 파싱 패턴
  String zoneId() default "Asia/Seoul";  // 기준 타임존
}
