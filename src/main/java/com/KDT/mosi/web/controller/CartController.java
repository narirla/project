
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

    Map<String, Object> cartSummary = cartSVC.getCartSummary(loginMember.getMemberId());
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
      // 필수 파라미터 검증
      Object productIdObj = request.get("productId");
      Object optionTypeObj = request.get("optionType");

      if (productIdObj == null || optionTypeObj == null) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "필수 파라미터가 누락되었습니다"
        ));
      }

      Long productId = Long.valueOf(productIdObj.toString());
      String optionType = optionTypeObj.toString();
      Long quantity = Long.valueOf(request.getOrDefault("quantity", 1).toString());

      Map<String, Object> result = cartSVC.addToCart(loginMember.getMemberId(), productId, optionType, quantity);

      return ResponseEntity.ok(result);

    } catch (NumberFormatException e) {
      log.error("잘못된 숫자 형식", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "잘못된 데이터 형식입니다"
      ));

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

    Map<String, Object> result = cartSVC.getCartSummary(loginMember.getMemberId());
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
      // 필수 파라미터 검증
      Object productIdObj = request.get("productId");
      Object optionTypeObj = request.get("optionType");
      Object quantityObj = request.get("quantity");

      if (productIdObj == null || optionTypeObj == null || quantityObj == null) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "필수 파라미터가 누락되었습니다"
        ));
      }

      Long productId = Long.valueOf(productIdObj.toString());
      String optionType = optionTypeObj.toString();
      Long quantity = Long.valueOf(quantityObj.toString());

      Long buyerId = loginMember.getMemberId();

      Map<String, Object> result = cartSVC.updateQuantity(buyerId, productId, optionType, quantity);

      return ResponseEntity.ok(result);

    } catch (NumberFormatException e) {
      log.error("잘못된 숫자 형식", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "잘못된 데이터 형식입니다"
      ));

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
      // 필수 파라미터 검증
      Object productIdObj = request.get("productId");
      Object optionTypeObj = request.get("optionType");

      if (productIdObj == null || optionTypeObj == null) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "필수 파라미터가 누락되었습니다"
        ));
      }

      Long productId = Long.valueOf(productIdObj.toString());
      String optionType = optionTypeObj.toString();

      Map<String, Object> result = cartSVC.removeFromCart(loginMember.getMemberId(), productId, optionType);

      return ResponseEntity.ok(result);

    } catch (NumberFormatException e) {
      log.error("잘못된 숫자 형식", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "잘못된 데이터 형식입니다"
      ));

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

    long count = cartSVC.getCartItemCount(loginMember.getMemberId());
    return ResponseEntity.ok(Map.of("count", count));
  }

  // 장바구니 전체 비우기 (추가)
  @DeleteMapping("/clear")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      cartSVC.clearCart(loginMember.getMemberId());
      return ResponseEntity.ok(Map.of("success", true));

    } catch (Exception e) {
      log.error("장바구니 비우기 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "장바구니 비우기 중 오류가 발생했습니다"
      ));
    }
  }

}