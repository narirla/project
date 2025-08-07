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
        .csrf(csrf -> csrf.disable())
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(loginSuccessHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/login/logout")
            .logoutSuccessUrl("/")
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/login/**",
                "/members/join", "/members/join/**",
                "/members/emailCheck", "/members/nicknameCheck",
                "/find/**", "/css/**", "/js/**", "/img/**",
                "/api/image-proxy", // ✅ 이미지 프록시 URL을 허용하도록 추가
                "/api/food/**", // 혹시 몰라 food API도 추가
                "/members/goodbye",
                "/find/**", "/css/**", "/js/**", "/img/**"
            ).permitAll()
            .requestMatchers("/mypage/seller/**").authenticated()
            .requestMatchers("/mypage/role/**").authenticated()
            .requestMatchers("/members/*/delete").authenticated()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/error/403")
        )
        .userDetailsService(userDetailsService);

    return http.build();
  }
}