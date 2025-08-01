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

  /**
   * ✅ 구매자 → 판매자 역할 전환
   * - R02(판매자) 역할이 없다면 추가 부여
   * - SecurityContext의 Authentication 갱신
   * - 세션에 loginMember 다시 저장
   * - 판매자 페이지 생성 여부에 따라 분기 이동
   */
  @PostMapping("/mypage/role/toSeller")
  public String changeToSeller(HttpServletRequest request) {
    // 현재 인증 정보 가져오기
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:/login"; // 비로그인 시 로그인 페이지로
    }

    // 현재 로그인한 사용자 정보 추출
    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    Member loginMember = userDetails.getMember();
    Long memberId = loginMember.getMemberId();

    // R02(판매자) 역할이 없으면 추가 부여
    if (!memberRoleDAO.hasRole(memberId, "R02")) {
      memberRoleDAO.addRole(memberId, "R02");
    }

    // 최신 역할 목록 조회 및 SecurityContext 갱신
    List<Role> updatedRoles = memberRoleDAO.findRolesByMemberId(memberId);
    CustomUserDetails updatedUserDetails = new CustomUserDetails(loginMember, updatedRoles);
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(updatedUserDetails, null, updatedUserDetails.getAuthorities());

    // SecurityContextHolder에 새로운 인증 정보 설정
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(newAuth);

    // ✅ 세션에 loginMember 다시 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", loginMember);
    session.setAttribute("loginMemberId", memberId);  // ✅ ID도 세션에 저장

    session.setAttribute("loginRole", "SELLER");
    log.info("✅ 세션에 저장된 loginRole: {}", session.getAttribute("loginRole"));


    log.info("🔁 역할 전환 후 세션에 loginMember 저장됨: {}", loginMember.getEmail());

    // 판매자 페이지 없으면 생성 페이지로 이동
    if (!sellerPageDAO.existByMemberId(memberId)) {
      return "redirect:/mypage/seller/create";
    }

    // 판매자 마이페이지로 이동
    return "redirect:/mypage/seller/home";
  }

  /**
   * ✅ 판매자 → 구매자 역할 전환
   * - 역할 삭제 없음 (R01, R02 모두 보유 상태 유지)
   * - SecurityContext의 Authentication 갱신
   * - 세션에 loginMember 다시 저장
   * - 구매자 마이페이지로 이동
   */
  @PostMapping("/mypage/role/toBuyer")
  public String changeToBuyer(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:/login";
    }

    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    Member loginMember = userDetails.getMember();
    Long memberId = loginMember.getMemberId();

    // ✅ SELLER(R02) 역할만 삭제
    if (memberRoleDAO.hasRole(memberId, "R02")) {
      memberRoleDAO.deleteRole(memberId, "R02");
      log.info("🗑 판매자(R02) 역할 삭제 완료 (memberId={})", memberId);
    }

    // ✅ 최신 역할 다시 조회
    List<Role> updatedRoles = memberRoleDAO.findRolesByMemberId(memberId);
    CustomUserDetails updatedUserDetails = new CustomUserDetails(loginMember, updatedRoles);
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(updatedUserDetails, null, updatedUserDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(newAuth);

    // ✅ 세션 갱신
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", loginMember);
    session.setAttribute("loginMemberId", memberId);
    session.setAttribute("loginRole", "BUYER");

    log.info("✅ SELLER → BUYER 전환 완료, 세션 업데이트");

    return "redirect:/mypage/buyer";
  }

}
