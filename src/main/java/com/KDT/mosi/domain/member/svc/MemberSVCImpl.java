package com.KDT.mosi.domain.member.svc;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.member.dao.RoleDAO;
import com.KDT.mosi.domain.terms.dao.TermsDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberSVCImpl implements MemberSVC {

  private final MemberDAO memberDAO;
  private final RoleDAO roleDAO;
  private final TermsDAO termsDAO;

  @Override
  public Long join(Member member) {
    return memberDAO.save(member);
  }

  @Override
  public Long join(Member member, List<String> roles, List<Long> agreedTermsIds) {
    Long memberId = memberDAO.save(member);

    // 역할 등록
    if (roles != null) {
      for (String roleId : roles) {
        roleDAO.addRoleToMember(memberId, roleId);
      }
    }

    // 약관 동의 등록
    if (agreedTermsIds != null) {
      for (Long termsId : agreedTermsIds) {
        termsDAO.agreeTerms(memberId, termsId);
      }
    }

    return memberId;
  }

  @Override
  public Optional<Member> findByEmail(String email) {
    return memberDAO.findByEmail(email);
  }

  @Override
  public Optional<Member> findById(Long memberId) {
    return memberDAO.findById(memberId);
  }

  @Override
  public boolean isExistEmail(String email) {
    return memberDAO.isExistEmail(email);
  }
}
