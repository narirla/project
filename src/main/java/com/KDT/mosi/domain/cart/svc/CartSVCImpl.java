package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.cart.repository.CartRepository;
import com.KDT.mosi.domain.cart.repository.CartItemRepository;
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

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductSVC productSVC;

  @Override
  public Map<String, Object> addToCart(Long buyerId, Long productId, String optionType, Long quantity) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 입력값 검증
      if (quantity <= 0) {
        result.put("success", false);
        return result;
      }

      // Product 존재 여부 및 상태 확인
      Optional<Product> productOpt = productSVC.getProduct(productId);
      if (productOpt.isEmpty() || !"판매중".equals(productOpt.get().getStatus())) {
        result.put("success", false);
        return result;
      }

      Product product = productOpt.get();

      // 기존 동일 상품 확인
      Optional<CartItem> existingItem = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      CartItem savedItem;
      if (existingItem.isPresent()) {
        // 기존 상품 수량 증가
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + quantity);
        savedItem = cartItemRepository.save(item);
      } else {
        // 장바구니 조회 또는 생성
        Cart cart = getOrCreateCart(buyerId);

        // 새 상품 추가
        CartItem newItem = new CartItem();
        newItem.setCartId(cart.getCartId());
        newItem.setBuyerId(buyerId);
        newItem.setSellerId(product.getMember().getMemberId());
        newItem.setProductId(productId);
        newItem.setOptionType(optionType);
        newItem.setQuantity(quantity);

        // 가격 설정
        setPrice(newItem, product, optionType);

        savedItem = cartItemRepository.save(newItem);
      }

      // 총액 업데이트 (통합 메소드 사용)
      updateCartTotal(buyerId);

      result.put("success", true);
      result.put("data", savedItem);

    } catch (Exception e) {
      log.error("장바구니 추가 실패", e);
      result.put("success", false);
    }

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getCartSummary(Long buyerId) {
    Map<String, Object> result = new HashMap<>();

    try {
      List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);

      // 빈 장바구니 처리
      if (items.isEmpty()) {
        Map<String, Object> emptySummary = new HashMap<>();
        emptySummary.put("items", new ArrayList<>());
        emptySummary.put("totalCount", 0);
        emptySummary.put("totalQuantity", 0);
        emptySummary.put("totalPrice", 0L);

        result.put("success", true);
        result.put("data", emptySummary);
        return result;
      }

      // 총 수량 계산
      int totalQuantity = 0;
      for (CartItem item : items) {
        totalQuantity += item.getQuantity();
      }

      // Product 정보와 함께 응답 생성
      List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
      long totalPrice = 0;

      for (CartItem item : items) {
        Optional<Product> productOpt = productSVC.getProduct(item.getProductId());
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("cartItem", item);
        itemData.put("product", productOpt.orElse(null));

        // 판매중인 상품만 총액에 포함
        if (productOpt.isPresent() && "판매중".equals(productOpt.get().getStatus())) {
          totalPrice += item.getSalePrice() * item.getQuantity();
          itemData.put("available", true);
        } else {
          itemData.put("available", false);
        }

        itemsWithProduct.add(itemData);
      }

      Map<String, Object> summary = new HashMap<>();
      summary.put("items", itemsWithProduct);
      summary.put("totalCount", items.size());
      summary.put("totalQuantity", totalQuantity);
      summary.put("totalPrice", totalPrice);

      result.put("success", true);
      result.put("data", summary);

    } catch (Exception e) {
      log.error("장바구니 조회 실패", e);
      result.put("success", false);
    }

    return result;
  }

  @Override
  public Map<String, Object> updateQuantity(Long buyerId, Long productId, String optionType, Long quantity) {
    Map<String, Object> result = new HashMap<>();

    try {
      if (quantity <= 0) {
        // 수량이 0 이하면 삭제
        cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);
        updateCartTotal(buyerId);
        result.put("success", true);
        return result;
      }

      Optional<CartItem> itemOpt = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      if (itemOpt.isEmpty()) {
        result.put("success", false);
        return result;
      }

      CartItem item = itemOpt.get();
      item.setQuantity(quantity);
      cartItemRepository.save(item);

      // 총액 업데이트 (통합 메소드 사용)
      updateCartTotal(buyerId);

      result.put("success", true);

    } catch (Exception e) {
      log.error("수량 변경 실패", e);
      result.put("success", false);
    }

    return result;
  }

  @Override
  public Map<String, Object> removeFromCart(Long buyerId, Long productId, String optionType) {
    Map<String, Object> result = new HashMap<>();

    try {
      cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      // 총액 업데이트 (통합 메소드 사용)
      updateCartTotal(buyerId);

      result.put("success", true);

    } catch (Exception e) {
      log.error("상품 삭제 실패", e);
      result.put("success", false);
    }

    return result;
  }

  @Override
  public void clearCart(Long buyerId) {
    cartItemRepository.deleteByBuyerId(buyerId);
    updateCartTotal(buyerId);
    log.info("장바구니 전체 비우기: buyerId={}", buyerId);
  }

  @Override
  @Transactional(readOnly = true)
  public int getCartItemCount(Long buyerId) {
    return cartItemRepository.countByBuyerId(buyerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CartItem> getSelectedCartItems(Long buyerId, List<Long> cartItemIds) {
    List<CartItem> allSelectedItems = cartItemRepository.findByCartItemIdIn(cartItemIds);

    // 보안: 구매자 ID로 필터링
    List<CartItem> validItems = new ArrayList<>();
    for (CartItem item : allSelectedItems) {
      if (item.getBuyerId().equals(buyerId)) {
        validItems.add(item);
      }
    }
    return validItems;
  }

  private Cart getOrCreateCart(Long buyerId) {
    if (!cartRepository.existsByBuyerId(buyerId)) {
      Cart newCart = new Cart();
      newCart.setBuyerId(buyerId);
      newCart.setTotalPrice(0L);
      return cartRepository.save(newCart);
    }
    return cartRepository.findByBuyerId(buyerId).get();
  }

  private void setPrice(CartItem newItem, Product product, String optionType) {
    if ("가이드포함".equals(optionType)) {
      newItem.setOriginalPrice(product.getGuidePrice() != null ?
          product.getGuidePrice().longValue() : 0L);
      newItem.setSalePrice(product.getSalesGuidePrice() != null ?
          product.getSalesGuidePrice().longValue() : 0L);
    } else {
      newItem.setOriginalPrice(product.getNormalPrice() != null ?
          product.getNormalPrice().longValue() : 0L);
      newItem.setSalePrice(product.getSalesPrice() != null ?
          product.getSalesPrice().longValue() : 0L);
    }
  }

  private void updateCartTotal(Long buyerId) {
    List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);
    long totalPrice = 0;

    for (CartItem item : items) {
      Optional<Product> prod = productSVC.getProduct(item.getProductId());
      if (prod.isPresent() && "판매중".equals(prod.get().getStatus())) {
        totalPrice += item.getSalePrice() * item.getQuantity();
      }
    }

    Optional<Cart> cartOpt = cartRepository.findByBuyerId(buyerId);
    if (cartOpt.isPresent()) {
      cartOpt.get().setTotalPrice(totalPrice);
      cartRepository.save(cartOpt.get());
    }
  }
}