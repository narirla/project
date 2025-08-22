document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded. Initializing TourInfo page.');

    const dataListContainer = document.getElementById('data-list');
    const dataTitle = document.getElementById('data-title');
    const searchForm = document.getElementById('search-form');
    const searchTypeSelect = document.getElementById('search-type');
    const searchKeywordInput = document.getElementById('search-keyword');
    const paginationContainer = document.getElementById('pagination-container');

    let currentDataType = 'food';
    let currentEndpoint = '/api/food';
    let currentPage = 0;
    const pageSize = 10;

    // 로딩 메시지 함수
    function showLoading() {
        dataListContainer.innerHTML = '<p class="loading-message">데이터를 불러오는 중입니다...</p>';
        paginationContainer.innerHTML = '';
    }

    // 사이드 메뉴 클릭 이벤트 리스너
    document.querySelectorAll('.menu-link').forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();

            // 액티브 클래스 업데이트
            document.querySelectorAll('.menu-link').forEach(l => l.classList.remove('active'));
            this.classList.add('active');

            currentDataType = this.dataset.type;
            currentEndpoint = this.dataset.endpoint;
            currentPage = 0;

            dataTitle.textContent = this.textContent.replace(' 정보', '');
            searchKeywordInput.value = '';

            // 메뉴 클릭 시 검색 타입 옵션을 동적으로 변경
            updateSearchTypeOptions(currentDataType);
            fetchAndDisplayData(currentPage);
        });
    });

    // 검색 폼 제출 이벤트 리스너
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        currentPage = 0;
        const searchType = searchTypeSelect.value;
        const keyword = searchKeywordInput.value;
        fetchAndDisplayData(currentPage, searchType, keyword);
    });

    // 검색 타입 드롭다운 메뉴를 업데이트하는 함수
    function updateSearchTypeOptions(dataType) {
        searchTypeSelect.innerHTML = ''; // 기존 옵션 초기화
        let optionsHtml = '';
        if (dataType === 'food') {
            optionsHtml = `
                <option value="title">이름</option>
                <option value="rprsntvMenu">대표메뉴</option>
            `;
        } else if (dataType === 'facility') { // 이 부분을 'facility'로 수정합니다.
            optionsHtml = `
                <option value="subject">이름</option>
                <option value="setValueNm">분류</option>
            `;
        }
        searchTypeSelect.innerHTML = optionsHtml;
    }

    /**
     * API 호출 및 데이터 표시를 처리하는 메인 함수
     * @param {number} page 페이지 번호
     * @param {string} searchType 검색 필드 (선택 사항)
     * @param {string} keyword 검색어 (선택 사항)
     */
    function fetchAndDisplayData(page, searchType = '', keyword = '') {
        console.log(`Fetching data for type: ${currentDataType}, page: ${page}, searchType: ${searchType}, keyword: ${keyword}`);
        showLoading(); // 로딩 메시지 표시
        currentPage = page; // 현재 페이지를 전역 변수에 업데이트

        let apiUrl = currentEndpoint;
        const queryParams = new URLSearchParams({
            page: page,
            size: pageSize
        });

        if (keyword) {
            apiUrl = `${currentEndpoint}/search`;
            queryParams.append('searchType', searchType);
            queryParams.append('keyword', keyword);
        }

        fetch(`${apiUrl}?${queryParams.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.content && data.content.length > 0) {
                    renderDataList(data.content);
                    renderPaginationControls(data);
                } else {
                    dataListContainer.innerHTML = '<p class="no-results-message">검색 결과가 없습니다.</p>';
                    paginationContainer.innerHTML = '';
                }
            })
            .catch(error => {
                console.error('Error fetching data:', error);
                dataListContainer.innerHTML = `<p class="error-message">데이터를 불러오는 중 오류가 발생했습니다: ${error.message}</p>`;
            });
    }

    /**
     * 가져온 데이터를 카드 형식으로 렌더링하는 함수
     * @param {Array} items 데이터 항목 리스트
     */
    function renderDataList(items) {
        let html = '';

        items.forEach(item => {
            if (currentDataType === 'food') {
                // 이미지 프록시 로직 적용
                // item.mainImgThumb에 URL이 없으면 null이 됩니다.
                const proxyUrl = item.mainImgThumb ? `/api/image-proxy?url=${encodeURIComponent(item.mainImgThumb)}` : null;
                // proxyUrl이 null이면 No Image 플레이스홀더를 사용합니다.
                const imageUrl = proxyUrl || 'https://placehold.co/400x300/e9e9e9/333?text=No Image';

                const homepage = item.homepageUrl ? `<a href="${item.homepageUrl}" target="_blank">${item.homepageUrl}</a>` : '없음';
                const subtitle = item.subtitle ? `(${item.subtitle})` : '';

                html += `
                    <div class="data-card food-card">
                        <div class="card-image-container">
                            <img src="${imageUrl}" alt="${item.title || '정보 없음'}" class="card-image" onerror="this.onerror=null;this.src='https://placehold.co/400x300/e9e9e9/333?text=No Image';">
                        </div>
                        <div class="card-content">
                            <h3 class="card-title">${item.title || '정보 없음'} ${subtitle}</h3>
                            <p><strong>주소:</strong> ${item.addr1 || '정보 없음'}</p>
                            <p><strong>전화번호:</strong> ${item.cntctTel || '정보 없음'}</p>
                            <p><strong>영업시간:</strong> ${item.usageDayWeekAndTime || '정보 없음'}</p>
                            <p><strong>대표메뉴:</strong> ${item.rprsntvMenu || '정보 없음'}</p>
                            <p><strong>홈페이지:</strong> ${homepage}</p>
                            <p class="description"><strong>설명:</strong> ${item.itemcntnts || '정보 없음'}</p>
                        </div>
                    </div>
                `;
            } else if (currentDataType === 'facility') {
                const proxyUrl = item.imgUrl ? `/api/image-proxy?url=${encodeURIComponent(item.imgUrl)}` : null;
                const imageUrl = proxyUrl || 'https://placehold.co/400x300/e9e9e9/333?text=No Image';
                const gubun = (item.gubun && item.gubun.length > 0) ? item.gubun.join(', ') : '정보 없음';
                const setValueNm = (item.setValueNm && item.setValueNm.length > 0) ? item.setValueNm.join(', ') : '정보 없음';
                const mainMenu = (item.mainMenu && item.mainMenu.length > 0) ? item.mainMenu.join(', ') : '정보 없음';

                html += `
                    <div class="data-card facilities-card">
                        <div class="card-image-container">
                            <img src="${imageUrl}" alt="${item.subject || '정보 없음'}" class="card-image" onerror="this.onerror=null;this.src='https://placehold.co/400x300/e9e9e9/333?text=No Image';">
                        </div>
                        <div class="card-content">
                            <h3 class="card-title">${item.subject || '정보 없음'}</h3>
                            <p><strong>주소:</strong> ${item.addr || '정보 없음'}</p>
                            <p><strong>전화번호:</strong> ${item.tel || '정보 없음'}</p>
                            <p><strong>분류:</strong> ${setValueNm}</p>
                            <p><strong>구분:</strong> ${gubun}</p>
                            <p><strong>주메뉴:</strong> ${mainMenu}</p>
                            <p><strong>테이블 수:</strong> ${item.tableCount || '정보 없음'}</p>
                        </div>
                    </div>
                `;
            }
        });

        dataListContainer.innerHTML = html;
    }

    /**
     * 페이지네이션 컨트롤을 렌더링하는 함수
     * @param {Object} pageInfo 페이지 정보가 담긴 객체
     */
    function renderPaginationControls(pageInfo) {
        paginationContainer.innerHTML = '';
        const totalPages = pageInfo.totalPages;
        const currentPage = pageInfo.number;
        const currentKeyword = searchKeywordInput.value;
        const currentSearchType = searchTypeSelect.value;

        if (totalPages > 1) {
            const startPage = Math.floor(currentPage / 10) * 10;
            const endPage = Math.min(startPage + 9, totalPages - 1);

            // "이전" 버튼 추가
            if (currentPage > 0) {
                const prevPageNumber = (currentPage === startPage) ? Math.max(0, startPage - 1) : currentPage - 1;
                const prevButton = createButton('이전', () => fetchAndDisplayData(prevPageNumber, currentSearchType, currentKeyword));
                paginationContainer.appendChild(prevButton);
            }

            // 페이지 번호 버튼 추가
            for (let i = startPage; i <= endPage; i++) {
                const pageButton = createButton(i + 1, () => fetchAndDisplayData(i, currentSearchType, currentKeyword));
                if (i === currentPage) {
                    pageButton.classList.add('active');
                }
                paginationContainer.appendChild(pageButton);
            }

            // "다음" 버튼 추가
            if (currentPage < totalPages - 1) {
                const nextPageNumber = (currentPage === endPage) ? endPage + 1 : currentPage + 1;
                const nextButton = createButton('다음', () => fetchAndDisplayData(nextPageNumber, currentSearchType, currentKeyword));
                paginationContainer.appendChild(nextButton);
            }
        }
    }

    /**
     * 페이지네이션 버튼을 생성하는 헬퍼 함수
     * @param {string} text 버튼에 표시할 텍스트
     * @param {function} clickHandler 클릭 이벤트 핸들러
     * @return {HTMLButtonElement} 생성된 버튼 요소
     */
    function createButton(text, clickHandler) {
        const button = document.createElement('button');
        button.textContent = text;
        button.classList.add('pagination-button');
        button.addEventListener('click', clickHandler);
        return button;
    }

    // 초기 페이지 로딩 시 기본 데이터(맛집)를 가져옵니다.
    fetchAndDisplayData(currentPage);
    // 초기 검색 타입 옵션을 설정합니다.
    updateSearchTypeOptions(currentDataType);
});