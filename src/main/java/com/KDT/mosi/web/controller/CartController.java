// CartController.java
package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.cart.svc.CartSVC;
import com.KDT.mosi.domain.entity.Member;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartSVC cartSVC;

  // 장바구니 페이지 조회
  @GetMapping
  public String cartPage(Model model, HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    // ✅ buyerId 통일: Member에서 buyerId 추출
    Long buyerId = getBuyerIdFromMember(loginMember);

    Map<String, Object> cartSummary = cartSVC.getCartSummary(buyerId);
    model.addAttribute("cartSummary", cartSummary);
    model.addAttribute("memberNickname", loginMember.getNickname());

    return "cart/cart_page";
  }

  // 장바구니에 상품 추가 (AJAX)
  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> addToCart(
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      Long productId = Long.valueOf(request.get("productId").toString());
      String optionType = request.get("optionType").toString();
      Integer quantity = Integer.valueOf(request.getOrDefault("quantity", 1).toString());

      // ✅ buyerId 통일
      Long buyerId = getBuyerIdFromMember(loginMember);

      Map<String, Object> result = cartSVC.addToCart(buyerId, productId, optionType, quantity);

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("장바구니 추가 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "장바구니 추가 중 오류가 발생했습니다"
      ));
    }
  }

  // 장바구니 조회 (AJAX)
  @GetMapping("/api")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    // ✅ buyerId 통일
    Long buyerId = getBuyerIdFromMember(loginMember);
    Map<String, Object> result = cartSVC.getCartSummary(buyerId);
    return ResponseEntity.ok(result);
  }

  // 수량 변경 (AJAX)
  @PutMapping("/quantity")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> updateQuantity(
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      Long productId = Long.valueOf(request.get("productId").toString());
      String optionType = request.get("optionType").toString();
      Integer quantity = Integer.valueOf(request.get("quantity").toString());

      // ✅ buyerId 통일
      Long buyerId = getBuyerIdFromMember(loginMember);

      Map<String, Object> result = cartSVC.updateQuantity(buyerId, productId, optionType, quantity);

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("수량 변경 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "수량 변경 중 오류가 발생했습니다"
      ));
    }
  }

  // 상품 삭제 (AJAX)
  @DeleteMapping("/remove")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> removeFromCart(
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      Long productId = Long.valueOf(request.get("productId").toString());
      String optionType = request.get("optionType").toString();

      // ✅ buyerId 통일
      Long buyerId = getBuyerIdFromMember(loginMember);

      Map<String, Object> result = cartSVC.removeFromCart(buyerId, productId, optionType);

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("상품 삭제 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "상품 삭제 중 오류가 발생했습니다"
      ));
    }
  }

  // 장바구니 상품 개수 조회 (헤더용)
  @GetMapping("/count")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getCartItemCount(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.ok(Map.of("count", 0));
    }

    // ✅ buyerId 통일
    Long buyerId = getBuyerIdFromMember(loginMember);
    int count = cartSVC.getCartItemCount(buyerId);
    return ResponseEntity.ok(Map.of("count", count));
  }

  /**
   * ✅ 간단한 방식: memberId = buyerId
   */
  private Long getBuyerIdFromMember(Member member) {
    return member.getMemberId();
  }

// ========================================
// 주문서 연동을 위한 회원정보 조회 메소드
// ========================================

  /**
   * 주문서 작성용 회원정보 조회 (buyerId 기반)
   * 장바구니 → 주문서 이동 시 회원정보 자동입력
   */
  @GetMapping("/member-info")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getMemberInfoForOrder(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      // 주문서에 필요한 회원정보 반환
      Map<String, Object> memberInfo = new HashMap<>();
      memberInfo.put("buyerId", getBuyerIdFromMember(loginMember));
      memberInfo.put("name", loginMember.getName());
      memberInfo.put("email", loginMember.getEmail());
      memberInfo.put("tel", loginMember.getTel());
      memberInfo.put("address", loginMember.getAddress());
      memberInfo.put("nickname", loginMember.getNickname());

      return ResponseEntity.ok(Map.of(
          "success", true,
          "data", memberInfo
      ));

    } catch (Exception e) {
      log.error("회원정보 조회 실패", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "회원정보 조회에 실패했습니다"
      ));
    }
  }
}