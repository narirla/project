document.addEventListener('DOMContentLoaded', function() {
  // 1. 상품 설명 글자 100자 제한 후 '...' 표시
  const maxLength = 100;
  const descriptionElements = document.querySelectorAll('.text-trim');

  descriptionElements.forEach(el => {
    if (el.textContent.length > maxLength) {
      el.textContent = el.textContent.substring(0, maxLength) + '...';
    }
  });

  // 2. CSRF 토큰과 헤더명을 meta 태그에서 읽는 함수
  function getCsrfToken() {
    const tokenElement = document.querySelector('meta[name="_csrf"]');
    const token = tokenElement ? tokenElement.getAttribute('content') : null;
    // console.log('CSRF token:', token); // 디버깅용이므로 실제 배포 시에는 제거하거나 주석 처리 권장
    return token;
  }

  function getCsrfHeader() {
    const headerElement = document.querySelector('meta[name="_csrf_header"]');
    const header = headerElement ? headerElement.getAttribute('content') : null;
    // console.log('CSRF header:', header); // 디버깅용이므로 실제 배포 시에는 제거하거나 주석 처리 권장
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

      const headers = {
        'Content-Type': 'application/json'
      };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      fetch(`/product/status/${productId}`, {
        method: 'PATCH',
        headers: headers,
        body: JSON.stringify({ status: status })
      })
      .then(response => {
        if (!response.ok) {
          return response.json().then(err => { throw new Error(err.message || '상태 업데이트 실패'); });
        }
        // 상태 업데이트 성공 시 추가적인 UI 처리 (예: 메시지 표시 등)
      })
      .catch(error => {
        console.error('상태 업데이트 중 오류 발생:', error);
        alert('상태 업데이트 실패: ' + error.message);
      });
    });
  });
  // 4. Select Box 변경 시 페이지네이션 1페이지로 초기화 기능
  const statusForm = document.getElementById('statusForm'); // HTML에서 form의 id가 'statusForm'이라고 가정
  const statusSelectElement = statusForm ? statusForm.querySelector('select[name="status"]') : null;
  const currentPageInput = statusForm ? statusForm.querySelector('input[name="page"]') : null;

  if (statusForm && statusSelectElement && currentPageInput) {
    statusSelectElement.addEventListener('change', function() {
      currentPageInput.value = 1; // 페이지 파라미터를 1로 설정
      statusForm.submit(); // 폼 제출
    });
  } else {
    console.warn('Status filtering form elements not found. Check if #statusForm, select[name="status"], and input[name="page"] exist.');
  }
});



