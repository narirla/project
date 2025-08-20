package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.domain.cart.dto.CartItemResponse;
import com.KDT.mosi.domain.cart.repository.CartItemRepository;
import com.KDT.mosi.domain.cart.repository.CartRepository;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.entity.cart.Cart;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 장바구니 Service 구현체
 * React+Vite 환경과 완전 호환
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartSVCImpl implements CartSVC {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductSVC productSVC;
  private final SellerPageSVC sellerPageSVC;

  @Override
  public ApiResponse<Void> addToCart(Long buyerId, Long productId, String optionType, Long quantity) {
    try {
      if (quantity <= 0) {
        return ApiResponse.of(ApiResponseCode.INVALID_PARAMETER, null);
      }

      Optional<Product> productOpt = productSVC.getProduct(productId);
      if (productOpt.isEmpty() || !"판매중".equals(productOpt.get().getStatus())) {
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
      }

      Product product = productOpt.get();

      Optional<CartItem> existingItem = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      if (existingItem.isPresent()) {
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
      } else {
        Cart cart = getOrCreateCart(buyerId);

        CartItem newItem = new CartItem();
        newItem.setCartId(cart.getCartId());
        newItem.setBuyerId(buyerId);
        newItem.setSellerId(product.getMember().getMemberId());
        newItem.setProductId(productId);
        newItem.setOptionType(optionType);
        newItem.setQuantity(quantity);

        setPrice(newItem, product, optionType);
        cartItemRepository.save(newItem);
      }

      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("장바구니 추가 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public CartResponse getCart(Long buyerId, String memberNickname) {
    try {
      List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);

      if (items.isEmpty()) {
        return CartResponse.createEmptyCart(memberNickname, buyerId);
      }

      // Entity → DTO 수동 변환
      List<CartItemResponse> cartItems = convertToCartItemResponses(items);

      long totalPrice = 0;
      int totalQuantity = 0;

      // React에서 계산 로직을 단순화하기 위해 서버에서 미리 계산
      for (CartItemResponse dto : cartItems) {
        if (dto.isAvailable()) {
          totalPrice += dto.getPrice() * dto.getQuantity();
          totalQuantity += dto.getQuantity().intValue();
        }
      }

      // 🔧 수정: 모든 파라미터를 Long 타입으로 전달, CartItemResponse 리스트 사용
      return CartResponse.createSuccess(
          memberNickname,
          buyerId,
          cartItems,                    // List<CartItemResponse>
          (long) cartItems.size(),      // Long
          (long) totalQuantity,         // Long
          totalPrice                    // Long
      );

    } catch (Exception e) {
      log.error("장바구니 조회 중 오류 발생: buyerId={}", buyerId, e);
      return CartResponse.createError(memberNickname, buyerId, "장바구니 조회 중 오류가 발생했습니다");
    }
  }

  @Override
  public ApiResponse<Void> updateQuantity(Long buyerId, Long productId, String optionType, Long quantity) {
    try {
      if (quantity <= 0) {
        cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);
        updateCartTotal(buyerId);
        return ApiResponse.of(ApiResponseCode.SUCCESS, null);
      }

      Optional<CartItem> itemOpt = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      if (itemOpt.isEmpty()) {
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
      }

      CartItem item = itemOpt.get();
      item.setQuantity(quantity);
      cartItemRepository.save(item);

      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("수량 변경 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  @Override
  public ApiResponse<Void> removeFromCart(Long buyerId, Long productId, String optionType) {
    try {
      cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);
      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("상품 삭제 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  @Override
  public void clearCart(Long buyerId) {
    try {
      cartItemRepository.deleteByBuyerId(buyerId);
      updateCartTotal(buyerId);
      log.info("장바구니 전체 비우기 완료: buyerId={}", buyerId);
    } catch (Exception e) {
      log.error("장바구니 비우기 중 오류 발생: buyerId={}", buyerId, e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public int getCartItemCount(Long buyerId) {
    try {
      return cartItemRepository.countByBuyerId(buyerId);
    } catch (Exception e) {
      log.error("장바구니 개수 조회 중 오류 발생: buyerId={}", buyerId, e);
      return 0;
    }
  }

  /**
   * CartItem → CartItemResponse 변환
   * React+Vite와 완전 호환되는 DTO 변환
   */
  private List<CartItemResponse> convertToCartItemResponses(List<CartItem> items) {
    List<CartItemResponse> result = new ArrayList<>();

    for (CartItem item : items) {
      Optional<Product> productOpt = productSVC.getProduct(item.getProductId());

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        String sellerNickname = getSellerNickname(item.getSellerId());
        boolean isAvailable = "판매중".equals(product.getStatus());

        // 🔧 수정: React가 기대하는 정확한 필드명으로 매핑
        CartItemResponse dto = isAvailable ?
            CartItemResponse.createAvailable(
                item.getProductId(),
                product.getTitle(),          // productName으로 매핑됨
                product.getDescription(),
                item.getSalePrice(),
                item.getOriginalPrice(),
                item.getQuantity(),
                item.getOptionType(),
                product.getFileName(),       // productImage로 매핑됨
                sellerNickname
            ) :
            CartItemResponse.createUnavailable(
                item.getProductId(),
                product.getTitle(),          // productName으로 매핑됨
                product.getDescription(),
                item.getSalePrice(),
                item.getOriginalPrice(),
                item.getQuantity(),
                item.getOptionType(),
                product.getFileName(),       // productImage로 매핑됨
                sellerNickname
            );

        result.add(dto);
      }
    }

    return result;
  }

  /**
   * 판매자 닉네임 조회
   */
  private String getSellerNickname(Long sellerId) {
    return sellerPageSVC.findByMemberId(sellerId)
        .map(SellerPage::getNickname)
        .orElse("판매자");
  }

  /**
   * 장바구니 가져오기 또는 생성
   */
  private Cart getOrCreateCart(Long buyerId) {
    return cartRepository.findByBuyerId(buyerId)
        .orElseGet(() -> {
          Cart newCart = new Cart();
          newCart.setBuyerId(buyerId);
          newCart.setTotalPrice(0L);
          return cartRepository.save(newCart);
        });
  }

  /**
   * 상품 가격 설정
   */
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

  /**
   * 장바구니 총액 업데이트
   */
  private void updateCartTotal(Long buyerId) {
    List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);

    long totalPrice = items.stream()
        .filter(item -> {
          Optional<Product> prod = productSVC.getProduct(item.getProductId());
          return prod.isPresent() && "판매중".equals(prod.get().getStatus());
        })
        .mapToLong(item -> item.getSalePrice() * item.getQuantity())
        .sum();

    cartRepository.findByBuyerId(buyerId).ifPresent(cart -> {
      cart.setTotalPrice(totalPrice);
      cartRepository.save(cart);
    });
  }
}