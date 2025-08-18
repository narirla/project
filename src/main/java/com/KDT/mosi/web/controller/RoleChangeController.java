package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Role;
import com.KDT.mosi.domain.member.dao.MemberRoleDAO;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDAO;
import com.KDT.mosi.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoleChangeController {

  private final MemberRoleDAO memberRoleDAO;
  private final SellerPageDAO sellerPageDAO;

  /** ✅ 구매자 → 판매자 전환 */
  @PostMapping("/mypage/role/toSeller")
  public String changeToSeller(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:/login";
    }

    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    Member loginMember = userDetails.getMember();
    Long memberId = loginMember.getMemberId();

    // R02 없으면 부여
    if (!memberRoleDAO.hasRole(memberId, "R02")) {
      memberRoleDAO.addRole(memberId, "R02");
    }

    // 최신 권한으로 SecurityContext 갱신
    List<Role> updatedRoles = memberRoleDAO.findRolesByMemberId(memberId);
    CustomUserDetails updatedUserDetails = new CustomUserDetails(loginMember, updatedRoles);
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(updatedUserDetails, null, updatedUserDetails.getAuthorities());
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(newAuth);

    // 세션 갱신
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", loginMember);
    session.setAttribute("loginMemberId", memberId);
    List<String> normRoles = updatedRoles.stream()
        .map(Role::getRoleId) // "R01","R02"
        .map(r -> "R01".equals(r) ? "BUYER" : "R02".equals(r) ? "SELLER" : r)
        .toList();
    session.setAttribute("loginRoles", normRoles);
    session.setAttribute("loginRole", "SELLER");
    log.info("✅ BUYER→SELLER 전환, Roles(norm)={}, loginRole=SELLER", normRoles);

    // 판매자 페이지 없으면 생성 페이지로
    if (!sellerPageDAO.existByMemberId(memberId)) {
      return "redirect:/mypage/seller/create";
    }
    return "redirect:/mypage/seller/home";
  }

  /** ✅ 판매자 → 구매자 전환 */
  @PostMapping("/mypage/role/toBuyer")
  public String changeToBuyer(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:/login";
    }

    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    Member loginMember = userDetails.getMember();
    Long memberId = loginMember.getMemberId();

    // 정책: SELLER(R02) 역할 제거 (보유 유지 원하면 이 블록 삭제)
    if (memberRoleDAO.hasRole(memberId, "R02")) {
      memberRoleDAO.deleteRole(memberId, "R02");
      log.info("🗑 SELLER(R02) 삭제 완료 memberId={}", memberId);
    }

    // 최신 권한으로 SecurityContext 갱신
    List<Role> updatedRoles = memberRoleDAO.findRolesByMemberId(memberId);
    CustomUserDetails updatedUserDetails = new CustomUserDetails(loginMember, updatedRoles);
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(updatedUserDetails, null, updatedUserDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(newAuth);

    // 세션 갱신
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", loginMember);
    session.setAttribute("loginMemberId", memberId);
    List<String> normRoles = updatedRoles.stream()
        .map(Role::getRoleId)
        .map(r -> "R01".equals(r) ? "BUYER" : "R02".equals(r) ? "SELLER" : r)
        .toList();
    session.setAttribute("loginRoles", normRoles);
    session.setAttribute("loginRole", "BUYER");

    log.info("✅ SELLER→BUYER 전환, Roles(norm)={}, loginRole=BUYER", normRoles);

    return "redirect:/mypage/buyer";
  }
}
