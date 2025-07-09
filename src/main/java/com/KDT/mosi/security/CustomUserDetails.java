package com.KDT.mosi.security;

import com.KDT.mosi.domain.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Member member;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();  // 권한 없으면 빈 리스트
  }

  @Override
  public String getPassword() {
    return member.getPasswd();  // 실제 비밀번호
  }

  @Override
  public String getUsername() {
    return member.getEmail();   // 로그인 ID (이메일)
  }

  @Override public boolean isAccountNonExpired() { return true; }

  @Override public boolean isAccountNonLocked() { return true; }

  @Override public boolean isCredentialsNonExpired() { return true; }

  @Override public boolean isEnabled() { return true; }
}
