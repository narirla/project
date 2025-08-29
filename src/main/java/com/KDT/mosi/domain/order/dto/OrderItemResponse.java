package com.KDT.mosi.domain.order.dto;

import com.KDT.mosi.domain.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 주문 상품 응답 DTO
 */
@Data
public class OrderItemResponse {
  private Long productId;
  private String productName;
  private String guideYn;
  private String description;
  private String productImage;
  private Long currentPrice;         // 할인가 (PRODUCT 테이블)
  private Long currentOriginalPrice; // 정가 (PRODUCT 테이블)
  private Long cartPrice;           // 주문서 저장된 당시 할인가
  private Long cartOriginalPrice;   // 주문서 저장된 당시 정가
  private Long quantity;
  private String optionType;
  private String sellerNickname;    // 판매자 닉네임 (SELLER_PAGE 테이블)
  private String productStatus;     // "판매중", "판매대기" (PRODUCT 테이블)
  private boolean available;        // 주문 가능 여부
  private boolean priceChanged;     // 가격 변동 여부
  private String statusMessage;     // "판매중단" 사용자에게 보여줄 메시지
  private Date createdDate;

  /**
   * 판매중인 상품
   */
  public static OrderItemResponse createAvailable(Long productId, String productName, String guideYn, String description,
                                                  Long currentPrice, Long currentOriginalPrice,
                                                  Long cartPrice, Long cartOriginalPrice,
                                                  Long quantity, String optionType,
                                                  String productImage, String sellerNickname,
                                                  Date createdDate) {
    OrderItemResponse response = new OrderItemResponse();
    response.setProductId(productId);
    response.setProductName(productName);
    response.setGuideYn(guideYn);
    response.setDescription(description);
    response.setProductImage(productImage);
    response.setCurrentPrice(currentPrice);
    response.setCurrentOriginalPrice(currentOriginalPrice);
    response.setCartPrice(cartPrice);
    response.setCartOriginalPrice(cartOriginalPrice);
    response.setQuantity(quantity);
    response.setOptionType(optionType);
    response.setSellerNickname(sellerNickname);
    response.setProductStatus("판매중");
    response.setAvailable(true);
    response.setPriceChanged(!currentPrice.equals(cartPrice));
    response.setCreatedDate(createdDate);
    return response;
  }

  /**
   * 판매중단된 상품
   */
  public static OrderItemResponse createUnavailable(Long productId, String productName, String description,
                                                    Long currentPrice, Long currentOriginalPrice,
                                                    Long cartPrice, Long cartOriginalPrice,
                                                    Long quantity, String optionType,
                                                    String productImage, String sellerNickname,
                                                    String productStatus, String statusMessage,
                                                    Date createdDate) {
    OrderItemResponse response = new OrderItemResponse();
    response.setProductId(productId);
    response.setProductName(productName);
    response.setDescription(description);
    response.setProductImage(productImage);
    response.setCurrentPrice(currentPrice);
    response.setCurrentOriginalPrice(currentOriginalPrice);
    response.setCartPrice(cartPrice);
    response.setCartOriginalPrice(cartOriginalPrice);
    response.setQuantity(quantity);
    response.setOptionType(optionType);
    response.setSellerNickname(sellerNickname);
    response.setProductStatus(productStatus);
    response.setAvailable(false);
    response.setPriceChanged(!currentPrice.equals(cartPrice));
    response.setStatusMessage(statusMessage);
    response.setCreatedDate(createdDate);
    return response;
  }
}
