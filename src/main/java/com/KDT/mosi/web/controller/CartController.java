package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.domain.cart.request.CartFormRequest;
import com.KDT.mosi.domain.cart.svc.CartSVC;
import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.mypage.buyer.svc.BuyerPageSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartSVC cartSVC;
  private final BuyerPageSVC buyerPageSVC;

  /**
   * 장바구니 HTML 페이지 반환 (브라우저 직접 접근)
   * GET /cart
   */
  @GetMapping(produces = "text/html")
  public String cartPageHtml(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");

    if (loginMember == null) {
      return "redirect:/login";
    }

    model.addAttribute("member", loginMember);

    // ✅ buyerPage 조회해서 모델에 추가
    BuyerPage buyerPage = buyerPageSVC.findByMemberId(loginMember.getMemberId())
        .orElse(null);
    model.addAttribute("buyerPage", buyerPage);

    return "cart/cart";
  }

  /**
   * 장바구니 JSON 데이터 반환 (React AJAX 호출)
   * GET /cart
   */
  @GetMapping(produces = "application/json")
  @ResponseBody
  public ResponseEntity<ApiResponse<CartResponse>> getCartJson(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      CartResponse cartResponse = cartSVC.getCart(
          loginMember.getMemberId(),
          loginMember.getNickname()
      );

      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, cartResponse)
      );

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 추가
   * POST cart/add
   */
  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addToCart(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.addToCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 수량 변경
   * PUT cart/quantity
   */
  @PutMapping("/quantity")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> updateQuantity(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.updateQuantity(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 삭제
   * DELETE /cart/remove
   */
  @DeleteMapping("/remove")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeFromCart(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.removeFromCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 개수 조회
   * GET /cart/count
   */
  @GetMapping("/count")
  @ResponseBody
  public ResponseEntity<ApiResponse<Integer>> getCartItemCount(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, 0)
      );
    }

    try {
      int count = cartSVC.getCartItemCount(loginMember.getMemberId());
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, count)
      );
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0)
      );
    }
  }

  /**
   * 장바구니 아이템 ID 목록으로 조회
   * GET /cart/items?ids=1,2,3
   */
  @GetMapping("/items")
  @ResponseBody
  public ResponseEntity<ApiResponse<Object>> getCartItemsByIds(
      @RequestParam("ids") String ids,
      HttpSession session) {
    
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      // 전체 장바구니 조회 후 클라이언트에서 필터링하도록 반환
      CartResponse cartResponse = cartSVC.getCart(
          loginMember.getMemberId(),
          loginMember.getNickname()
      );

      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, cartResponse.getCartItems())
      );

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 전체 비우기
   * DELETE /cart/clear
   */
  @DeleteMapping("/clear")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> clearCart(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      cartSVC.clearCart(loginMember.getMemberId());
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, null)
      );

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }
}