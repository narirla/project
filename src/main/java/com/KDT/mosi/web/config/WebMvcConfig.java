package com.KDT.mosi.web.config;

import com.KDT.mosi.web.interceptor.PasswordVerifyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
  }


}
