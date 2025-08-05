package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Role;
import com.KDT.mosi.domain.member.dao.MemberRoleDAO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Slf4j
@RequestMapping("/information")
@RequiredArgsConstructor
@Controller
public class RestaurantPageController {

  private final MemberRoleDAO memberRoleDAO;

  @GetMapping("/restaurant")
  public String showMapPage(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember != null){
      List<Role> roles = memberRoleDAO.findRolesByMemberId(loginMember.getMemberId());
      boolean isSeller = roles.stream().anyMatch(role -> "R02".equals(role.getRoleId()));
      model.addAttribute("loginRole", isSeller ? "SELLER" : "BUYER");
    }
    return "info/info-restaurant";  // templates/info/info-restaurant.html
  }
}
