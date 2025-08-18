
const api = {
  get: (url) => fetch(`/api${url}`, { credentials: 'include' }),
  post: (url, data) => fetch(`/api${url}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data)
  }),
  put: (url, data) => fetch(`/api${url}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data)
  }),
  delete: (url, data) => fetch(`/api${url}`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data)
  })
};

// 장바구니 API
export const getCart = async () => {
  const response = await api.get('/cart/api');
  return await response.json();
};

export const addToCart = async (productData) => {
  const response = await api.post('/cart/add', productData);
  return await response.json();
};

export const updateQuantity = async (productId, optionType, quantity) => {
  const response = await api.put('/cart/quantity', { productId, optionType, quantity });
  return await response.json();
};

export const removeFromCart = async (productId, optionType) => {
  const response = await api.delete('/cart/remove', { productId, optionType });
  return await response.json();
};

export const getCartCount = async () => {
  const response = await api.get('/cart/count');
  return await response.json();
};

export const getMemberInfo = async () => {
  const response = await api.get('/cart/member-info');
  return await response.json();
};

// 주문 API
export const createOrder = async (orderData) => {
  const response = await api.post('/order/create', orderData);
  return await response.json();
};