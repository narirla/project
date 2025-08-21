document.addEventListener("DOMContentLoaded", function() {
  // 상품 이미지 슬라이더
  const slides = document.querySelectorAll(".slide");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  let currentIndex = 0;

  // 슬라이드 표시
  function showSlide(index) {
    slides.forEach((slide, i) => {
      slide.classList.toggle("active", i === index);
    });
  }

  // 이전 버튼
  if (prevBtn) {
  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === 0) ? slides.length - 1 : currentIndex - 1;
    showSlide(currentIndex);
  });
  }

  // 다음 버튼
  if (nextBtn) {
  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === slides.length - 1) ? 0 : currentIndex + 1;
    showSlide(currentIndex);
  });
  }

  // 초기 슬라이드 표시
  if (slides.length > 0) {
  showSlide(currentIndex);
  }

  // 가격 포맷팅
  const priceElems = document.querySelectorAll(".allPrice");
  priceElems.forEach(priceElem => {
    const originalText = priceElem.textContent;
    const onlyNumber = originalText.match(/[\d.-]+/g);
    if (onlyNumber) {
      const numberValue = Number(onlyNumber.join(""));
      if (!isNaN(numberValue)) {
        const formattedNumber = numberValue.toLocaleString();
        const newText = originalText.replace(/[\d.,-]+/, formattedNumber);
        priceElem.textContent = newText;
      }
    }
  });

  // 탭 메뉴 처리
  const tabLinks = document.querySelectorAll(".tab-menu a");
  tabLinks.forEach(link => {
    link.addEventListener("click", function(event) {
      event.preventDefault();
      tabLinks.forEach(item => item.classList.remove("active"));
      this.classList.add("active");

      const targetId = this.getAttribute("href").substring(1);
      const targetElement = document.getElementById(targetId);
      if(targetElement) {
        targetElement.scrollIntoView({ behavior: "smooth" });
      }
    });
  });

  // 확장 버튼 처리
  const expandButtons = document.querySelectorAll('.ex_btn');
  expandButtons.forEach(button => {
    button.style.cursor = 'pointer';
    button.addEventListener('click', () => {
      const policyMenu = button.closest('.policy-menu');
      if (!policyMenu) return;

      const content = policyMenu.nextElementSibling;
      if (!content || !content.classList.contains('policy-content')) return;

      content.classList.toggle('active');
      button.classList.toggle('active');
    });
  });

  // 버튼 이벤트 연결
  const cartButtons = document.querySelectorAll('.btn-cart');
  cartButtons.forEach(button => {
    button.addEventListener('click', handleAddToCart);
  });

  const buyButtons = document.querySelectorAll('.btn-buy');
  buyButtons.forEach(button => {
    button.addEventListener('click', handleBuyNow);
  });
});

// 장바구니 추가
async function handleAddToCart(event) {
  const button = event.target;
  const optionType = button.getAttribute('data-option');
  
  button.disabled = true;
  
  try {
    // 유효성 검사
    const validation = await validatePurchase(optionType);
    if (!validation.isValid) {
      showAlert(validation.message);
      return;
    }
    
    const productId = getProductIdFromPage();
    
    const result = await addToCartAPI(productId, optionType, 1);
    
    if (result.header && result.header.rtcd === 'S00') {
      showSuccessModal('장바구니에 상품이 추가되었습니다');
      updateCartCount();
    } else {
      const message = result.header?.rtmsg || '장바구니 추가에 실패했습니다';
      showAlert(message);
    }
    
  } catch (error) {
    console.error('장바구니 추가 오류:', error);
    showAlert('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
  } finally {
    button.disabled = false;
  }
}

// 즉시 구매
async function handleBuyNow(event) {
  const button = event.target;
  const optionType = button.getAttribute('data-option');
  
  button.disabled = true;
  
  try {
    // 유효성 검사
    const validation = await validatePurchase(optionType);
    if (!validation.isValid) {
      showAlert(validation.message);
      return;
    }
    
    // 주문 페이지로 이동
    const productId = getProductIdFromPage();
    const orderUrl = `/order?productId=${productId}&optionType=${encodeURIComponent(optionType)}&quantity=1`;
    window.location.href = orderUrl;
    
  } catch (error) {
    console.error('구매하기 오류:', error);
    showAlert('오류가 발생했습니다. 다시 시도해주세요.');
  } finally {
    button.disabled = false;
  }
}

// 구매 유효성 검사
async function validatePurchase(optionType) {
  // 로그인 확인
  const isLoggedIn = await checkLoginStatus();
  if (!isLoggedIn) {
    return {
      isValid: false,
      message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?'
    };
  }
  
  // 판매 상태 체크
  const productStatus = getProductStatusFromPage();
  if (productStatus === '판매대기') {
    return {
      isValid: false,
      message: '현재 판매중단된 상품입니다.'
    };
  }
  
  // 옵션 체크
  if (!optionType) {
    return {
      isValid: false,
      message: '옵션을 선택해주세요.'
    };
  }
  
  return { isValid: true };
}

// 장바구니 API 호출
async function addToCartAPI(productId, optionType, quantity) {
  // CSRF 토큰
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };
  
  // CSRF 헤더 설정
  if (csrfToken && csrfHeader) {
    headers[csrfHeader] = csrfToken;
  }

  const response = await fetch('/cart/add', {
    method: 'POST',
    headers,
    credentials: 'include',
    body: JSON.stringify({
      productId: parseInt(productId),
      optionType,
      quantity: parseInt(quantity)
    })
  });
  
  const data = await response.json();
  
  // 응답 상태 확인
  if (!response.ok) {
    return data;
  }
  
  return data;
}

// 로그인 상태 확인
async function checkLoginStatus() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    return response.status !== 401;
  } catch (error) {
    return false;
  }
}

// 상품 ID 조회
function getProductIdFromPage() {
  return document.querySelector('[data-product-id]').getAttribute('data-product-id');
}

// 상품 상태 조회
function getProductStatusFromPage() {
  const statusElement = document.querySelector('[data-product-status]');
  return statusElement ? statusElement.getAttribute('data-product-status') : '판매중';
}

// 성공 모달 표시
function showSuccessModal(message) {
  const modal = document.createElement('div');
  modal.style.cssText = `
    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.5); display: flex; align-items: center;
    justify-content: center; z-index: 10000;
  `;
  
  modal.innerHTML = `
    <div style="
      background: white; 
      padding: 40px 50px; 
      border-radius: 12px; 
      text-align: center; 
      width: 450px;
      height: 250px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.15);
      display: flex;
      flex-direction: column;
      justify-content: center;
    ">
      <p style="
        margin: 0 0 30px 0; 
        font-size: 20px; 
        font-weight: 600;
        line-height: 1.5;
        color: #333;
      ">${message}</p>
      <div style="display: flex; gap: 12px; justify-content: center;">
        <button id="continue-shopping" style="
          width: 100%;
          height: 50px;
          border: 1px solid #BFBFBF;
          border-radius: 10px;
          background-color: #fff;
          font-size: 16px;
          font-weight: 500;
          cursor: pointer;
          color: #333;
          transition: all 0.2s ease;
        ">계속 쇼핑하기</button>
        <button id="go-to-cart" style="
          width: 100%;
          height: 50px;
          border: 1px solid #BFBFBF;
          border-radius: 10px;
          background-color: #0099AD;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          color: white;
          transition: all 0.2s ease;
        ">장바구니 확인하기</button>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  
  // 버튼 요소 선택
  const continueBtn = modal.querySelector('#continue-shopping');
  const cartBtn = modal.querySelector('#go-to-cart');
  
  // 장바구니 확인하기 버튼만 호버 효과 적용
  cartBtn.addEventListener('mouseenter', () => {
    cartBtn.style.backgroundColor = '#007a8a';
    cartBtn.style.transform = 'translateY(-1px)';
  });
  
  cartBtn.addEventListener('mouseleave', () => {
    cartBtn.style.backgroundColor = '#0099AD';
    cartBtn.style.transform = 'translateY(0)';
  });
  
  // 클릭 이벤트
  continueBtn.addEventListener('click', () => {
    document.body.removeChild(modal);
  });
  
  cartBtn.addEventListener('click', () => {
    window.location.href = '/cart';
  });
  
  // 배경 클릭시 닫기
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      document.body.removeChild(modal);
    }
  });
}

// 알림 표시
function showAlert(message) {
  if (message.includes('로그인이 필요합니다')) {
    if (confirm(message)) {
      window.location.href = '/login';
    }
  } else {
    alert(message);
  }
}

// 장바구니 개수 업데이트
async function updateCartCount() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      const badge = document.getElementById('cart-count');
      
      if (badge) {
        if (data.count > 0) {
          badge.textContent = data.count > 99 ? '99+' : data.count;
          badge.style.display = 'inline';
        } else {
          badge.style.display = 'none';
        }
      }
    }
  } catch (error) {

  }
}

// 중복 상품 체크
async function checkDuplicateProduct(productId, optionType) {
  try {
    const response = await fetch('/cart', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      // 동일한 상품ID와 옵션이 이미 장바구니에 있는지 확인
      // 판매 상품은 최대 1개만 구매 가능 (중복 불가)
      const existingItem = data.cartItems?.find(item => 
        item.productId == productId && item.optionType === optionType
      );
      
      return !!existingItem;
    }
    
    return false;
  } catch (error) {
    return false;
  }
}