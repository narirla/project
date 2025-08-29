// 주문 완료 페이지
import React, { useState, useEffect } from 'react'

export default function OrderCompletePage() {
  const [orderData, setOrderData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchOrderData()
  }, [])

  const fetchOrderData = async () => {
    try {
      // URL에서 orderCode 파라미터 추출
      const urlParams = new URLSearchParams(window.location.search)
      const orderCode = urlParams.get('orderCode')

      if (!orderCode) {
        throw new Error('주문번호가 없습니다.')
      }

      setLoading(true)
      const response = await fetch(`/order/complete/data?orderCode=${orderCode}`)

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        const message = errorData.header?.rtmsg || errorData.message || '주문 정보를 불러올 수 없습니다.'
        throw new Error(message)
      }

      const apiResponse = await response.json()

      // ApiResponse 구조에서 오류 체크
      if (apiResponse.header && apiResponse.header.rtcd !== 'S00') {
        throw new Error(apiResponse.header.rtmsg || '주문 정보를 불러올 수 없습니다.')
      }

      // ApiResponse 구조에서 실제 데이터 추출
      const data = apiResponse.body || apiResponse
      setOrderData(data)
    } catch (error) {
      setError(error.message)
    } finally {
      setLoading(false)
    }
  }

  const formatPrice = (price) => {
    return price?.toLocaleString() || '0'
  }

  const formatDate = (dateStr) => {
    try {
      const date = new Date(dateStr)
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).replace(/\. /g, '.').replace(/\.$/, '')
    } catch {
      return dateStr || ''
    }
  }

  if (loading) {
    return (
      <div className="order-complete-container">
        <div className="loading-container">
          <div className="custom-spinner"></div>
          <div className="loading-text">주문 정보를 불러오는 중...</div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="complete-message">
        <h2>오류가 발생했습니다</h2>
        <p>{error}</p>
        <button onClick={() => window.location.href = '/'}>홈으로 이동</button>
      </div>
    )
  }

  if (!orderData) {
    return (
      <div className="complete-message">
        <h2>주문 정보를 찾을 수 없습니다</h2>
        <button onClick={() => window.location.href = '/'}>홈으로 이동</button>
      </div>
    )
  }

  // 총 할인가 계산
  const totalSaleAmount = orderData.orderItems?.reduce((total, item) => {
    return total + (item.cartPrice * item.quantity)
  }, 0) || 0

  return (
    <div className="order-complete-container">
      {/* 주문 완료 메시지 */}
      <div className="complete-message">
        <div className="complete-icon">
          <i className="fas fa-shopping-bag"></i>
          <div className="success-badge">
            <i className="fas fa-check"></i>
          </div>
        </div>
        <h2>주문이 완료되었습니다</h2>
        <p className="order-date">
          {formatDate(orderData.orderDate)} 주문하신 상품의<br />
          주문번호는 {orderData.orderCode}입니다.
        </p>
      </div>

      {/* 주문 상품 */}
      <div className="complete-section">
        <h3 className="section-title">
          주문상품
        </h3>
        <table className="order-table">
          <thead>
            <tr>
              <th>상품명</th>
              <th>옵션</th>
              <th>판매자</th>
              <th>금액</th>
            </tr>
          </thead>
          <tbody>
            {orderData.orderItems?.map((item, index) => (
              <tr key={index}>
                <td>
                  <div className="product-info">
                    <a href={`/product/view/${item.productId}`} className="product-link">
                      <div className="product-image">
                        <img
                          src={item.productImage || "/img/placeholder.png"}
                          alt={item.productName}
                        />
                      </div>
                      <span>{item.productName}</span>
                    </a>
                  </div>
                </td>
                <td>{item.optionType}</td>
                <td>{item.sellerNickname || '판매자'}</td>
                <td>
                  {formatPrice(item.cartPrice * item.quantity)}원
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 주문자 정보와 결제 정보 */}
      <div className="info-sections">
        <div className="info-section">
          <h3 className="section-title">
            주문자 정보
          </h3>
          <table className="info-table">
            <tr>
              <td>주문자명</td>
              <td>{orderData.buyerName || '-'}</td>
            </tr>
            <tr>
              <td>연락처</td>
              <td>{orderData.buyerPhone || '-'}</td>
            </tr>
            <tr>
              <td>이메일</td>
              <td>{orderData.buyerEmail || '-'}</td>
            </tr>
            {orderData.specialRequest && (
              <tr>
                <td>요청사항</td>
                <td>{orderData.specialRequest}</td>
              </tr>
            )}
          </table>
        </div>

        <div className="info-section">
          <h3 className="section-title">
            결제정보
          </h3>
          <table className="info-table">
            <tr>
              <td>결제수단</td>
              <td>간편 결제</td>
            </tr>
            <tr>
              <td className="total-label">총 결제 금액</td>
              <td className="total-amount">{formatPrice(totalSaleAmount)}원</td>
            </tr>
          </table>
        </div>
      </div>

      {/* 하단 버튼 */}
      <div className="complete-actions">
        <a href="/order/history" className="btn btn-primary">주문내역 확인</a>
        <a href="/order/complete/shopping" className="btn btn-secondary">쇼핑 계속하기</a>
      </div>
    </div>
  )
}