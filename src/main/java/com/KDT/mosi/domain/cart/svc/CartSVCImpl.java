package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.cart.dao.CartDAO;
import com.KDT.mosi.domain.cart.dao.CartItemDAO;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.cart.Cart;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartSVCImpl implements CartSVC {

  private final CartDAO cartDAO;
  private final CartItemDAO cartItemDAO;
  private final ProductSVC productSVC;

  @Override
  public Map<String, Object> addToCart(Long buyerId, Long productId, String optionType, Integer quantity) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 입력값 검증
      if (quantity <= 0) {
        result.put("success", false);
        result.put("message", "수량은 1개 이상이어야 합니다");
        return result;
      }

      // Product 존재 여부 및 상태 확인
      Optional<Product> productOpt = productSVC.getProduct(productId);
      if (productOpt.isEmpty()) {
        result.put("success", false);
        result.put("message", "존재하지 않는 상품입니다");
        return result;
      }

      Product product = productOpt.get();
      if (!"판매중".equals(product.getStatus())) {
        result.put("success", false);
        result.put("message", "현재 판매가 중단된 상품입니다");
        return result;
      }

      // 기존 동일 상품 확인 (메소드명 수정)
      Optional<CartItem> existingItem = cartItemDAO.findByBuyerAndProduct(buyerId, productId, optionType);

      CartItem savedItem;
      if (existingItem.isPresent()) {
        // 기존 상품 수량 증가
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + quantity);
        savedItem = cartItemDAO.insert(item);

        log.info("장바구니 상품 수량 증가: buyerId={}, productId={}, newQuantity={}",
            buyerId, productId, savedItem.getQuantity());
      } else {
        // 새 상품 추가
        Cart cart = getOrCreateCart(buyerId);

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setBuyerId(buyerId);
        newItem.setSellerId(product.getMember().getMemberId());
        newItem.setProductId(productId);
        newItem.setOptionType(optionType);
        newItem.setQuantity(quantity);

        // 실제 Product에서 가격 정보 가져오기
        Integer[] prices = extractPricesFromProduct(product, optionType);
        newItem.setOriginalPrice(prices[0]);
        newItem.setSalePrice(prices[1]);

        savedItem = cartItemDAO.insert(newItem);

        log.info("장바구니 상품 추가: buyerId={}, productId={}", buyerId, productId);
      }

      // 장바구니 총액 업데이트
      updateCartTotalPrice(buyerId);

      result.put("success", true);
      result.put("message", "장바구니에 추가되었습니다");
      result.put("data", savedItem);

    } catch (Exception e) {
      log.error("장바구니 추가 실패", e);
      result.put("success", false);
      result.put("message", "장바구니 추가에 실패했습니다: " + e.getMessage());
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getCartSummary(Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      List<CartItem> items = cartItemDAO.findByBuyerId(buyerId);  // 메소드명 수정
      int totalCount = cartItemDAO.countByBuyerId(buyerId);
      int totalQuantity = cartDAO.getTotalItemCount(buyerId);  // 메소드명 수정

      // Product 정보와 함께 응답 생성
      List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
      long totalPrice = 0;

      for (CartItem item : items) {
        Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("cartItem", item);
        itemData.put("product", productOpt.orElse(null));

        // 상품이 존재하고 판매중인 경우만 총액에 포함
        if (productOpt.isPresent() && "판매중".equals(productOpt.get().getStatus())) {
          totalPrice += (long) item.getSalePrice() * item.getQuantity();
          itemData.put("available", true);
        } else {
          itemData.put("available", false);
        }

        itemsWithProduct.add(itemData);
      }

      Map<String, Object> summary = new HashMap<>();
      summary.put("items", itemsWithProduct);
      summary.put("totalCount", totalCount);
      summary.put("totalQuantity", totalQuantity);
      summary.put("totalPrice", totalPrice);

      result.put("success", true);
      result.put("data", summary);

    } catch (Exception e) {
      log.error("장바구니 조회 실패", e);
      result.put("success", false);
      result.put("message", "장바구니 조회에 실패했습니다: " + e.getMessage());
    }

    return result;
  }

  @Override
  public Map<String, Object> updateQuantity(Long buyerId, Long productId, String optionType, Integer quantity) {
    Map<String, Object> result = new HashMap<>();

    try {
      if (quantity <= 0) {
        // 수량이 0 이하면 삭제
        return removeFromCart(buyerId, productId, optionType);
      }

      Optional<CartItem> itemOpt = cartItemDAO.findByBuyerAndProduct(buyerId, productId, optionType);  // 메소드명 수정
      if (itemOpt.isEmpty()) {
        result.put("success", false);
        result.put("message", "해당 상품을 찾을 수 없습니다");
        return result;
      }

      CartItem item = itemOpt.get();
      item.setQuantity(quantity);
      CartItem updatedItem = cartItemDAO.insert(item);

      // 장바구니 총액 업데이트
      updateCartTotalPrice(buyerId);

      log.info("장바구니 수량 변경: buyerId={}, productId={}, newQuantity={}",
          buyerId, productId, quantity);

      result.put("success", true);
      result.put("message", "수량이 변경되었습니다");
      result.put("data", updatedItem);

    } catch (Exception e) {
      log.error("수량 변경 실패", e);
      result.put("success", false);
      result.put("message", "수량 변경에 실패했습니다: " + e.getMessage());
    }

    return result;
  }

  @Override
  public Map<String, Object> removeFromCart(Long buyerId, Long productId, String optionType) {
    Map<String, Object> result = new HashMap<>();

    try {
      boolean exists = cartItemDAO.existsByBuyerAndProduct(buyerId, productId, optionType);  // 메소드명 수정
      if (!exists) {
        result.put("success", false);
        result.put("message", "해당 상품을 찾을 수 없습니다");
        return result;
      }

      cartItemDAO.deleteByBuyerAndProduct(buyerId, productId, optionType);  // 메소드명 수정

      // 장바구니 총액 업데이트
      updateCartTotalPrice(buyerId);

      log.info("장바구니 상품 삭제: buyerId={}, productId={}", buyerId, productId);

      result.put("success", true);
      result.put("message", "상품이 삭제되었습니다");

    } catch (Exception e) {
      log.error("상품 삭제 실패", e);
      result.put("success", false);
      result.put("message", "상품 삭제에 실패했습니다: " + e.getMessage());
    }

    return result;
  }

  @Override
  public void clearCart(Long buyerId) {
    try {
      cartItemDAO.deleteByBuyerId(buyerId);

      // 장바구니 총액을 0으로 업데이트
      Optional<Cart> cartOpt = cartDAO.findByBuyerId(buyerId);
      if (cartOpt.isPresent()) {
        Cart cart = cartOpt.get();
        cart.setTotalPrice(0);
        cartDAO.insert(cart);
      }

      log.info("장바구니 전체 비우기: buyerId={}", buyerId);

    } catch (Exception e) {
      log.error("장바구니 비우기 실패", e);
      throw new RuntimeException("장바구니 비우기에 실패했습니다", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public int getCartItemCount(Long buyerId) {
    try {
      return cartItemDAO.countByBuyerId(buyerId);
    } catch (Exception e) {
      log.error("장바구니 상품 개수 조회 실패", e);
      return 0;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<CartItem> getSelectedCartItems(Long buyerId, List<Long> cartItemIds) {
    try {
      return cartItemDAO.findByItemIds(cartItemIds, buyerId);  // ✅ 메소드명 수정
    } catch (Exception e) {
      log.error("선택된 장바구니 아이템 조회 실패", e);
      return Collections.emptyList();
    }
  }

  // === Private Helper Methods ===

  private Cart getOrCreateCart(Long buyerId) {
    Optional<Cart> cartOpt = cartDAO.findByBuyerId(buyerId);
    if (cartOpt.isPresent()) {
      return cartOpt.get();
    }

    // 새 장바구니 생성
    Cart newCart = new Cart();
    newCart.setBuyerId(buyerId);
    return cartDAO.insert(newCart);
  }

  /**
   * Product에서 실제 가격 추출
   */
  private Integer[] extractPricesFromProduct(Product product, String optionType) {
    if ("가이드포함".equals(optionType)) {
      return new Integer[]{product.getGuidePrice(), product.getSalesGuidePrice()};
    } else {
      return new Integer[]{product.getNormalPrice(), product.getSalesPrice()};
    }
  }

  /**
   * 장바구니 총액 업데이트
   */
  private void updateCartTotalPrice(Long buyerId) {
    try {
      List<CartItem> items = cartItemDAO.findByBuyerId(buyerId);  // 메소드명 수정

      int totalPrice = 0;
      for (CartItem item : items) {
        // 상품 상태 확인
        Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
        if (productOpt.isPresent() && "판매중".equals(productOpt.get().getStatus())) {
          totalPrice += item.getSalePrice() * item.getQuantity();
        }
      }

      Optional<Cart> cartOpt = cartDAO.findByBuyerId(buyerId);
      if (cartOpt.isPresent()) {
        Cart cart = cartOpt.get();
        cart.setTotalPrice(totalPrice);
        cartDAO.insert(cart);
      }
    } catch (Exception e) {
      log.error("장바구니 총액 업데이트 실패: buyerId={}", buyerId, e);
    }
  }
}