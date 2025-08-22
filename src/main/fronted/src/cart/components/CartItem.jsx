// components/CartItem.jsx - 개별 장바구니 상품 컴포넌트
import React, { useState, useCallback, memo } from 'react'

/**
 * 장바구니 개별 상품 컴포넌트
 * 상품 정보 표시, 수량 변경, 선택, 삭제 기능 제공
 */
const CartItem = memo(({
  item,
  selected,
  onSelect,
  onQuantityChange,
  onRemove,
  updating = false
}) => {
  // 컴포넌트 내부 업데이트 상태 관리
  const [isUpdating, setIsUpdating] = useState(false)

  /**
   * 상품 수량 변경 처리
   * 백엔드 API 호출하여 수량 업데이트
   */
  const handleQuantityChange = useCallback(async (newQuantity) => {
    if (newQuantity < 1 || isUpdating || updating) return

    setIsUpdating(true)
    try {
      // 개별 파라미터로 호출 (CartPage와 일치)
      await onQuantityChange(item.productId, item.optionType, newQuantity)
    } catch (error) {
      console.error('수량 변경 실패:', error)
      alert('수량 변경에 실패했습니다')
    } finally {
      setIsUpdating(false)
    }
  }, [item.productId, item.optionType, onQuantityChange, isUpdating, updating])

  /**
   * 상품 삭제 처리
   * 사용자 확인 후 백엔드 API 호출하여 삭제
   */
  const handleRemove = useCallback(async () => {
    if (isUpdating || updating) return
    if (!confirm('해당 상품을 삭제하시겠습니까?')) return

    setIsUpdating(true)
    try {
      // 개별 파라미터로 호출 (CartPage와 일치)
      await onRemove(item.productId, item.optionType)
    } catch (error) {
      console.error('삭제 실패:', error)
      alert('삭제에 실패했습니다')
    } finally {
      setIsUpdating(false)
    }
  }, [item.productId, item.optionType, onRemove, isUpdating, updating])

  /**
   * 상품 선택/해제 처리
   * 체크박스 상태 변경 시 호출
   */
  const handleSelect = useCallback((checked) => {
    onSelect(item.productId, item.optionType, checked)
  }, [item.productId, item.optionType, onSelect])

  // 백엔드 응답 데이터 구조화
  const {
    productName,
    description,
    price,
    originalPrice,
    quantity,
    optionType,
    productImage,
    sellerNickname,
    available = true
  } = item

  // 계산된 값들
  const totalPrice = price * quantity
  const hasDiscount = originalPrice && originalPrice > price
  const isDisabled = isUpdating || updating || !available

  return (
    <div className={`cart-item ${selected ? 'selected' : ''} ${isUpdating ? 'updating' : ''} ${!available ? 'unavailable' : ''}`}>
      {/* 선택 체크박스 */}
      <input
        type="checkbox"
        checked={selected}
        onChange={(e) => handleSelect(e.target.checked)}
        disabled={isDisabled}
        className="item-checkbox"
      />

      {/* 상품 이미지 */}
      <div className="item-image">
        {productImage ? (
          <img
            src={productImage}
            alt={productName}
            loading="lazy"
            onError={(e) => {
              if (e.target) {
                e.target.style.display = 'none'
                if (e.target.nextSibling) {
                  e.target.nextSibling.style.display = 'block'
                }
              }
            }}
          />
        ) : (
          <div className="no-image">이미지 없음</div>
        )}
      </div>

      {/* 상품 정보 */}
      <div className="item-info">
        <div className="item-title">{productName}</div>
        {!available && <div className="sold-out-status">판매중단</div>}
        
        <div className="item-option">옵션: ({optionType})</div>
        
        <div className="item-seller">
          판매자: {sellerNickname || '판매자'}
        </div>
      </div>

      {/* 가격 정보 */}
      <div className="item-price-info">
        {hasDiscount && (
          <div className="item-original-price">
            {originalPrice?.toLocaleString()}원
          </div>
        )}
        <div className="item-price">{totalPrice?.toLocaleString()}원</div>
      </div>

      {/* 삭제 버튼 */}
      <button
        onClick={handleRemove}
        disabled={isDisabled}
        className="remove-btn"
        title="삭제"
        type="button"
      >
        {isUpdating ? (
          <i className="fas fa-spinner fa-spin"></i>
        ) : (
          <i className="fas fa-times"></i>
        )}
      </button>
    </div>
  )
})

CartItem.displayName = 'CartItem'

export default CartItem