package com.KDT.mosi.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MaxAgeValidatorForString implements ConstraintValidator<MaxAge, String> {
  private int max;
  private DateTimeFormatter fmt;
  private ZoneId zoneId;

  @Override
  public void initialize(MaxAge anno) {
    this.max = anno.value();
    this.fmt = DateTimeFormatter.ofPattern(anno.pattern());
    this.zoneId = ZoneId.of(anno.zoneId());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return true; // 선택 항목: 비어있으면 통과
    try {
      LocalDate birth = LocalDate.parse(value, fmt);
      LocalDate today = LocalDate.now(zoneId);
      if (birth.isAfter(today)) return false;
      int age = Period.between(birth, today).getYears();
      return age <= max;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
