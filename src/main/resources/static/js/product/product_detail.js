document.addEventListener("DOMContentLoaded", function() {
  // 상품 이미지 슬라이더
  const slider = document.querySelector(".image-slider");
  const slides = document.querySelectorAll(".slide");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  let currentIndex = 0;

  const maxLength = 70;
  const descriptionElements = document.querySelectorAll('.text-trim');

  descriptionElements.forEach(el => {
    if (el.textContent.length > maxLength) {
      el.textContent = el.textContent.substring(0, maxLength) + '...';
    }
  });


  // 슬라이드 표시
    function showSlide(index) {
    currentIndex = index;
    slider.style.transform = `translateX(-${index * 100}%)`;
  }

  if (prevBtn) {
    prevBtn.addEventListener("click", () => {
      currentIndex = (currentIndex === 0) ? slides.length - 1 : currentIndex - 1;
      showSlide(currentIndex);
    });
  }

  if (nextBtn) {
    nextBtn.addEventListener("click", () => {
      currentIndex = (currentIndex === slides.length - 1) ? 0 : currentIndex + 1;
      showSlide(currentIndex);
    });
  }

  // 초기 위치
  if (slides.length > 0) {
    showSlide(0);
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
    } else if (result.header && result.header.rtcd === 'C11') {
      // 장바구니 중복 상품 에러 처리
      showAlert('이미 장바구니에 동일한 상품이 존재합니다.');
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
    
    // 1. 먼저 장바구니에 추가
    const productId = getProductIdFromPage();
    const result = await addToCartAPI(productId, optionType, 1);
    
    if (result.header && result.header.rtcd === 'S00') {
      // 2. 장바구니 추가 성공 시 해당 상품을 세션 스토리지에 저장
      try {
        const cartResponse = await fetch('/cart', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          credentials: 'include'
        });
        
        if (cartResponse.ok) {
          const cartData = await cartResponse.json();
          const cartItems = cartData.body?.cartItems || cartData.cartItems || [];
          
          // 방금 추가한 상품을 찾아서 세션 스토리지에 저장
          const addedItem = cartItems.find(item => 
            item.productId == productId && item.optionType === optionType
          );
          
          if (addedItem) {
            sessionStorage.setItem('selectedCartItems', JSON.stringify([addedItem]));
          }
        }
      } catch (error) {
        console.error('장바구니 데이터 조회 실패:', error);
      }
      
      // 3. 주문서 작성 페이지로 바로 이동
      window.location.href = '/order';
    } else {
      const message = result.header?.rtmsg || '상품 추가에 실패했습니다';
      showAlert(message);
    }
    
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
  
  // 옵션 체크 - 자동으로 기본 옵션 선택
  if (!optionType) {
    optionType = '기본코스'; // 기본 옵션으로 설정
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
  modal.className = 'modal-overlay';
  
  modal.innerHTML = `
    <div class="modal-content">
      <p class="modal-message">${message}</p>
      <div class="modal-buttons">
        <button id="continue-shopping" class="modal-btn secondary">계속 쇼핑하기</button>
        <button id="go-to-cart" class="modal-btn primary">장바구니 확인하기</button>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  
  // 버튼 요소 선택
  const continueBtn = modal.querySelector('#continue-shopping');
  const cartBtn = modal.querySelector('#go-to-cart');
  
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

// 장바구니 개수 업데이트 (전역 함수 사용)
function updateCartCount() {
  if (window.updateCartCount) {
    window.updateCartCount();
  }
}

