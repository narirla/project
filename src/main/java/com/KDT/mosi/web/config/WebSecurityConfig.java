package com.KDT.mosi.web.config;

import com.KDT.mosi.web.login.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final LoginSuccessHandler loginSuccessHandler;

  // ✅ 1. 특정 URL 패턴을 무시하는 커스텀 RequestCache Bean 정의
  //    - 이 RequestCache는 '/api/**'로 시작하는 요청을 SavedRequest에 저장하지 않습니다.
  @Bean
  public RequestCache requestCache() {
    // 람다 표현식을 사용하여 '/api/**'로 시작하는 요청을 매칭
    RequestMatcher apiRequestMatcher = request -> request.getRequestURI().startsWith("/api/");

    HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    // 요청 매처가 'true'를 반환하면 (즉, /api/ 요청이면) 해당 요청은 저장하지 않도록 설정
    requestCache.setRequestMatcher(request -> !apiRequestMatcher.matches(request));

    return requestCache;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(loginSuccessHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll() // [추가] 로그아웃도 공개 허용
        )
        .authorizeHttpRequests(auth -> auth
            // ✅ 1) Spring Security의 PathRequest를 사용하여 정적 리소스에 대한 접근 허용 (최신 권장 방식)
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

            // 2) 정적 리소스, 공통 공개
            .requestMatchers(
                "/", "/login/**",
                "/members/join", "/members/join/**",
                "/members/emailCheck", "/members/nicknameCheck",
                "/find/**", "/css/**", "/js/**", "/img/**",
                "/api/image-proxy", // 이미지 프록시 URL을 허용하도록 추가
                "/api/food/**", // 혹시 몰라 food API도 추가
                "/members/goodbye",
                "/find/**",
                "/cart/**",
                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
                "/favicon.ico", "/.well-known/**"
            ).permitAll()

            // 3) 메뉴에 있는 공개 페이지들 (로그인 불필요)
            //    - 메뉴 코드 기준: 테마여행/관광정보/항공/호텔/상품목록 등
            .requestMatchers(
                "/product/**",          // [추가] 지역/상품 목록
                "/theme/**",            // [추가] 테마여행 랜딩/목록
                "/tour/**",             // [추가] 관광(코스) 정보
                "/information/**"       // [추가] (메뉴는 /information/* 사용) ※ 기존 /info/** 대신
            ).permitAll()

            // 4) 커뮤니티 공개(예외) → 반드시 bbs 전체 인증 규칙보다 "먼저"
            .requestMatchers("/bbs/community/**").permitAll() // 자유게시판 공개

            // 5) 리뷰만 인증 강제
            .requestMatchers("/bbs/**").authenticated()       // 리뷰 목록/상세/작성/수정/삭제 등

            // 6) 공개 API가 정말로 필요할 때만 허용 (현재 전부 공개는 위험)
            // .requestMatchers("/api/**").permitAll()         // [권고] 필요 시에만 열기

            // 7) 보호 자원
            .requestMatchers(
                "/members/verify-password",
                "/members/password",
                "/mypage/seller/**",
                "/mypage/role/**",
                "/members/*/delete"
            ).authenticated()

            // 8) 그 외는 기본 인증 필요
            .anyRequest().authenticated()
        )
        // ✅ 2. 위에서 정의한 커스텀 RequestCache Bean을 SecurityFilterChain에 적용
        .requestCache(cache -> cache
            .requestCache(requestCache())
        )
        .csrf(csrf -> csrf.disable())  // CSRF 비활성화 (테스트용)
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/error/403")
        )
        .userDetailsService(userDetailsService);

    return http.build();
  }

//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http
//        .authorizeHttpRequests(authz -> authz
//            .requestMatchers("/api/**").permitAll()  // 테스트용 경로 인증 제외
//            .anyRequest().authenticated()
//        )
//        .csrf(csrf -> csrf.disable());  // CSRF 비활성화 (테스트용)
//
//    return http.build();
//  }
}

