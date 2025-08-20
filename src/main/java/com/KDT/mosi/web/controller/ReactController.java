package com.KDT.mosi.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReactController {

  @GetMapping(value = {
      "/",
      "/cart/**",
      "/order/**",
      "/{path:^(?!api|static|css|js|img).*}/**"
  })
  public String forward() {
    return "forward:/index.html";
  }
}
