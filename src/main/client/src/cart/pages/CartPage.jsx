import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { cartService } from '../services/cartService.js'
import CartItem from '../components/CartItem.jsx'

/**
 * 체크박스 컴포넌트
 * 재사용 가능한 체크박스 UI
 */
const Checkbox = React.memo(({ checked, onChange, className = "" }) => {
  return (
    <input
      type="checkbox"
      checked={checked}
      onChange={(e) => onChange(e.target.checked)}
      className={`custom-checkbox ${className}`}
    />
  )
})

/**
 * 에러 메시지 컴포넌트
 * 에러 발생 시 표시되는 UI
 */
const ErrorMessage = React.memo(({ message, onRetry }) => {
  return (
    <div className="error-container">
      <div className="error-icon">
        <i className="fas fa-exclamation-triangle"></i>
      </div>
      <h2>오류가 발생했습니다</h2>
      <p>{message}</p>
      <button onClick={onRetry} className="order-button">다시 시도</button>
    </div>
  )
})

/**
 * 빈 장바구니 상태 컴포넌트
 * 장바구니가 비어있을 때 표시되는 UI
 */
const EmptyCartState = React.memo(() => {
  return (
    <div className="cart-container">
      <div className="breadcrumb">
        장바구니 &gt; 주문결제 &gt; 주문완료
      </div>

      <h1 className="page-title">장바구니</h1>

      <div className="empty-cart-content">
        <div className="empty-cart-icon">
          <i className="fas fa-shopping-cart cart-icon"></i>
        </div>

        <div className="empty-cart-title">
          장바구니가 비어 있습니다
        </div>

        <a href="/" className="shop-button">
          쇼핑하러 가기
        </a>
      </div>
    </div>
  )
})

/**
 * 주문 요약 컴포넌트
 * 선택된 상품들의 총액과 주문 버튼 제공
 */
const OrderSummary = React.memo(({ items, onCheckout, loading = false }) => {
  const selectedCount = items.length
  const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0)

  return (
    <div className="order-summary">
      <div className="summary-title">
        <i className="fas fa-file-alt"></i>
        주문요약
      </div>

      <div className="summary-section">
        <div className="summary-label">
          선택된 상품
          <span className="selected-count">{selectedCount}개</span>
        </div>
      </div>

      {/* 선택된 상품 목록 */}
      <div className="price-list">
        {items.map((item) => (
          <div key={`item-${item.productId}-${item.optionType}`} className="price-item">
            <span className="price-item-name">{item.productName}</span>
            <span className="price-item-type">{item.optionType}</span>
            <span className="price-item-amount">{(item.price * item.quantity)?.toLocaleString()}원</span>
          </div>
        ))}
      </div>

      {/* 총액 표시 */}
      <div className="total-amount">
        <span className="total-label">총 상품 금액</span>
        <span className="total-price">{totalPrice?.toLocaleString()}원</span>
      </div>

      {/* 주문하기 버튼 */}
      <button
        onClick={onCheckout}
        className="order-button"
        disabled={items.length === 0 || loading}
      >
        {loading ? (
          <i className="fas fa-spinner fa-spin"></i>
        ) : (
          `${selectedCount}개 상품 주문하기`
        )}
      </button>

      <div className="notice-text">
        * 할인은 결제 시 자동 적용됩니다.
      </div>
    </div>
  )
})

/**
 * 메인 장바구니 페이지 컴포넌트
 * 장바구니 상품 관리 및 주문 기능 제공
 */
function CartPage() {
  // 상태 관리
  const [cartData, setCartData] = useState(null)
  const [error, setError] = useState(null)
  const [selectedItems, setSelectedItems] = useState(new Set())
  const [updating, setUpdating] = useState(false)
  const [loading, setLoading] = useState(true)

  // 컴포넌트 마운트 시 장바구니 데이터 로드
  useEffect(() => {
    fetchCartData()
  }, [])

  /**
   * 서버에서 장바구니 데이터 가져오기
   * 백엔드 API 호출하여 장바구니 정보 조회
   */
  const fetchCartData = useCallback(async () => {
    try {
      setError(null)
      setLoading(true)

      const data = await cartService.getCart()

      if (data && data.success) {
        setCartData(data)

        // 사용 가능한 상품들을 자동 선택
        if (data.cartItems && data.cartItems.length > 0) {
          const allItems = data.cartItems.map(item =>
            `${item.productId}-${item.optionType}`
          )
          setSelectedItems(new Set(allItems))
        } else {
          setSelectedItems(new Set())
        }
      } else {
        setError('장바구니 데이터를 불러올 수 없습니다')
      }
    } catch (error) {
      console.error('장바구니 조회 실패:', error)
      setError('네트워크 오류가 발생했습니다')
    } finally {
      setLoading(false)
    }
  }, [])

  /**
   * 개별 상품 수량 변경
   * 백엔드 API 호출하여 수량 업데이트
   */
  const handleQuantityChange = useCallback(async (productId, optionType, newQuantity) => {
    try {
      setUpdating(true)
      const result = await cartService.updateQuantity(productId, optionType, newQuantity)
      if (result && result.success) {
        await fetchCartData()
      } else {
        alert(result?.message || '수량 변경에 실패했습니다')
      }
    } catch (error) {
      console.error('수량 변경 실패:', error)
      alert('수량 변경 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [fetchCartData])

  /**
   * 개별 상품 삭제
   * 백엔드 API 호출하여 상품 제거
   */
  const removeItem = useCallback(async (productId, optionType) => {
    try {
      setUpdating(true)
      const result = await cartService.removeFromCart(productId, optionType)
      if (result && result.success) {
        await fetchCartData()
        // 선택 목록에서도 제거
        setSelectedItems(prev => {
          const newSet = new Set(prev)
          newSet.delete(`${productId}-${optionType}`)
          return newSet
        })
      } else {
        alert(result?.message || '삭제에 실패했습니다')
      }
    } catch (error) {
      console.error('삭제 실패:', error)
      alert('삭제 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [fetchCartData])

  /**
   * 개별 상품 선택/해제
   * 체크박스 상태 변경 처리
   */
  const handleSelectItem = useCallback((productId, optionType, selected) => {
    const itemKey = `${productId}-${optionType}`
    setSelectedItems(prev => {
      const newSet = new Set(prev)
      if (selected) {
        newSet.add(itemKey)
      } else {
        newSet.delete(itemKey)
      }
      return newSet
    })
  }, [])

  /**
   * 전체 선택/해제
   * 모든 상품의 선택 상태 일괄 변경
   */
  const handleSelectAll = useCallback((selected) => {
    if (selected) {
      const allItems = cartData?.cartItems?.map(item =>
        `${item.productId}-${item.optionType}`
      ) || []
      setSelectedItems(new Set(allItems))
    } else {
      setSelectedItems(new Set())
    }
  }, [cartData?.cartItems])

  /**
   * 선택된 상품들 삭제
   * 다중 선택된 상품들을 일괄 삭제
   */
  const handleDeleteSelected = useCallback(async () => {
    if (selectedItems.size === 0) {
      alert('삭제할 상품을 선택해주세요.')
      return
    }

    if (!confirm(`${selectedItems.size}개 상품을 삭제하시겠습니까?`)) return

    try {
      setUpdating(true)

      for (const itemKey of selectedItems) {
        const [productId, optionType] = itemKey.split('-')
        await cartService.removeFromCart(parseInt(productId), optionType)
      }

      await fetchCartData()
      setSelectedItems(new Set())
      alert(`${selectedItems.size}개 상품이 삭제되었습니다.`)
    } catch (error) {
      console.error('선택 삭제 실패:', error)
      alert('삭제 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [selectedItems, fetchCartData])

  /**
   * 주문 페이지로 이동
   * 선택된 상품들을 세션에 저장하고 주문 페이지로 이동
   */
  const goToOrder = useCallback(async () => {
    const selectedCartItems = cartData?.cartItems?.filter(item =>
      selectedItems.has(`${item.productId}-${item.optionType}`)
    ) || []

    if (selectedCartItems.length === 0) {
      alert('주문할 상품을 선택해주세요')
      return
    }

    try {
      sessionStorage.setItem('selectedCartItems', JSON.stringify(selectedCartItems))
      window.location.href = '/order'
    } catch (error) {
      console.error('주문 생성 실패:', error)
      alert('주문 생성 중 오류가 발생했습니다')
    }
  }, [cartData?.cartItems, selectedItems])

  // 계산된 값들
  const cartItems = cartData?.cartItems || []
  const selectedCartItems = cartItems.filter(item =>
    selectedItems.has(`${item.productId}-${item.optionType}`)
  )
  const isAllSelected = cartItems.length > 0 && selectedItems.size === cartItems.length

  // 로딩 상태
  if (loading) {
    return (
      <div className="cart-container">
        <div className="breadcrumb">
          장바구니 &gt; 주문결제 &gt; 주문완료
        </div>
        <h1 className="page-title">장바구니</h1>
        <div className="loading-container">
          <i className="fas fa-spinner fa-spin loading-spinner"></i>
          <p>장바구니를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  // 에러 발생
  if (error) {
    return (
      <div className="cart-container">
        <ErrorMessage message={error} onRetry={fetchCartData} />
      </div>
    )
  }

  // 빈 장바구니
  if (cartData?.empty) {
    return <EmptyCartState />
  }

  // 상품이 있는 장바구니
  return (
    <div className="cart-container">
      <div className="breadcrumb">
        장바구니 &gt; 주문결제 &gt; 주문완료
      </div>

      <a href="/" className="continue-shopping">← 쇼핑 계속하기</a>

      <h1 className="page-title">장바구니</h1>

      <div className="cart-content">
        {/* 전체 선택 및 삭제 컨트롤 */}
        <div className="select-all-section">
          <div className="left">
            <Checkbox
              checked={isAllSelected}
              onChange={handleSelectAll}
            />
            <span className="select-all-text">
              전체선택({selectedItems.size}/{cartItems.length})
            </span>
          </div>

          <div className="select-actions">
            <button
              onClick={handleDeleteSelected}
              disabled={selectedItems.size === 0 || updating}
              className="delete-selected-btn"
            >
              선택삭제
            </button>
          </div>
        </div>

        {/* 장바구니 상품 목록 */}
        {cartItems.map((item) => (
          <CartItem
            key={`${item.productId}-${item.optionType}`}
            item={item}
            selected={selectedItems.has(`${item.productId}-${item.optionType}`)}
            onSelect={handleSelectItem}
            onQuantityChange={handleQuantityChange}
            onRemove={removeItem}
            updating={updating}
          />
        ))}

        {/* 주문 요약 */}
        <OrderSummary
          items={selectedCartItems}
          onCheckout={goToOrder}
          loading={updating}
        />
      </div>
    </div>
  )
}

export default CartPage