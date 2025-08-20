/**
 * 주문 API 서비스 레이어
 * 모든 주문 관련 API 호출을 담당
 */

// 공통 응답 처리 함수
const handleResponse = async (response) => {
  if (response.status === 401) {
    window.location.href = '/login'
    return null
  }

  try {
    return await response.json()
  } catch (error) {
    console.error('JSON 파싱 오류:', error)
    throw new Error('서버 응답을 처리할 수 없습니다')
  }
}

// 공통 에러 처리 함수
const handleError = (error, operation) => {
  console.error(`${operation} 실패:`, error)
  throw error
}

export const orderService = {
  /**
   * 주문 정보 조회 (선택된 장바구니 상품들)
   * @returns {Promise<Object>} 주문 가능한 상품 데이터
   */
  async getOrderInfo() {
    try {
      const response = await fetch('/order/info', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      return await handleResponse(response)
    } catch (error) {
      handleError(error, '주문 정보 조회')
    }
  },

  /**
   * 주문 생성
   * @param {Object} orderData - 주문 데이터
   * @param {string} orderData.ordererName - 주문자명
   * @param {string} orderData.phone - 연락처
   * @param {string} orderData.email - 이메일
   * @param {string} orderData.requirements - 요구사항
   * @param {string} orderData.paymentMethod - 결제수단
   * @param {Array} orderData.items - 주문 상품들
   * @returns {Promise<Object>} 주문 생성 결과
   */
  async createOrder(orderData) {
    try {
      const response = await fetch('/order/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(orderData)
      })

      return await handleResponse(response)
    } catch (error) {
      handleError(error, '주문 생성')
    }
  },

  /**
   * 결제 처리
   * @param {Object} paymentData - 결제 데이터
   * @param {number} paymentData.orderId - 주문 ID
   * @param {string} paymentData.paymentMethod - 결제수단
   * @param {number} paymentData.amount - 결제 금액
   * @returns {Promise<Object>} 결제 처리 결과
   */
  async processPayment(paymentData) {
    try {
      const response = await fetch('/order/payment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(paymentData)
      })

      return await handleResponse(response)
    } catch (error) {
      handleError(error, '결제 처리')
    }
  },

  /**
   * 주문 내역 조회
   * @param {number} orderId - 주문 ID
   * @returns {Promise<Object>} 주문 상세 정보
   */
  async getOrderDetail(orderId) {
    try {
      const response = await fetch(`/order/${orderId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      return await handleResponse(response)
    } catch (error) {
      handleError(error, '주문 상세 조회')
    }
  },

  /**
   * 주문 취소
   * @param {number} orderId - 주문 ID
   * @param {string} reason - 취소 사유
   * @returns {Promise<Object>} 취소 처리 결과
   */
  async cancelOrder(orderId, reason) {
    try {
      const response = await fetch(`/order/${orderId}/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reason })
      })

      return await handleResponse(response)
    } catch (error) {
      handleError(error, '주문 취소')
    }
  }
}