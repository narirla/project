package com.KDT.mosi.web.config;

import com.KDT.mosi.web.login.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final LoginSuccessHandler loginSuccessHandler;

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
            // 1) 정적 리소스, 공통 공개
            .requestMatchers(
                "/", "/login/**",
                "/members/join", "/members/join/**",
                "/members/emailCheck", "/members/nicknameCheck",
                "/members/goodbye",
                "/find/**",
                "/cart/**",
                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
                "/favicon.ico", "/.well-known/**"
            ).permitAll()

            // 2) 메뉴에 있는 공개 페이지들 (로그인 불필요)
            //    - 메뉴 코드 기준: 테마여행/관광정보/항공/호텔/상품목록 등
            .requestMatchers(
                "/product/**",          // [추가] 지역/상품 목록
                "/theme/**",            // [추가] 테마여행 랜딩/목록
                "/tour/**",             // [추가] 관광(코스) 정보
                "/information/**"       // [추가] (메뉴는 /information/* 사용) ※ 기존 /info/** 대신
            ).permitAll()

            // 3) 커뮤니티 공개(예외) → 반드시 bbs 전체 인증 규칙보다 "먼저"
            .requestMatchers("/bbs/community/**").permitAll() // 자유게시판 공개

            // 4) 리뷰만 인증 강제
            .requestMatchers("/bbs/**").authenticated()       // 리뷰 목록/상세/작성/수정/삭제 등

            // 5) 공개 API가 정말로 필요할 때만 허용 (현재 전부 공개는 위험)
            // .requestMatchers("/api/**").permitAll()         // [권고] 필요 시에만 열기

            // 6) 보호 자원
            .requestMatchers(
                "/members/verify-password",
                "/members/password",
                "/mypage/seller/**",
                "/mypage/role/**",
                "/members/*/delete"
            ).authenticated()

            // 7) 그 외는 기본 인증 필요
            .anyRequest().authenticated()
        )
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

