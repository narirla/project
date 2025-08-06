package com.KDT.mosi.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RequestMapping("/information")
@RequiredArgsConstructor
@Controller
public class RestaurantPageController {


  //맛집 페이지
  @GetMapping("/restaurant")
  public String showMapPage() {
    return "info/info-restaurant";  // templates/info/info-restaurant.html
  }

  // 편의시설 대시보드 페이지
  @GetMapping("/accessibility")
  public String accessibilityDashboard(Model model) {

    return "info/accessibility";
  }


}


