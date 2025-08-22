package com.KDT.mosi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // @RestController가 아닌 @Controller를 사용합니다.
public class TourInfoController {

  /**
   * 투어 정보 페이지를 반환하는 엔드포인트입니다.
   * 브라우저에서 'http://localhost:9070/tour-info'로 접속하면 이 메서드가 호출됩니다.
   * @return 뷰 이름 ("TourInfo")
   */
  @GetMapping("/tour/info")
  public String tourInfoPage() {
    return "place/TourInfo"; // templates/TourInfo.html 파일을 반환합니다.
  }
}