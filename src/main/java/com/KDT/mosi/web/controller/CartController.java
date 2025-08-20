package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.cart.dto.CartRequest;
import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.domain.cart.svc.CartSVC;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.web.api.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartSVC cartSVC;

  /**
   * 장바구니 HTML 페이지 반환 (브라우저 직접 접근)
   * GET /cart (Accept: text/html)
   */
  @GetMapping(produces = "text/html")
  public String cartPageHtml(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    
    if (loginMember == null) {
      return "redirect:/login";
    }

    model.addAttribute("member", loginMember);
    log.info("장바구니 HTML 페이지 접근: memberId={}, nickname={}", 
        loginMember.getMemberId(), loginMember.getNickname());
    
    return "cart/cart";
  }

  /**
   * 장바구니 JSON 데이터 반환 (React AJAX 호출)
   * GET /cart (Accept: application/json)
   */
  @GetMapping(produces = "application/json")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getCartJson(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      CartResponse cartResponse = cartSVC.getCart(
          loginMember.getMemberId(),
          loginMember.getNickname()
      );

      Map<String, Object> response = new HashMap<>();
      response.put("success", cartResponse.isSuccess());
      response.put("empty", cartResponse.isEmpty());
      response.put("memberNickname", cartResponse.getMemberNickname());
      response.put("memberId", cartResponse.getMemberId());
      response.put("cartItems", cartResponse.getCartItems());
      response.put("totalCount", cartResponse.getTotalCount());
      response.put("totalQuantity", cartResponse.getTotalQuantity());
      response.put("totalPrice", cartResponse.getTotalPrice());

      if (cartResponse.isEmpty()) {
        response.put("message", "장바구니가 비어있습니다");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("장바구니 조회 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "장바구니 조회 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 장바구니 상품 추가
   * POST cart/add
   */
  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> addToCart(
      @Valid @RequestBody CartRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      ApiResponse<Void> result = cartSVC.addToCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      Map<String, Object> response = new HashMap<>();

      boolean isSuccess = result.toString().contains("rtcd=S00");

      response.put("success", isSuccess);
      response.put("code", isSuccess ? "S00" : "ERROR");

      if (isSuccess) {
        response.put("message", "장바구니에 상품이 추가되었습니다");
      } else {
        response.put("message", "상품 추가에 실패했습니다");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("장바구니 추가 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "장바구니 추가 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 장바구니 수량 변경
   * PUT cart/quantity
   */
  @PutMapping("/quantity")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> updateQuantity(
      @Valid @RequestBody CartRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      ApiResponse<Void> result = cartSVC.updateQuantity(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      Map<String, Object> response = new HashMap<>();
      boolean isSuccess = result.toString().contains("rtcd=S00");

      response.put("success", isSuccess);
      response.put("code", isSuccess ? "S00" : "ERROR");

      if (isSuccess) {
        response.put("message", "수량이 변경되었습니다");
      } else {
        response.put("message", "수량 변경에 실패했습니다");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("수량 변경 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "수량 변경 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 장바구니 상품 삭제
   * DELETE /cart/remove
   */
  @DeleteMapping("/remove")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> removeFromCart(
      @Valid @RequestBody CartRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      ApiResponse<Void> result = cartSVC.removeFromCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType()
      );

      Map<String, Object> response = new HashMap<>();
      boolean isSuccess = result.toString().contains("rtcd=S00");

      response.put("success", isSuccess);
      response.put("code", isSuccess ? "S00" : "ERROR");

      if (isSuccess) {
        response.put("message", "상품이 삭제되었습니다");
      } else {
        response.put("message", "상품 삭제에 실패했습니다");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("상품 삭제 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "상품 삭제 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 장바구니 상품 개수 조회
   * GET /cart/count
   */
  @GetMapping("/count")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getCartItemCount(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.ok(Map.of(
          "success", true,
          "count", 0
      ));
    }

    try {
      int count = cartSVC.getCartItemCount(loginMember.getMemberId());
      return ResponseEntity.ok(Map.of(
          "success", true,
          "count", count
      ));
    } catch (Exception e) {
      log.error("장바구니 개수 조회 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.ok(Map.of(
          "success", false,
          "count", 0,
          "message", "장바구니 개수 조회 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 장바구니 전체 비우기
   * DELETE /cart/clear
   */
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
      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "장바구니가 비워졌습니다"
      ));

    } catch (Exception e) {
      log.error("장바구니 비우기 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "장바구니 비우기 중 오류가 발생했습니다"
      ));
    }
  }

  /**
   * 유효성 검증 예외 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "입력값이 올바르지 않습니다");

    Map<String, String> errors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    response.put("errors", errors);

    log.warn("유효성 검증 실패: {}", errors);
    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 일반 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
    log.error("예상치 못한 오류 발생", ex);
    return ResponseEntity.status(500).body(Map.of(
        "success", false,
        "message", "서버에서 오류가 발생했습니다"
    ));
  }
}