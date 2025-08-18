// src/components/CartItem.jsx
import React from 'react';

const CartItem = ({ item, onRemove, isSelected, onSelect }) => {
  return (
    <div className="cart-item">
      <div className="item-checkbox">
        <input
          type="checkbox"
          checked={isSelected}
          onChange={() => onSelect(item.cartItemId)}
        />
      </div>

      <div className="item-image">
        <div className="image-placeholder">이미지 없음</div>
      </div>

      <div className="item-details">
        <h3 className="item-name">{item.productName}</h3>
        <p className="item-option">{item.optionType}</p>
        <p className="item-note">(상품 출고 3일이 소요돼 ㄱ...)</p>
      </div>

      <div className="item-price">
        <span>(가격)</span>
      </div>

      <button
        className="item-remove-btn"
        onClick={() => onRemove(item.cartItemId)}
      >
        ×
      </button>
    </div>
  );
};

export default CartItem;