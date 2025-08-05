package com.KDT.mosi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
public class AccessibilityController {

  // 편의시설 대시보드 페이지
  @GetMapping("/accessibility")
  public String accessibilityDashboard(Model model) {

    return "info/accessibility";
  }
}
