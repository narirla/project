package com.KDT.mosi.web.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {

  private final UserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())

        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/", true)
            .permitAll()
        )

        .logout(logout -> logout
            .logoutUrl("/login/logout")
            .logoutSuccessUrl("/")
        )

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/login/**", "/members/**", "/find/**", "/css/**", "/js/**", "/img/**").permitAll()
            .anyRequest().authenticated()
        )

        .exceptionHandling(ex -> ex
            .accessDeniedPage("/error/403")
        )

        // 🔽 여기에 추가
        .userDetailsService(userDetailsService);

    return http.build();
  }
}
