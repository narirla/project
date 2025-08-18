package com.KDT.mosi.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxAgeValidatorForString.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxAge {
  String message() default "만 {value}세 이하여야 합니다.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  int value();                        // 최대 연령
  String pattern() default "yyyy-MM-dd";
  String zoneId() default "Asia/Seoul";
}
