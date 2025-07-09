package com.KDT.mosi.security;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberSVC memberSVC;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Member member = memberSVC.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

    return new CustomUserDetails(member);
  }
}
