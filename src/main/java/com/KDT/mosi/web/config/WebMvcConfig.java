package com.KDT.mosi.web.config;

import com.KDT.mosi.web.interceptor.PasswordVerifyInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final PasswordVerifyInterceptor passwordVerifyInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(passwordVerifyInterceptor)
        .addPathPatterns(
            "/mypage/buyer/*/edit/**",
            "/mypage/seller/*/edit/**",
            "/members/password"          // ✅ 비밀번호 변경 진입도 보호
        );
    log.info("PasswordVerifyInterceptor 등록 완료");
  }


}
