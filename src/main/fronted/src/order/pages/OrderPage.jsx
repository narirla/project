/**
 * 주문결제 메인 페이지 컴포넌트
 * Image 2와 완전 동일하게 구현 - 주문 진행 상태 제거
 */

import React, { useState, useEffect } from 'react'
import { orderService } from '../services/orderService.js'
import OrderForm from '../components/OrderForm.jsx'
import PaymentSummary from '../components/PaymentSummary.jsx'

/**
 * 에러 컴포넌트
 */
function ErrorMessage({ message, onRetry }) {
  return (
    <div className="error-container">
      <div className="error-icon">
        <i className="fas fa-exclamation-triangle"></i>
      </div>
      <h2>오류가 발생했습니다</h2>
      <p>{message}</p>
      <button onClick={onRetry} className="payment-button">다시 시도</button>
    </div>
  )
}

/**
 * 메인 주문결제 페이지 컴포넌트
 * Image 2와 완전 동일하게 구현
 */
function OrderPage() {
  // 상태 관리
  const [orderData, setOrderData] = useState(null)
  const [memberInfo, setMemberInfo] = useState({})
  const [error, setError] = useState(null)
  const [processing, setProcessing] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('card')

  // 초기 데이터 로드
  useEffect(() => {
    fetchOrderData()
  }, [])

  /**
   * 주문 데이터 가져오기
   */
  const fetchOrderData = async () => {
    try {
      setError(null)

      // 세션 스토리지에서 선택된 상품들 가져오기
      const selectedItems = sessionStorage.getItem('selectedCartItems')

      if (!selectedItems) {
        // 선택된 상품이 없으면 장바구니로 리다이렉트
        alert('주문할 상품을 선택해주세요.')
        window.location.href = '/cart'
        return
      }

      const items = JSON.parse(selectedItems)

      // 회원 정보 가져오기 (실제로는 서버에서)
      const memberData = {
        name: '홍길동',
        phone: '010-1234-5678',
        email: 'hong@example.com'
      }

      setOrderData({ items })
      setMemberInfo(memberData)

    } catch (error) {
      console.error('주문 데이터 조회 실패:', error)
      setError('주문 정보를 불러올 수 없습니다')
    }
  }

  /**
   * 주문서 제출 처리
   */
  const handleOrderSubmit = async (formData) => {
    try {
      setProcessing(true)

      // 주문 생성
      const orderResult = await orderService.createOrder({
        ...formData,
        totalAmount: calculateTotalAmount(formData.items)
      })

      if (orderResult && orderResult.success) {
        // 주문 성공시 결제 처리로 이동
        await handlePayment(orderResult.orderId, formData.paymentMethod)
      } else {
        alert(orderResult?.message || '주문 생성에 실패했습니다')
      }
    } catch (error) {
      console.error('주문 생성 실패:', error)
      alert('주문 처리 중 오류가 발생했습니다')
    } finally {
      setProcessing(false)
    }
  }

  /**
   * 결제 처리
   */
  const handlePayment = async (orderId, paymentMethod) => {
    try {
      setProcessing(true)

      const totalAmount = calculateTotalAmount(orderData.items)

      const paymentResult = await orderService.processPayment({
        orderId,
        paymentMethod,
        amount: totalAmount
      })

      if (paymentResult && paymentResult.success) {
        // 결제 성공
        alert('결제가 완료되었습니다!')

        // 세션 스토리지 정리
        sessionStorage.removeItem('selectedCartItems')

        // 주문 완료 페이지로 이동
        window.location.href = `/order/complete/${orderId}`
      } else {
        alert(paymentResult?.message || '결제에 실패했습니다')
      }
    } catch (error) {
      console.error('결제 처리 실패:', error)
      alert('결제 처리 중 오류가 발생했습니다')
    } finally {
      setProcessing(false)
    }
  }

  /**
   * 총 결제 금액 계산
   */
  const calculateTotalAmount = (items) => {
    return items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  }

  /**
   * 결제 방법 변경 핸들러
   */
  const handlePaymentMethodChange = (method) => {
    setPaymentMethod(method)
  }

  // 계산된 값들
  const items = orderData?.items || []

  // 에러 발생
  if (error) {
    return (
      <div className="order-container">
        <ErrorMessage message={error} onRetry={fetchOrderData} />
      </div>
    )
  }

  // 데이터가 없으면 간단히 표시
  if (!orderData) {
    return (
      <div className="order-container">
        <div className="breadcrumb">
          <a href="/cart">장바구니</a> &gt;
          <span className="current"> 주문결제</span> &gt;
          <span> 주문완료</span>
        </div>
        <h1 className="page-title">주문결제</h1>
        <p>주문 정보를 불러오는 중...</p>
      </div>
    )
  }

  return (
    <div className="order-container">
      {/* 브레드크럼 - Image 2와 동일 */}
      <div className="breadcrumb">
        <a href="/cart">장바구니</a> &gt;
        <span className="current"> 주문결제</span> &gt;
        <span> 주문완료</span>
      </div>

      {/* 페이지 제목 */}
      <h1 className="page-title">주문결제</h1>

      <div className="order-layout">
        {/* 왼쪽: 주문 폼 */}
        <div className="order-content">
          <OrderForm
            orderItems={items}
            memberInfo={memberInfo}
            onSubmit={handleOrderSubmit}
            loading={processing}
            onPaymentMethodChange={handlePaymentMethodChange}
          />
        </div>

        {/* 오른쪽: 결제 정보 사이드바 */}
        <div className="order-sidebar">
          <PaymentSummary
            items={items}
            onPayment={(amount) => {
              // 폼 데이터와 함께 결제 처리
              const formData = {
                ordererName: memberInfo.name,
                phone: memberInfo.phone,
                email: memberInfo.email,
                requirements: '',
                paymentMethod,
                items
              }
              handleOrderSubmit(formData)
            }}
            paymentMethod={paymentMethod}
          />
        </div>
      </div>
    </div>
  )
}

export default OrderPage