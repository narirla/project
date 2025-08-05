package com.KDT.mosi.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RequestMapping("/information")
@RequiredArgsConstructor
@Controller
public class RestaurantPageController {

  @GetMapping("/restaurant")
  public String showMapPage() {
    return "info/info-restaurant";  // templates/info/info-restaurant.html
  }
}
