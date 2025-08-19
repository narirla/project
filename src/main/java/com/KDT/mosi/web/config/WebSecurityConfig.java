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
        // (선택) AJAX를 POST로 보낼 때 CSRF를 무시하려면 아래 주석 해제
        // .csrf(csrf -> csrf.ignoringRequestMatchers("/members/password/check"))  // [변경] 단일 경로만

        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(loginSuccessHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/login/**",
                "/members/join", "/members/join/**",
                "/members/emailCheck", "/members/nicknameCheck",
                "/members/goodbye",
                "/find/**",
                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
                "/favicon.ico", "/.well-known/**",
                "/info/**",
                "/api/**",
                // 현재 비밀번호 확인 API (AJAX)
                "/members/password/check"                        // [변경] 레거시(/members/passwordCheck) 제거
            ).permitAll()
            // 보호 자원
            .requestMatchers("/members/verify-password").authenticated()
            .requestMatchers("/members/password").authenticated()
            .requestMatchers("/mypage/seller/**").authenticated()
            .requestMatchers("/mypage/role/**").authenticated()
            .requestMatchers("/members/*/delete").authenticated()
            .anyRequest().authenticated()
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
