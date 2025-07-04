package com.KDT.mosi.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 처리 컨트롤러
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 403 접근 거부 예외 처리
   */
  @ExceptionHandler(AccessDeniedException.class)
  public String handleAccessDeniedException(AccessDeniedException e,
                                            HttpServletRequest request,
                                            Model model) {
    log.warn("403 예외 발생: {}", e.getMessage());

    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("errorMessage", e.getMessage());
    return "error/403";
  }

  // 필요 시 다른 예외도 여기에 추가 가능
}
