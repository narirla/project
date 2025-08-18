// src/pages/CartPage.jsx
import React, { useState } from 'react';
import CartItem from '../components/CartItem';
import '../styles/cart.css';

const CartPage = ({ cartItems, setCartItems, onGoToOrder }) => {
  // 첫 번째 아이템만 선택된 상태로 시작 (사진과 동일)
  const [selectedItems, setSelectedItems] = useState([1]);

  // 빈 장바구니인 경우 (이미지 3)
  if (cartItems.length === 0) {
    return (
      <div className="page-container">
        <div className="breadcrumb">
          장바구니 &gt; 주문결제 &gt; 주문완료
        </div>

        <h1 className="page-title">장바구니</h1>

        <div className="empty-cart">
          <div className="empty-cart-icon">
            <svg width="100" height="100" viewBox="0 0 100 100" fill="none">
              <path d="M20 25H80L75 65H25L20 25Z" stroke="#ddd" strokeWidth="2" fill="none"/>
              <circle cx="30" cy="80" r="4" fill="#ddd"/>
              <circle cx="70" cy="80" r="4" fill="#ddd"/>
              <path d="M20 25L15 10H5" stroke="#ddd" strokeWidth="2"/>
            </svg>
          </div>
          <div className="empty-cart-message">장바구니가 비어 있습니다</div>
          <button className="continue-shopping-btn">
            쇼핑하러 가기
          </button>
        </div>
      </div>
    );
  }

  const totalItems = cartItems.length;
  const selectedCartItems = cartItems.filter(item =>
    selectedItems.includes(item.cartItemId)
  );
  const totalPrice = selectedCartItems.reduce(
    (sum, item) => sum + (item.salePrice * item.quantity), 0
  );

  const handleSelectAll = () => {
    if (selectedItems.length === cartItems.length) {
      setSelectedItems([]);
    } else {
      setSelectedItems(cartItems.map(item => item.cartItemId));
    }
  };

  const handleSelectItem = (itemId) => {
    if (selectedItems.includes(itemId)) {
      setSelectedItems(selectedItems.filter(id => id !== itemId));
    } else {
      setSelectedItems([...selectedItems, itemId]);
    }
  };

  const handleRemoveItem = (itemId) => {
    setCartItems(cartItems.filter(item => item.cartItemId !== itemId));
    setSelectedItems(selectedItems.filter(id => id !== itemId));
  };

  const handleOrder = () => {
    if (selectedItems.length === 0) {
      alert('주문할 상품을 선택해주세요.');
      return;
    }
    onGoToOrder();
  };

  return (
    <div className="page-container">
      <div className="breadcrumb">
        장바구니 &gt; 주문결제 &gt; 주문완료
      </div>

      <h1 className="page-title">장바구니</h1>

      <div className="cart-content">
        <div className="cart-left">
          <div className="cart-header">
            <div className="select-all">
              <input
                type="checkbox"
                checked={selectedItems.length === cartItems.length && cartItems.length > 0}
                onChange={handleSelectAll}
              />
              <span>전체선택({selectedItems.length}/{totalItems})</span>
            </div>
            <button className="delete-selected-btn">선택삭제</button>
          </div>

          <div className="cart-items">
            {cartItems.map(item => (
              <CartItem
                key={item.cartItemId}
                item={item}
                isSelected={selectedItems.includes(item.cartItemId)}
                onSelect={handleSelectItem}
                onRemove={handleRemoveItem}
              />
            ))}
          </div>
        </div>

        <div className="cart-right">
          <div className="order-summary">
            <div className="summary-header">📋 주문요약</div>
            <div className="summary-content">
              <div className="summary-text">선택한 상품은 총 {selectedItems.length}개</div>

              {selectedCartItems.length > 0 && (
                <>
                  <div className="summary-product">
                    <div className="product-name">{selectedCartItems[0]?.productName}</div>
                    <div className="product-details">
                      <span className="product-option">{selectedCartItems[0]?.optionType}</span>
                      <span className="product-price">{selectedCartItems[0]?.salePrice.toLocaleString()}원</span>
                    </div>
                  </div>

                  <div className="summary-total">
                    <span>총 상품 금액</span>
                    <span className="total-amount">{totalPrice.toLocaleString()}원</span>
                  </div>
                </>
              )}

              <div className="summary-note">
                * 해당 주문 시 카드 지급 5% 할인 적용됩니다.
              </div>

              <button className="order-button" onClick={handleOrder}>
                {totalPrice.toLocaleString()}원 주문하기
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;