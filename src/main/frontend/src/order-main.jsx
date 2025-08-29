import 'vite/modulepreload-polyfill'
import React from 'react'
import ReactDOM from 'react-dom/client'
import OrderPage from './order/pages/OrderPage'

ReactDOM.createRoot(document.getElementById('order-root')).render(
  <React.StrictMode>
    <OrderPage />
  </React.StrictMode>
)
