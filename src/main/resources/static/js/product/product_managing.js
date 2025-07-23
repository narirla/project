document.addEventListener('DOMContentLoaded', function() {
  // 1. 상품 설명 글자 100자 제한 후 '...' 표시
  const maxLength = 100;
  const descriptionElements = document.querySelectorAll('.text-trim');

  descriptionElements.forEach(el => {
    if (el.textContent.length > maxLength) {
      el.textContent = el.textContent.substring(0, maxLength) + '...';
    }
  });

  // 2. CSRF 토큰과 헤더명을 meta 태그에서 읽는 함수 (console.log 포함)
  function getCsrfToken() {
    const tokenElement = document.querySelector('meta[name="_csrf"]');
    const token = tokenElement ? tokenElement.getAttribute('content') : null;
    console.log('CSRF token:', token);
    return token;
  }

  function getCsrfHeader() {
    const headerElement = document.querySelector('meta[name="_csrf_header"]');
    const header = headerElement ? headerElement.getAttribute('content') : null;
    console.log('CSRF header:', header);
    return header;
  }

  const csrfToken = getCsrfToken();
  const csrfHeader = getCsrfHeader();

  // 3. 판매 상태 변경 시 서버에 PATCH 요청 보내기
  const statusSelects = document.querySelectorAll('.status-select');

  statusSelects.forEach(select => {
    select.addEventListener('change', e => {
      const productId = e.target.dataset.productId;
      const status = e.target.value;

      // fetch 요청 헤더 생성: Content-Type은 기본, CSRF 토큰은 있을 때만 추가
      const headers = {
        'Content-Type': 'application/json'
      };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      fetch(`/product/status/${productId}`, {
        method: 'PATCH',
        headers: headers,
        body: JSON.stringify({ status: status })  // JSON 바디로 상태 전송
      })
      .then(response => {
        if (!response.ok) {
          alert('상태 업데이트 실패');
        }
      })
      .catch(error => {
        console.error('상태 업데이트 중 오류 발생:', error);
        alert('상태 업데이트 실패');
      });
    });
  });

});

// 수정 버튼 클릭 함수
function editProduct(button) {
  const productId = button.dataset.productId;
  location.href = `/product/edit/${productId}`;
}

// 삭제 버튼 클릭 함수  
function deleteProduct(button) {
  const productId = button.dataset.productId;
  if (confirm('정말로 이 상품을 삭제하시겠습니까?')) {
    location.href = `/product/delete/${productId}`;
  }
}