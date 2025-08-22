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
    return token;
  }

  function getCsrfHeader() {
    const headerElement = document.querySelector('meta[name="_csrf_header"]');
    const header = headerElement ? headerElement.getAttribute('content') : null;
    return header;
  }

  const csrfToken = getCsrfToken();
  const csrfHeader = getCsrfHeader();

  // 3. 판매 상태 변경 시 서버에 PATCH 요청 보내기 (기존 코드 유지)
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
        // 상태 업데이트 성공 시 페이지를 새로고침하여 변경된 상태를 반영합니다.
        // 비동기 필터링 기능을 적용하려면, 이 부분도 동적으로 업데이트하도록 수정해야 합니다.
        window.location.reload();
      })
      .catch(error => {
        console.error('상태 업데이트 중 오류 발생:', error);
        alert('상태 업데이트 실패: ' + error.message);
      });
    });
  });

  // 4. 필터링용 Select Box 변경 시 비동기(AJAX) 요청 보내기
  const statusFilterSelect = document.querySelector('.overall-info .status-filter-select');
  const productListContainer = document.querySelector('.product-list');
  const overallInfoCount = document.querySelector('.overall-info > div');
  const paginationContainer = document.querySelector('.pagination');

  if (statusFilterSelect && productListContainer) {
    statusFilterSelect.addEventListener('change', function() {
      const selectedStatus = this.value;
      const currentPage = 1; // 필터 변경 시 1페이지로 초기화
      fetchFilteredProducts(selectedStatus, currentPage);
    });
  } else {
    console.warn('Filter elements not found. Check if .status-filter-select and .product-list exist.');
  }

  // 페이지네이션 클릭 이벤트 리스너
  if(paginationContainer) {
    paginationContainer.addEventListener('click', e => {
      e.preventDefault();
      const pageLink = e.target.closest('.page-link');
      if (pageLink && pageLink.href) {
        const url = new URL(pageLink.href);
        const page = url.searchParams.get('page');
        const status = new URL(window.location.href).searchParams.get('category') || 'all';
        fetchFilteredProducts(status, page);
      }
    });
  }

  // 필터링된 상품 목록을 가져오는 비동기 함수
  function fetchFilteredProducts(status, page) {
    const url = `/api/product/manage/filtered-data?status=${status}&page=${page}&size=5`;
    const headers = {
      'Accept': 'application/json'
    };

    fetch(url, { method: 'GET', headers: headers })
      .then(response => {
        if (!response.ok) {
          throw new Error('네트워크 응답이 올바르지 않습니다.');
        }
        return response.json();
      })
      .then(data => {
        // 서버로부터 받은 데이터로 화면 업데이트
        updateProductList(data.content);
        updateOverallCount(data.totalCount);
        updatePagination(data.pagination);

        // 브라우저 URL을 변경하여 뒤로가기/앞으로가기 버튼이 작동하도록 함
        const newUrl = `/product/manage?category=${status}&page=${page}&size=5`;
        history.pushState(null, '', newUrl);

      })
      .catch(error => {
        console.error('상품 목록을 불러오는 중 오류 발생:', error);
        alert('상품 목록을 불러오는 데 실패했습니다.');
      });
  }

  // DOM을 업데이트하는 함수
  function updateProductList(products) {
    productListContainer.innerHTML = ''; // 기존 목록을 비웁니다.
    if (products && products.length > 0) {
      products.forEach(form => {
        const productItemHtml = `
          <div class="product-item">
            <img src="${form.images && form.images.length > 0 ? form.images[0].base64ImageData : '/img/default-product.png'}"
                 alt="${form.images && form.images.length > 0 ? form.images[0].fileName : '기본 상품 이미지'}"/>
            <div class="product-info">
              <div class="product-info-left">
                <h2><a href="/product/view/${form.product.productId}">${form.product.title}</a></h2>
                <div class="product-info-left-detail">
                  <select class="status-select" data-product-id="${form.product.productId}"
                          value="${form.product.status}">
                    <option value="판매중" ${form.product.status === '판매중' ? 'selected' : ''}>판매중</option>
                    <option value="판매대기" ${form.product.status === '판매대기' ? 'selected' : ''}>판매대기</option>
                  </select>
                  <div class="product-info-left-detail-details">
                    <div>${form.product.updateDate && form.product.createDate !== form.product.updateDate ? form.product.updateDate : form.product.createDate}</div>
                    <div>조회수</div>
                    <div>좋아요</div>
                    <div>댓글수</div>
                  </div>
                </div>
                <span class="text-trim">${form.product.description.substring(0, maxLength) + (form.product.description.length > maxLength ? '...' : '')}</span>
              </div>
              <div class="product-info-right">
                <div class="product-info-right-detail">
                  <div>${form.salesRate}% 저렴</div>
                  <div class="price">
                    <h1>${form.product.salesPrice.toLocaleString()}</h1>
                    <div>원</div>
                  </div>
                  <div>정가 ${form.product.normalPrice.toLocaleString()}원</div>
                </div>
                <div class="btn-group">
                  <button class="btn-group-edit" onclick="location.href='/product/edit/${form.product.productId}'">수정</button>
                  <button class="btn-group-delete" onclick="if(confirm('해당 상품을 삭제하시겠습니까?')) location.href='/product/delete/${form.product.productId}'">삭제</button>
                </div>
              </div>
            </div>
          </div>
          <hr>
        `;
        productListContainer.insertAdjacentHTML('beforeend', productItemHtml);
      });
    } else {
      productListContainer.innerHTML = '<div style="text-align:center; padding: 20px;">등록된 상품이 없습니다.</div>';
    }
    // 동적으로 생성된 select에 다시 이벤트 리스너 등록
    bindStatusSelectListeners();
  }

  // 총 상품 수를 업데이트하는 함수
  function updateOverallCount(totalCount) {
      if (overallInfoCount) {
          overallInfoCount.textContent = `총 ${totalCount}개 상품`;
      }
  }

  // 페이지네이션 HTML을 업데이트하는 함수
  function updatePagination(paginationData) {
      if (!paginationContainer) return;
      paginationContainer.innerHTML = ''; // 기존 페이지네이션을 비웁니다.

      if (paginationData.totalCount > 0) {
          // 페이지네이션 HTML을 동적으로 생성
          let html = '';
          const baseUrl = `/product/manage?category=${paginationData.selectedStatus}&size=5`;

          // << 버튼
          if (paginationData.currentPage > 1) {
              html += `<a href="${baseUrl}&page=1" class="page-link">&lt;&lt;</a>`;
          } else {
              html += `<span class="page-link-spacer">&lt;&lt;</span>`;
          }

          // < 버튼
          if (paginationData.currentPage > 1) {
              const prevPageTarget = (paginationData.currentPage === paginationData.startPage && paginationData.startPage > 1) ? (paginationData.startPage - 1) : (paginationData.currentPage - 1);
              html += `<a href="${baseUrl}&page=${prevPageTarget}" class="page-link">&lt;</a>`;
          } else {
              html += `<span class="page-link-spacer">&lt;</span>`;
          }

          // 페이지 번호
          for (let i = paginationData.startPage; i <= paginationData.endPage; i++) {
              if (i === paginationData.currentPage) {
                  html += `<a href="${baseUrl}&page=${i}" class="page-link active">${i}</a>`;
              } else {
                  html += `<a href="${baseUrl}&page=${i}" class="page-link">${i}</a>`;
              }
          }

          // > 버튼
          if (paginationData.currentPage < paginationData.totalPages) {
              const nextPageTarget = (paginationData.currentPage === paginationData.endPage && paginationData.endPage < paginationData.totalPages) ? (paginationData.endPage + 1) : (paginationData.currentPage + 1);
              html += `<a href="${baseUrl}&page=${nextPageTarget}" class="page-link">&gt;</a>`;
          } else {
              html += `<span class="page-link-spacer">&gt;</span>`;
          }

          // >> 버튼
          if (paginationData.currentPage < paginationData.totalPages) {
              html += `<a href="${baseUrl}&page=${paginationData.totalPages}" class="page-link">&gt;&gt;</a>`;
          } else {
              html += `<span class="page-link-spacer">&gt;&gt;</span>`;
          }

          paginationContainer.innerHTML = html;
      }
  }

  // 상태 변경 select 박스에 이벤트 리스너를 다시 바인딩하는 함수
  function bindStatusSelectListeners() {
      const newStatusSelects = document.querySelectorAll('.status-select');
      newStatusSelects.forEach(select => {
          select.addEventListener('change', e => {
              const productId = e.target.dataset.productId;
              const status = e.target.value;
              const headers = { 'Content-Type': 'application/json' };
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
                  // 성공 시 현재 필터와 페이지를 유지하며 목록 새로고침
                  const currentStatus = new URL(window.location.href).searchParams.get('category') || 'all';
                  const currentPage = new URL(window.location.href).searchParams.get('page') || '1';
                  fetchFilteredProducts(currentStatus, currentPage);
              })
              .catch(error => {
                  console.error('상태 업데이트 중 오류 발생:', error);
                  alert('상태 업데이트 실패: ' + error.message);
              });
          });
      });
  }

  // 초기 로드 시 이벤트 리스너 바인딩
  bindStatusSelectListeners();
});