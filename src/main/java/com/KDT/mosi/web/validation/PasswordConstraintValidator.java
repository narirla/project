package com.KDT.mosi.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

  private int min;
  private int max;
  private boolean allowWhitespace;

  // 대/소문자/숫자/특수문자 1개 이상, 공백 비허용 시 전체는 \S 사용
  private Pattern basePattern;

  @Override
  public void initialize(ValidPassword constraint) {
    this.min = constraint.min();
    this.max = constraint.max();
    this.allowWhitespace = constraint.allowWhitespace();

    // 특수문자 집합 확장: 필요 시 프로젝트 정책에 맞춰 조정
    String specials = "!@#$%^&*()\\-_=+{}\\[\\]:;\"'|\\\\<>,.?/~`";
    String specialClass = "[" + specials + "]";

    String lengthClass = allowWhitespace ? "."
        : "\\S"; // 공백 금지 시 비공백만 허용

    String regex =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*" + specialClass + ")" +
            lengthClass + "{" + min + "," + max + "}$";

    this.basePattern = Pattern.compile(regex);
  }

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null || password.isEmpty()) {
      return buildViolation(context, "비밀번호를 입력해주세요.");
    }
    if (!basePattern.matcher(password).matches()) {
      // 실패 사유를 세분화하려면 아래 개별 체크들을 순차 수행
      if (!password.chars().anyMatch(Character::isLowerCase))
        return buildViolation(context, "소문자를 최소 1자 포함하세요.");
      if (!password.chars().anyMatch(Character::isUpperCase))
        return buildViolation(context, "대문자를 최소 1자 포함하세요.");
      if (!password.chars().anyMatch(Character::isDigit))
        return buildViolation(context, "숫자를 최소 1자 포함하세요.");
      if (!containsSpecial(password))
        return buildViolation(context, "특수문자를 최소 1자 포함하세요.");
      if (!allowWhitespace && password.chars().anyMatch(Character::isWhitespace))
        return buildViolation(context, "공백 문자는 사용할 수 없습니다.");
      if (password.length() < min || password.length() > max)
        return buildViolation(context, "비밀번호 길이는 " + min + "–" + max + "자여야 합니다.");
      // 기타 패턴 불일치
      return buildViolation(context, "비밀번호 형식이 올바르지 않습니다.");
    }

    if (hasThreeOrMoreRepeatedChars(password)) {
      return buildViolation(context, "동일 문자를 3회 이상 반복할 수 없습니다.");
    }

    // (선택) 연속 증가/감소 문자/숫자 방지
    if (hasSequentialChars(password, 3)) {
      return buildViolation(context, "연속된 문자/숫자를 3자 이상 사용할 수 없습니다.");
    }

    return true;
  }

  private boolean containsSpecial(String s) {
    // specials와 동일한 집합 사용
    String specials = "!@#$%^&*()\\-_=+{}\\[\\]:;\"'|\\\\<>,.?/~`";
    for (char c : s.toCharArray()) {
      if (specials.indexOf(c) >= 0) return true;
    }
    return false;
  }

  private boolean hasThreeOrMoreRepeatedChars(String s) {
    for (int i = 0; i < s.length() - 2; i++) {
      char c1 = s.charAt(i), c2 = s.charAt(i + 1), c3 = s.charAt(i + 2);
      if (c1 == c2 && c2 == c3) return true;
    }
    return false;
  }

  // abc, 123, cba, 321 같은 연속 패턴
  private boolean hasSequentialChars(String s, int n) {
    if (s.length() < n) return false;
    for (int i = 0; i <= s.length() - n; i++) {
      boolean asc = true, desc = true;
      for (int j = 1; j < n; j++) {
        if (s.charAt(i + j) != s.charAt(i + j - 1) + 1) asc = false;
        if (s.charAt(i + j) != s.charAt(i + j - 1) - 1) desc = false;
        if (!asc && !desc) break;
      }
      if (asc || desc) return true;
    }
    return false;
  }

  private boolean buildViolation(ConstraintValidatorContext ctx, String msg) {
    ctx.disableDefaultConstraintViolation();
    ctx.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
    return false;
  }
}
