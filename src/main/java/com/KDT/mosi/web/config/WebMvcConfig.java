package com.KDT.mosi.web.config;

import com.KDT.mosi.web.interceptor.PasswordVerifyInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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
            "/members/password",
            "/mypage/buyer/*/edit", "/mypage/buyer/*/edit/**",
            "/mypage/seller/*/edit", "/mypage/seller/*/edit/**",
            "/members/*/edit", "/members/*/edit/**"
        )
        .excludePathPatterns(
            "/members/verify-password",
            "/members/password/check", "/members/passwordCheck",
            "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
            "/favicon.ico", "/.well-known/**",
            "/api/**"
        );
    log.info("PasswordVerifyInterceptor 등록 완료");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/login").setViewName("login");
    log.info("뷰 컨트롤러 등록 완료");
  }

  // Vite CORS 설정(개발 시에만)
//  @Override
//  public void addCorsMappings(CorsRegistry registry) {
//    registry.addMapping("/api/**")
//        .allowedOrigins("http://localhost:3000") // Vite 개발 서버
//        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//        .allowedHeaders("*")
//        .allowCredentials(true);
//
//    log.info("Vite 개발 서버 허용");
//  }
}
