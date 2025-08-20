/**
 * 주문서 작성 폼 컴포넌트
 * Image 2와 완전 동일하게 구현 - 로딩 제거
 */

import React, { useState } from 'react'

/**
 * 입력 필드 컴포넌트
 */
function Input({
  label,
  placeholder,
  value,
  onChange,
  type = "text",
  required = false,
  disabled = false
}) {
  return (
    <div className="form-group">
      <label className="form-label">
        {required && <span className="required">*</span>}
        {label}
      </label>
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        disabled={disabled}
        className="form-input"
      />
    </div>
  )
}

/**
 * 라디오 버튼 컴포넌트
 */
function RadioButton({ name, value, checked, onChange, children, description }) {
  return (
    <div
      className={`payment-option ${checked ? 'selected' : ''}`}
      onClick={() => onChange(value)}
    >
      <input
        type="radio"
        name={name}
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
        className="payment-radio"
      />
      <div className="payment-content">
        <div className="payment-label">{children}</div>
        {description && <div className="payment-description">{description}</div>}
      </div>
    </div>
  )
}

/**
 * 주문 상품 아이템 컴포넌트
 */
function OrderItem({ productName, price, quantity, seller, productImage, description }) {
  return (
    <div className="order-item">
      <div className="order-item-image">
        {productImage ? (
          <img src={productImage} alt={productName} />
        ) : (
          <span>이미지</span>
        )}
      </div>
      <div className="order-item-info">
        <div className="order-item-title">{productName}</div>
        <div className="order-item-description">{description || '상세설명입니다.'}</div>
        <div className="order-item-meta">경비 • 예약 • {seller}</div>
      </div>
      <div className="order-item-quantity">수량: {quantity}개</div>
      <div className="order-item-price">{(price * quantity)?.toLocaleString()}원</div>
    </div>
  )
}

/**
 * 주문서 작성 폼 컴포넌트
 * Image 2와 완전 동일하게 구현
 */
export default function OrderForm({
  orderItems = [],
  memberInfo = {},
  onSubmit
}) {
  // 폼 데이터 상태
  const [formData, setFormData] = useState({
    ordererName: memberInfo.name || '',
    phone: memberInfo.phone || '',
    email: memberInfo.email || '',
    requirements: '',
    paymentMethod: 'card'
  })

  // 폼 데이터 변경 처리
  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  // 폼 제출 처리
  const handleSubmit = (e) => {
    e.preventDefault()

    if (!formData.ordererName || !formData.phone || !formData.email) {
      alert('필수 정보를 모두 입력해주세요.')
      return
    }

    if (!formData.paymentMethod) {
      alert('결제수단을 선택해주세요.')
      return
    }

    onSubmit({
      ...formData,
      items: orderItems
    })
  }

  return (
    <form onSubmit={handleSubmit} className="order-form">
      {/* 주문자 정보 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-user section-icon"></i>
          주문자 정보
        </div>

        <div className="form-row">
          <Input
            label="주문자명"
            placeholder="주문자명 입력 (수정불가)"
            value={formData.ordererName}
            onChange={(value) => handleInputChange('ordererName', value)}
            required
            disabled={!!memberInfo.name}
          />

          <Input
            label="연락처"
            placeholder="010-1234-5678(수정불가)"
            value={formData.phone}
            onChange={(value) => handleInputChange('phone', value)}
            required
            disabled={!!memberInfo.phone}
          />
        </div>

        <Input
          label="이메일"
          type="email"
          placeholder="moomoo@noomssom.co.kr(수정불가)"
          value={formData.email}
          onChange={(value) => handleInputChange('email', value)}
          required
          disabled={!!memberInfo.email}
        />

        <Input
          label="요구사항"
          placeholder="요구사항을 입력해주세요"
          value={formData.requirements}
          onChange={(value) => handleInputChange('requirements', value)}
        />
      </div>

      {/* 결제수단 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-credit-card section-icon"></i>
          결제수단
        </div>

        <div className="payment-options">
          <RadioButton
            name="payment"
            value="card"
            checked={formData.paymentMethod === 'card'}
            onChange={(value) => handleInputChange('paymentMethod', value)}
            description="무이자 할부 가능"
          >
            신용카드 / 체크카드
          </RadioButton>

          <RadioButton
            name="payment"
            value="bank"
            checked={formData.paymentMethod === 'bank'}
            onChange={(value) => handleInputChange('paymentMethod', value)}
            description="입금 확인 후 예약 확정"
          >
            무통장 입금
          </RadioButton>

          <RadioButton
            name="payment"
            value="kakao"
            checked={formData.paymentMethod === 'kakao'}
            onChange={(value) => handleInputChange('paymentMethod', value)}
            description="간편하고 안전한 결제"
          >
            카카오페이
          </RadioButton>
        </div>
      </div>

      {/* 주문상품 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-shopping-bag section-icon"></i>
          주문상품({orderItems.length}개)
        </div>

        {orderItems.length > 0 ? (
          orderItems.map((item, index) => (
            <OrderItem
              key={index}
              productName={item.productName}
              price={item.price}
              quantity={item.quantity}
              seller={item.seller || '판매자'}
              productImage={item.productImage}
              description={item.description}
            />
          ))
        ) : (
          <div className="no-items">
            <p>주문할 상품이 없습니다.</p>
            <a href="/cart" className="go-cart-btn">장바구니로 이동</a>
          </div>
        )}
      </div>

      {/* 제출 버튼 */}
      <div className="form-actions">
        <button
          type="button"
          onClick={() => window.location.href = '/cart'}
          className="back-button"
        >
          장바구니로 돌아가기
        </button>

        <button
          type="submit"
          className="submit-button"
          disabled={orderItems.length === 0}
        >
          주문하기
        </button>
      </div>
    </form>
  )
}