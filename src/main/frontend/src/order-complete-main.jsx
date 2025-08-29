// 주문 완료 페이지 엔트리 포인트
import React from 'react'
import { createRoot } from 'react-dom/client'
import OrderCompletePage from './order/pages/OrderCompletePage'

const container = document.getElementById('order-complete-root')
const root = createRoot(container)

root.render(<OrderCompletePage />)
