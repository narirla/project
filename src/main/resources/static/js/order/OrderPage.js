// src/pages/OrderPage.jsx
import React, { useState } from 'react';
import '../styles/order.css';

// 사진과 동일한 주문자 정보
const mockUserInfo = {
  name: '부상욱 염민',
  phone: '010-1234-5678',
  email: 'mosiuser@gmail.com',
  request: '여행을 잘부탁 & 언제출발?'
};

const OrderPage = ({ cartItems, onBack, onOrderComplete }) => {
  const [paymentMethod, setPaymentMethod] = useState('card');

  const totalPrice = cartItems.reduce(
    (sum, item) => sum + (item.salePrice * item.quantity), 0
  );

  const handleOrder = () => {
    onOrderComplete();
  };

  return (
    <div className="page-container">
      <div className="breadcrumb">
        장바구니 &gt; 주문결제 &gt; 주문완료
      </div>
      
      <h1 className="page-title">주문결제</h1>
      
      <div className="order-content">
        <div className="order-section">
          <h2 className="section-title">👤 주문자 정보</h2>
          <div className="user-info-box">
            <div className="info-row">
              <span className="info-label">주문자명</span>
              <span className="info-value">{mockUserInfo.name}</span>
            </div>
            <div className="info-row">
              <span className="info-label">연락처</span>
              <span className="info-value">{mockUserInfo.phone}</span>
            </div>
            <div className="info-row">
              <span className="info-label">이메일</span>
              <span className="info-value">{mockUserInfo.email}</span>
            </div>
            <div className="info-row">
              <span className="info-label">요청사항</span>
              <span className="info-value">{mockUserInfo.request}</span>
            </div>
          </div>
        </div>
        
        <div className="order-section">
          <h2 className="section-title">💳 결제수단</h2>
          <div className="payment-methods">
            <label className="payment-option">
              <input 
                type="radio" 
                value="card"
                checked={paymentMethod === 'card'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <span>신용카드 / 체크카드</span>
            </label>
            <label className="payment-option">
              <input 
                type="radio" 
                value="transfer"
                checked={paymentMethod === 'transfer'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <span>무통장입금</span>
            </label>
          </div>
        </div>
        
        <div className="order-section">
          <h2 className="section-title">📋 주문상품(1개)</h2>
          <div className="order-product">
            <div className="product-image">
              <div className="image-placeholder">이미지 없음</div>
            </div>
            <div className="product-info">
              <h3 className="product-name">{cartItems[0]?.productName}</h3>
              <p className="product-option">{cartItems[0]?.optionType}</p>
            </div>
            <div className="product-price">
              {cartItems[0]?.salePrice.toLocaleString()}원
            </div>
          </div>
        </div>
        
        <div className="order-section">
          <h2 className="section-title">💰 결제정보</h2>
          <div className="payment-summary">
            <div className="payment-row">
              <span className="payment-label">상품금액</span>
              <span className="payment-value">{totalPrice.toLocaleString()}원</span>
            </div>
            <div className="payment-row">
              <span className="payment-label">할인 금액</span>
              <span className="payment-value">50000원</span>
            </div>
            <div className="payment-row payment-total">
              <span className="payment-label">총 결제 금액은</span>
              <span className="payment-value total-amount">{totalPrice.toLocaleString()}원</span>
            </div>
          </div>
          
          <div className="payment-notice">
            <p>※ 상품 출고 후 취소 및 교환, 환불이 불가능합니다.</p>
            <p>※ 미성년자가 결제하는 경우 보호자의 동의가 필요합니다.</p>
            <p>※ 상품은 주 출고 과정에서 변경될 수 있습니다.</p>
          </div>
          
          <button className="final-order-btn" onClick={handleOrder}>
            {totalPrice.toLocaleString()}원 결제하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default OrderPage;