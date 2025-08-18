package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.order.svc.OrderSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderSVC orderSVC;

  // 주문서 작성 페이지
  @GetMapping("/form")
  public String orderForm(@RequestParam("cartItemIds") List<Long> cartItemIds,
                          Model model, HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    // 선택된 장바구니 아이템들 정보를 모델에 추가
    model.addAttribute("cartItemIds", cartItemIds);
    model.addAttribute("memberInfo", loginMember);

    return "order/order_form";
  }

  // 주문 생성 (AJAX)
  @PostMapping("/create")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> createOrder(
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
      @SuppressWarnings("unchecked")
      List<Long> cartItemIds = (List<Long>) request.get("cartItemIds");
      String specialRequest = (String) request.get("specialRequest");

      Map<String, Object> result = orderSVC.createOrder(
          loginMember.getMemberId(), cartItemIds, specialRequest);

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("주문 생성 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "주문 처리 중 오류가 발생했습니다"
      ));
    }
  }

  // 주문 완료 페이지
  @GetMapping("/complete")
  public String orderComplete(@RequestParam("orderCode") String orderCode,
                              Model model, HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Map<String, Object> orderDetail = orderSVC.getOrderDetail(orderCode, loginMember.getMemberId());
    model.addAttribute("orderDetail", orderDetail);

    return "order/order_complete";
  }

  // 주문 상세 조회 (AJAX)
  @GetMapping("/detail/{orderCode}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getOrderDetail(
      @PathVariable String orderCode,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    Map<String, Object> result = orderSVC.getOrderDetail(orderCode, loginMember.getMemberId());
    return ResponseEntity.ok(result);
  }

  // 내 주문 목록 조회 (AJAX)
  @GetMapping("/my")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getMyOrders(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    Map<String, Object> result = orderSVC.getMyOrders(loginMember.getMemberId());
    return ResponseEntity.ok(result);
  }

  // 주문 취소 (AJAX)
  @PatchMapping("/cancel/{orderCode}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> cancelOrder(
      @PathVariable String orderCode,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "로그인이 필요합니다"
      ));
    }

    try {
      Map<String, Object> result = orderSVC.cancelOrder(orderCode, loginMember.getMemberId());
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("주문 취소 오류", e);
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "주문 취소 중 오류가 발생했습니다"
      ));
    }
  }
}
