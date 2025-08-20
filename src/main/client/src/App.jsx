import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import CartPage from './cart/pages/CartPage'
import OrderPage from './order/pages/OrderPage'

function App() {
  return (
    <Router>
      <div className="App">
        <nav>
          <Link to="/">홈</Link>
          <Link to="/cart">장바구니</Link>
          <Link to="/order">주문하기</Link>
        </nav>
        <Routes>
          <Route path="/" element={<div>Mosi Project 홈</div>} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/order" element={<OrderPage />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App