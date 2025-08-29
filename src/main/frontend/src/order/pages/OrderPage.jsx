// 주문결제 메인 페이지
import React, { useState, useEffect } from 'react'
import { orderService } from '../services/OrderService'
import OrderForm from '../components/OrderForm'
import PaymentSummary from '../components/PaymentSummary'

// 에러 컴포넌트
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

// 주문결제 페이지 컴포넌트
function OrderPage() {
  // 상태 관리
  const [orderData, setOrderData] = useState(null)
  const [memberInfo, setMemberInfo] = useState({})
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('')
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false)
  const [showPaymentSuccessModal, setShowPaymentSuccessModal] = useState(false)

  // 사용자 입력 요청사항
  const [requirements, setRequirements] = useState('')


  // 계산된 값들
  const items = orderData?.orderItems || []
  const totalAmount = items.reduce((sum, item) => sum + (item.price * item.quantity), 0)

  // 초기 데이터 로드
  useEffect(() => {
    fetchOrderData()

    // 글로벌 함수로 즉시 결제 함수 등록
    window.processPaymentImmediately = processPaymentImmediately

    // 모달 표시
    setShowMemberInfoModal(true)



    return () => {
      // 컴포넌트 언마운트 시 정리
      delete window.processPaymentImmediately
    }
  }, [])

  // 주문 데이터 조회
  const fetchOrderData = async () => {
    try {
      setError(null)
      setLoading(true)

      // 1. 세션 스토리지에서 선택된 상품들 가져오기
      let selectedItems = sessionStorage.getItem('selectedCartItems')
      let items = null

      if (selectedItems) {
        items = JSON.parse(selectedItems)
      } else {
        // 2. 세션 스토리지에 없으면 서버 세션에서 주문 상태 복원 시도
        try {
          const sessionResponse = await fetch('/order/session-state', {
            method: 'GET',
            credentials: 'include'
          })

          if (sessionResponse.ok) {
            const sessionData = await sessionResponse.json()
            const orderState = sessionData.body || sessionData

            if (orderState.cartItemIds && orderState.cartItemIds.length > 0) {
              // 서버 세션에서 복원된 cartItemIds로 장바구니 데이터 조회
              const cartResponse = await fetch(`/cart/items?ids=${orderState.cartItemIds.join(',')}`, {
                method: 'GET',
                credentials: 'include'
              })

              if (cartResponse.ok) {
                const cartData = await cartResponse.json()
                items = cartData.body || cartData
                
                // 복원된 데이터를 세션 스토리지에도 저장
                sessionStorage.setItem('selectedCartItems', JSON.stringify(items))
              }
            }
          }
        } catch (error) {
          // 무시
        }
      }

      if (!items || items.length === 0) {
        // 선택된 상품이 없으면 장바구니로 리다이렉트 (메시지 없이)
        window.location.href = '/cart'
        return
      }

      // 회원 정보 가져오기
      try {
        const memberResponse = await fetch('/order/member-info', {
          method: 'GET',
          credentials: 'include'
        })

        if (memberResponse.ok) {
          const apiResponse = await memberResponse.json()

          // ApiResponse 구조에서 데이터 추출
          const memberData = apiResponse.body || apiResponse
          setMemberInfo({
            name: memberData.name || '',
            phone: memberData.tel || '',
            email: memberData.email || ''
          })
        } else {
          // 기본값 설정
          setMemberInfo({
            name: '',
            phone: '',
            email: ''
          })
        }
      } catch (error) {
        // 기본값 설정
        setMemberInfo({ name: '', phone: '', email: '' })
      }

      setOrderData({ orderItems: items })

    } catch (error) {
      setError('주문 정보를 불러올 수 없습니다')
    } finally {
      setLoading(false)
    }
  }

  // 주문서 제출 처리
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
      alert('주문에 실패했습니다')
    } finally {
      setProcessing(false)
    }
  }

  // 결제 처리
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
      alert('결제에 실패했습니다')
    } finally {
      setProcessing(false)
    }
  }

  // 총 결제 금액 계산
  const calculateTotalAmount = (items) => {
    return items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  }

  // 결제 방법 변경
  const handlePaymentMethodChange = (method) => {
    setPaymentMethod(method)
  }

  // 임시 결제 처리
  const processPaymentImmediately = async (selectedPaymentMethod) => {
    try {
      setShowPaymentModal(true)
      setProcessing(true)

      // 임시 결제 처리 API 호출 (시뮬레이션)
      await new Promise(resolve => setTimeout(resolve, 1500)) // 1.5초 대기

      // 임시 결제 완료 메시지 표시 (업계 표준)
      alert('결제가 완료되었습니다.')

    } catch (error) {
      alert('결제 처리 중 오류가 발생했습니다')
    } finally {
      setShowPaymentModal(false)
      setProcessing(false)
    }
  }

  // 주문 완료 처리
  const onPayment = async () => {
    // 결제 수단 선택 여부 확인
    if (!paymentMethod) {
      alert('결제 수단을 먼저 선택해주세요.')
      return
    }

    try {
      setProcessing(true)

      const requestData = {
        cartItemIds: items.map(item => item.cartItemId),
        ordererName: memberInfo.name || '',
        phone: memberInfo.phone || '',
        email: memberInfo.email || '',
        requirements: requirements || '',
        paymentMethod,
        totalAmount: totalAmount
      }

      const response = await fetch('/order/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
      })

      if (response.ok) {
        const apiResponse = await response.json()

        // ApiResponse 구조에서 오류 체크
        if (apiResponse.header && apiResponse.header.rtcd !== 'S00') {
          throw new Error(apiResponse.header.rtmsg || '주문 처리 중 오류가 발생했습니다.')
        }

        // ApiResponse 구조에서 실제 데이터 추출
        const result = apiResponse.body || apiResponse

        // 결제 완료 처리 (상태를 결제대기 → 결제완료로 변경)
        const orderCode = result.orderCode || result.data?.orderCode
        if (orderCode) {
          await fetch('/order/payment', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              orderCode: orderCode,
              paymentMethod: paymentMethod
            })
          })
        }

        // 결제 완료 로딩 모달 표시
        setShowPaymentSuccessModal(true)

        // 2초 후 주문 완료 페이지로 자동 이동
        setTimeout(() => {
          // 세션 스토리지 정리
          sessionStorage.removeItem('selectedCartItems')

          // 헤더 장바구니 개수 업데이트
          if (window.updateCartCount) {
            window.updateCartCount()
          }

          // 주문 완료 페이지로 이동
          const orderCode = result.orderCode || result.data?.orderCode
          if (orderCode) {
            window.location.href = `/order/complete?orderCode=${orderCode}`
          } else {
            alert('주문 코드를 찾을 수 없습니다. 주문내역에서 확인해주세요.')
            window.location.href = '/mypage/orders'
          }
        }, 2000)
      } else {
        const errorData = await response.json().catch(() => ({}))
        const message = errorData.header?.rtmsg || errorData.message || `결제에 실패했습니다: ${response.status} ${response.statusText}`
        alert(message)
      }
    } catch (error) {
      alert('주문에 실패했습니다')
    } finally {
      setProcessing(false)
    }
  }

  // 로딩 화면 (장바구니와 동일한 스타일)
  if (loading) {
    return (
      <div className="order-container">
        <div className="loading-container">
          <div className="custom-spinner"></div>
          <div className="loading-text">주문 정보를 불러오는 중...</div>
        </div>
      </div>
    )
  }

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
        <div className="continue-shopping-section">
          <a href="/cart" className="continue-shopping-btn">&lt; 장바구니로 돌아가기</a>
        </div>
        <p>주문 정보를 불러오는 중...</p>
      </div>
    )
  }

  return (
    <div className="order-container">
      {/* 장바구니로 돌아가기 버튼 - 장바구니와 동일한 위치 */}
      <div className="continue-shopping-section">
        <a href="/cart" className="continue-shopping-btn">&lt; 장바구니로 돌아가기</a>
      </div>

      <div className="order-layout">
        {/* 왼쪽: 주문 폼 */}
        <div className="order-content">
          <OrderForm
            orderItems={items}
            memberInfo={memberInfo}
            onSubmit={handleOrderSubmit}
            loading={processing}
            onPaymentMethodChange={handlePaymentMethodChange}
            paymentMethod={paymentMethod}
            onRequirementsChange={setRequirements}
          />
        </div>

        {/* 오른쪽: 결제 정보 사이드바 */}
        <div className="order-sidebar">
          <PaymentSummary
            items={items}
          />
        </div>
      </div>

      {/* 하단 안내 문구 - 전체 레이아웃 하단 */}
      <div className="order-notice">
        <div className="notice-item">※ 관광 상품은 예약 확정 후 취소 불가능합니다.</div>
        <div className="notice-item">※ 날씨나 현지 사정으로 인한 변경 시 사전안내 드립니다.</div>
        <div className="notice-item">※ 결제 후 예약 확정 안내를 받으실 수 있습니다.</div>
      </div>

      {/* 하단 결제 버튼 - 전체 레이아웃 하단 */}
      <div className="order-bottom">
        <button
          onClick={onPayment}
          className="full-payment-button"
          disabled={!items.length}
        >
          {totalAmount?.toLocaleString()}원 결제하기
        </button>
      </div>



      {/* 회원 정보 자동 입력 모달 */}
      {showMemberInfoModal && (
        <div className="modal-overlay" onClick={() => setShowMemberInfoModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>알림</h3>
              <button
                className="modal-close"
                onClick={() => setShowMemberInfoModal(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <p>회원 정보가 자동으로 입력됩니다.<br/>(회원 정보 수정은 내 정보&gt;회원 정보 관리에서 가능)</p>
            </div>
            <div className="modal-footer">
              <button
                className="modal-confirm-btn"
                onClick={() => setShowMemberInfoModal(false)}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 결제 완료 로딩 모달 - 원래 스타일 */}
      {showPaymentSuccessModal && (
        <div className="modal-overlay">
          <div className="loading-modal-content">
            <div className="loading-container">
              <div className="custom-spinner"></div>
              <div className="loading-text">주문 처리 중입니다.<br/>잠시만 기다려 주세요.</div>
            </div>
          </div>
        </div>
      )}

    </div>
  )
}

export default OrderPage