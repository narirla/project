package com.KDT.mosi.domain.order.svc;

import com.KDT.mosi.domain.cart.svc.CartSVC;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.entity.order.Order;
import com.KDT.mosi.domain.entity.order.OrderItem;
import com.KDT.mosi.domain.order.dao.OrderDAO;
import com.KDT.mosi.domain.order.dao.OrderItemDAO;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderSVCImpl implements OrderSVC {

  private final OrderDAO orderDAO;
  private final OrderItemDAO orderItemDAO;
  private final CartSVC cartSVC;
  private final ProductSVC productSVC;

  @Override
  @Transactional
  public Map<String, Object> createOrder(Long buyerId, List<Long> cartItemIds, String specialRequest) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 1. 장바구니 아이템 조회
      List<CartItem> cartItems = cartSVC.getSelectedCartItems(buyerId, cartItemIds);
      if (cartItems.isEmpty()) {
        result.put("success", false);
        result.put("message", "주문할 상품이 없습니다");
        return result;
      }

      // 2. 상품 상태 및 가격 검증
      for (CartItem cartItem : cartItems) {
        Product product = productSVC.getProduct(cartItem.getProductId()).orElse(null);
        if (product == null) {
          result.put("success", false);
          result.put("message", "삭제된 상품이 포함되어 있습니다");
          return result;
        }
        if (!"판매중".equals(product.getStatus())) {
          result.put("success", false);
          result.put("message", "판매 중단된 상품이 포함되어 있습니다: " + product.getTitle());
          return result;
        }

        // 가격 변동 체크
        Integer currentPrice = getCurrentPrice(product, cartItem.getOptionType());
        if (!cartItem.getSalePrice().equals(currentPrice)) {
          result.put("success", false);
          result.put("message", "가격이 변경된 상품이 있습니다: " + product.getTitle());
          return result;
        }
      }

      // 3. 주문 생성 (서비스에서 객체 생성 및 초기화)
      Order order = new Order();
      order.setBuyerId(buyerId);
      order.setOrderCode(generateOrderCode());
      order.setSpecialRequest(specialRequest);
      order.setStatus("결제완료"); // 임시결제는 바로 완료
      order.setOrderDate(LocalDateTime.now()); // 서비스에서 시간 설정

      // 총액 계산 (서비스 로직)
      int totalPrice = cartItems.stream()
          .mapToInt(this::calculateCartItemTotalPrice)
          .sum();
      order.setTotalPrice(totalPrice);

      Order savedOrder = orderDAO.insert(order);

      // 4. 주문 아이템 생성 (서비스에서 객체 생성)
      for (CartItem cartItem : cartItems) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(savedOrder);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setSellerId(cartItem.getSellerId());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setOriginalPrice(cartItem.getOriginalPrice());
        orderItem.setSalePrice(cartItem.getSalePrice());
        orderItem.setOptionType(cartItem.getOptionType());
        orderItem.setReviewed("N"); // 기본값 설정

        orderItemDAO.insert(orderItem);
      }

      // 5. 장바구니 비우기
      cartSVC.clearCart(buyerId);

      result.put("success", true);
      result.put("message", "주문이 완료되었습니다");
      result.put("orderCode", savedOrder.getOrderCode());
      result.put("totalPrice", totalPrice);

    } catch (Exception e) {
      log.error("주문 생성 오류: ", e);
      result.put("success", false);
      result.put("message", "주문 처리 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  public Map<String, Object> getOrderDetail(String orderCode, Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Order order = orderDAO.findByOrderCodeAndBuyerId(orderCode, buyerId)
          .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

      List<OrderItem> orderItems = orderItemDAO.findByOrderId(order.getOrderId());

      result.put("success", true);
      result.put("order", order);
      result.put("orderItems", orderItems);

    } catch (Exception e) {
      log.error("주문 조회 오류: ", e);
      result.put("success", false);
      result.put("message", "주문 조회 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  public Map<String, Object> getMyOrders(Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      List<Order> orders = orderDAO.findByBuyerIdOrderByOrderDateDesc(buyerId);

      result.put("success", true);
      result.put("orders", orders);

    } catch (Exception e) {
      log.error("주문 목록 조회 오류: ", e);
      result.put("success", false);
      result.put("message", "주문 목록 조회 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  @Transactional
  public Map<String, Object> cancelOrder(String orderCode, Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Order order = orderDAO.findByOrderCodeAndBuyerId(orderCode, buyerId)
          .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

      if ("취소".equals(order.getStatus())) {
        result.put("success", false);
        result.put("message", "이미 취소된 주문입니다");
        return result;
      }

      order.setStatus("취소");
      orderDAO.update(order);

      result.put("success", true);
      result.put("message", "주문이 취소되었습니다");

    } catch (Exception e) {
      log.error("주문 취소 오류: ", e);
      result.put("success", false);
      result.put("message", "주문 취소 중 오류가 발생했습니다");
    }

    return result;
  }

  // Private 메서드들
  private String generateOrderCode() {
    LocalDateTime now = LocalDateTime.now();
    String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String timeStr = String.valueOf(System.currentTimeMillis() % 1000000);
    return "MOSI-" + dateStr + "-" + timeStr;
  }

  private Integer getCurrentPrice(Product product, String optionType) {
    if ("기본코스".equals(optionType)) {
      return product.getSalesPrice();
    } else if ("가이드포함".equals(optionType)) {
      return product.getSalesGuidePrice();
    }
    throw new IllegalArgumentException("잘못된 옵션 타입입니다");
  }

  // 서비스에서 장바구니 아이템 총 가격 계산
  private Integer calculateCartItemTotalPrice(CartItem cartItem) {
    if (cartItem.getSalePrice() == null || cartItem.getQuantity() == null) {
      return 0;
    }
    return cartItem.getSalePrice() * cartItem.getQuantity();
  }
}