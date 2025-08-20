/**
 * 장바구니 API 서비스
 */

const handleResponse = async (response) => {
  if (response.status === 401) {
    window.location.href = '/login'
    return null
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`)
  }

  try {
    return await response.json()
  } catch (error) {
    throw new Error('서버 응답을 처리할 수 없습니다')
  }
}

const handleError = (error, operation) => {
  if (error.message.includes('네트워크')) {
    console.warn(`네트워크 오류: ${operation}`)
  } else {
    console.warn(`${operation} 중 오류가 발생했습니다`)
  }
  throw error
}

export const cartService = {
  /**
   * 장바구니 조회
   */
  async getCart() {
    try {
      const response = await fetch('/api/cart', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include'
      })

      return await handleResponse(response)

    } catch (error) {
      handleError(error, '장바구니 조회')
    }
  },

  /**
   * 장바구니에 상품 추가
   */
  async addToCart(productId, optionType, quantity = 1) {
    try {
      const response = await fetch('/api/cart/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          productId,
          optionType,
          quantity
        })
      })

      return await handleResponse(response)

    } catch (error) {
      handleError(error, '장바구니 추가')
    }
  },

  /**
   * 장바구니 상품 수량 변경
   */
  async updateQuantity(productId, optionType, quantity) {
    try {
      const response = await fetch('/api/cart/quantity', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          productId,
          optionType,
          quantity
        })
      })

      return await handleResponse(response)

    } catch (error) {
      handleError(error, '수량 변경')
    }
  },

  /**
   * 장바구니에서 상품 삭제
   */
  async removeFromCart(productId, optionType) {
    try {
      const response = await fetch('/api/cart/remove', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          productId,
          optionType
        })
      })

      return await handleResponse(response)

    } catch (error) {
      handleError(error, '상품 삭제')
    }
  },

  /**
   * 장바구니 전체 비우기
   */
  async clearCart() {
    try {
      const response = await fetch('/api/cart/clear', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include'
      })

      return await handleResponse(response)

    } catch (error) {
      handleError(error, '장바구니 비우기')
    }
  },

  /**
   * 장바구니 상품 개수 조회
   */
  async getCartItemCount() {
    try {
      const response = await fetch('/api/cart/count', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include'
      })

      const result = await handleResponse(response)
      return result?.count || 0

    } catch (error) {
      console.debug('장바구니 개수 조회 실패')
      return 0
    }
  }
}