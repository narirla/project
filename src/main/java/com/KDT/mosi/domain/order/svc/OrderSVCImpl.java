package com.KDT.mosi.domain.order.svc;

import com.KDT.mosi.domain.cart.repository.CartItemRepository;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.entity.order.Order;
import com.KDT.mosi.domain.entity.order.OrderItem;
import com.KDT.mosi.domain.order.dao.OrderItemRepository;
import com.KDT.mosi.domain.order.dao.OrderRepository;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderSVCImpl implements OrderSVC {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductSVC productSVC;

  @Override
  public Map<String, Object> createOrder(Long buyerId, List<Long> cartItemIds, String specialRequest) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 장바구니 아이템 조회 및 검증
      List<CartItem> cartItems = cartItemRepository.findByCartItemIdIn(cartItemIds);
      List<CartItem> validItems = new ArrayList<>();

      for (CartItem item : cartItems) {
        if (item.getBuyerId().equals(buyerId)) {
          // 상품 상태 확인 (판매중만)
          Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
          if (productOpt.isPresent() && "판매중".equals(productOpt.get().getStatus())) {
            validItems.add(item);
          }
        }
      }

      if (validItems.isEmpty()) {
        result.put("success", false);
        result.put("message", "주문 가능한 상품이 없습니다");
        return result;
      }

      // 주문 생성
      Order order = new Order();
      order.setOrderCode(generateOrderCode());
      order.setBuyerId(buyerId);
      order.setSpecialRequest(specialRequest);
      order.setOrderDate(LocalDateTime.now());
      order.setStatus("결제대기");

      // 총액 계산
      long totalPrice = 0;
      for (CartItem cartItem : validItems) {
        totalPrice += cartItem.getSalePrice() * cartItem.getQuantity();
      }
      order.setTotalPrice(totalPrice);

      // 주문 저장
      Order savedOrder = orderRepository.save(order);

      // 주문 상세 저장
      for (CartItem cartItem : validItems) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(savedOrder.getOrderId());  // FK 방식
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setSellerId(cartItem.getSellerId());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setOriginalPrice(cartItem.getOriginalPrice());
        orderItem.setSalePrice(cartItem.getSalePrice());
        orderItem.setOptionType(cartItem.getOptionType());
        orderItem.setReviewed("N");
        orderItemRepository.save(orderItem);
      }

      // 장바구니에서 주문 완료된 아이템 삭제
      cartItemRepository.deleteAllById(cartItemIds);

      result.put("success", true);
      result.put("data", savedOrder);
      result.put("orderCode", savedOrder.getOrderCode());

    } catch (Exception e) {
      log.error("주문 생성 실패", e);
      result.put("success", false);
      result.put("message", "주문 처리 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getOrderByCode(String orderCode, Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Optional<Order> orderOpt = orderRepository.findByOrderCodeAndBuyerId(orderCode, buyerId);
      if (orderOpt.isEmpty()) {
        result.put("success", false);
        result.put("message", "주문을 찾을 수 없습니다");
        return result;
      }

      Order order = orderOpt.get();
      List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

      // 상품 정보와 함께 응답
      List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
      for (OrderItem item : orderItems) {
        Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("orderItem", item);
        itemData.put("product", productOpt.orElse(null));
        itemsWithProduct.add(itemData);
      }

      Map<String, Object> orderData = new HashMap<>();
      orderData.put("order", order);
      orderData.put("items", itemsWithProduct);

      result.put("success", true);
      result.put("data", orderData);

    } catch (Exception e) {
      log.error("주문 코드 조회 실패", e);
      result.put("success", false);
      result.put("message", "주문 조회 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getBuyerOrders(Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      List<Order> orders = orderRepository.findByBuyerId(buyerId);

      if (orders.isEmpty()) {
        result.put("success", true);
        result.put("data", new ArrayList<>());
        return result;
      }

      List<Map<String, Object>> orderList = new ArrayList<>();
      for (Order order : orders) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order", order);
        orderData.put("items", orderItems);
        orderData.put("itemCount", orderItems.size());

        orderList.add(orderData);
      }

      result.put("success", true);
      result.put("data", orderList);

    } catch (Exception e) {
      log.error("주문 목록 조회 실패", e);
      result.put("success", false);
      result.put("message", "주문 목록 조회 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getOrderDetail(Long orderId, Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Optional<Order> orderOpt = orderRepository.findById(orderId);
      if (orderOpt.isEmpty() || !orderOpt.get().getBuyerId().equals(buyerId)) {
        result.put("success", false);
        result.put("message", "주문을 찾을 수 없습니다");
        return result;
      }

      Order order = orderOpt.get();
      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

      // 상품 정보와 함께 응답
      List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
      for (OrderItem item : orderItems) {
        Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("orderItem", item);
        itemData.put("product", productOpt.orElse(null));
        itemsWithProduct.add(itemData);
      }

      Map<String, Object> orderData = new HashMap<>();
      orderData.put("order", order);
      orderData.put("items", itemsWithProduct);

      result.put("success", true);
      result.put("data", orderData);

    } catch (Exception e) {
      log.error("주문 상세 조회 실패", e);
      result.put("success", false);
      result.put("message", "주문 조회 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  public Map<String, Object> cancelOrder(Long orderId, Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Optional<Order> orderOpt = orderRepository.findById(orderId);
      if (orderOpt.isEmpty()) {
        result.put("success", false);
        result.put("message", "주문을 찾을 수 없습니다");
        return result;
      }

      Order order = orderOpt.get();

      // 권한 확인
      if (!order.getBuyerId().equals(buyerId)) {
        result.put("success", false);
        result.put("message", "권한이 없습니다");
        return result;
      }

      // 취소 가능 상태 확인
      if (!"결제대기".equals(order.getStatus()) && !"결제완료".equals(order.getStatus())) {
        result.put("success", false);
        result.put("message", "취소할 수 없는 주문 상태입니다");
        return result;
      }

      order.setStatus("취소");
      orderRepository.save(order);

      result.put("success", true);
      result.put("data", order);

    } catch (Exception e) {
      log.error("주문 취소 실패", e);
      result.put("success", false);
      result.put("message", "주문 취소 중 오류가 발생했습니다");
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public int getOrderCount(Long buyerId) {
    return orderRepository.countByBuyerId(buyerId);
  }

  // 주문 코드 생성 (MOSI-20250818-001 형태)
  private String generateOrderCode() {
    String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    return "MOSI-" + dateStr + "-" + timeStr;
  }

  // 주문 상태 검증
  private boolean isValidStatus(String status) {
    return Arrays.asList("결제대기", "결제완료", "취소").contains(status);
  }
}