// 결제 정보 사이드바 컴포넌트

import React from 'react'

export default function PaymentSummary({
  items = []
}) {
  // 계산 값
  const itemCount = items.length
  const totalOriginalAmount = items.reduce((sum, item) => sum + ((item.originalPrice || item.price) * item.quantity), 0)
  const totalDiscountAmount = items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  const totalDiscount = totalOriginalAmount - totalDiscountAmount

  return (
    <div className="payment-summary">
      {/* 제목 - 개수 포함 */}
      <div className="payment-title">결제정보 ({itemCount}건)</div>

      {/* 상품 목록 (헤더 제거) */}
      <div className="order-summary-section">
        <div className="items-summary">
          {items.map((item, index) => (
            <div key={index} className="summary-item">
              <div className="item-info">
                <span className="item-name">{item.productName}</span>
              </div>
              <span className="item-price">{(item.price * item.quantity)?.toLocaleString()}원</span>
            </div>
          ))}
        </div>
      </div>

      {/* 금액 상세 */}
      <div className="payment-details">
        {totalDiscount > 0 && (
          <div className="payment-row discount">
            <span>할인금액</span>
            <span className="discount-amount">-{totalDiscount?.toLocaleString()}원</span>
          </div>
        )}
      </div>

      {/* 총 결제 금액 - Image 2와 동일 */}
      <div className="payment-total">
        <span className="payment-total-label">총 결제 금액</span>
        <span className="payment-total-price">{totalDiscountAmount?.toLocaleString()}원</span>
      </div>


    </div>
  )
}