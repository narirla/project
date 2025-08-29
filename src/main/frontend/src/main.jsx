import 'vite/modulepreload-polyfill'
import React from 'react'
import ReactDOM from 'react-dom/client'
import CartPage from './cart/pages/CartPage'

ReactDOM.createRoot(document.getElementById('cart-root')).render(
  <React.StrictMode>
    <CartPage />
  </React.StrictMode>
)