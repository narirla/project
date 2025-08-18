package com.KDT.mosi.web.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModel {

  @ModelAttribute("activePath")
  public String activePath(HttpServletRequest req) {
    return req != null && req.getRequestURI() != null ? req.getRequestURI() : "";
  }

  @ModelAttribute("currentPath")
  public String currentPath(HttpServletRequest req) {
    return req != null && req.getRequestURI() != null ? req.getRequestURI() : "";
  }
}
