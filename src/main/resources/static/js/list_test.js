// 여행상품 목록 테스트 JavaScript (Thymeleaf 연동)

// 전역 변수
let currentProducts = [];
let filteredProducts = [];
let currentPage = 1;
let pageSize = 12;
let currentFilters = {
    search: '',
    location: '',
    priceRange: '',
    duration: ''
};
let currentSort = 'recommend';

// 테스트용 샘플 데이터
const sampleProducts = [
    {
        id: 1,
        title: "제주도 3박4일 패키지",
        price: 150000,
        location: "제주",
        duration: 4,
        rating: 4.5,
        imageUrl: "https://via.placeholder.com/300x200/87CEEB/000000?text=제주도",
        description: "제주도의 아름다운 자연을 만끽하는 3박4일 패키지"
    },
    {
        id: 2,
        title: "부산 해운대 2박3일",
        price: 80000,
        location: "부산",
        duration: 3,
        rating: 4.2,
        imageUrl: "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=부산",
        description: "부산 해운대에서 즐기는 2박3일 여행"
    },
    {
        id: 3,
        title: "서울 도심 투어 1박2일",
        price: 60000,
        location: "서울",
        duration: 2,
        rating: 4.0,
        imageUrl: "https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=서울",
        description: "서울의 주요 관광지를 둘러보는 1박2일 투어"
    },
    {
        id: 4,
        title: "강릉 커피 투어 2박3일",
        price: 95000,
        location: "강릉",
        duration: 3,
        rating: 4.7,
        imageUrl: "https://via.placeholder.com/300x200/45B7D1/FFFFFF?text=강릉",
        description: "강릉의 유명한 커피거리를 탐방하는 투어"
    },
    {
        id: 5,
        title: "경주 역사 문화 투어 3박4일",
        price: 120000,
        location: "경주",
        duration: 4,
        rating: 4.8,
        imageUrl: "https://via.placeholder.com/300x200/96CEB4/FFFFFF?text=경주",
        description: "경주의 역사와 문화를 체험하는 3박4일 투어"
    },
    {
        id: 6,
        title: "제주 올레길 트레킹 2박3일",
        price: 75000,
        location: "제주",
        duration: 3,
        rating: 4.3,
        imageUrl: "https://via.placeholder.com/300x200/87CEEB/000000?text=올레길",
        description: "제주 올레길을 걸으며 자연을 만끽하는 투어"
    },
    {
        id: 7,
        title: "부산 감천문화마을 투어 1박2일",
        price: 45000,
        location: "부산",
        duration: 2,
        rating: 4.1,
        imageUrl: "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=감천마을",
        description: "부산 감천문화마을의 아름다운 풍경을 감상하는 투어"
    },
    {
        id: 8,
        title: "서울 한강 유람선 투어",
        price: 35000,
        location: "서울",
        duration: 1,
        rating: 3.9,
        imageUrl: "https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=한강",
        description: "서울 한강에서 즐기는 유람선 투어"
    },
    {
        id: 9,
        title: "강릉 정동진 일출 투어 1박2일",
        price: 55000,
        location: "강릉",
        duration: 2,
        rating: 4.6,
        imageUrl: "https://via.placeholder.com/300x200/45B7D1/FFFFFF?text=정동진",
        description: "강릉 정동진에서 아름다운 일출을 감상하는 투어"
    },
    {
        id: 10,
        title: "경주 불국사 석굴암 투어 2박3일",
        price: 85000,
        location: "경주",
        duration: 3,
        rating: 4.4,
        imageUrl: "https://via.placeholder.com/300x200/96CEB4/FFFFFF?text=불국사",
        description: "경주 불국사와 석굴암을 둘러보는 문화 투어"
    },
    {
        id: 11,
        title: "제주 서귀포 해변 투어 1박2일",
        price: 65000,
        location: "제주",
        duration: 2,
        rating: 4.2,
        imageUrl: "https://via.placeholder.com/300x200/87CEEB/000000?text=서귀포",
        description: "제주 서귀포의 아름다운 해변을 즐기는 투어"
    },
    {
        id: 12,
        title: "부산 태종대 공원 투어",
        price: 25000,
        location: "부산",
        duration: 1,
        rating: 3.8,
        imageUrl: "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=태종대",
        description: "부산 태종대 공원에서 바다를 감상하는 투어"
    }
];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeProducts();
    logTest('페이지 로드 완료');
    
    // 서버 데이터가 있으면 서버 데이터 사용, 없으면 테스트 데이터 사용
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 사용 모드');
        // 서버에서 이미 렌더링된 상품들이 있으므로 추가 작업 불필요
    } else {
        logTest('테스트 데이터 사용 모드');
        initializeTestProducts();
    }
});

// 서버 데이터가 없을 때 테스트 상품 초기화
function initializeTestProducts() {
    const testProductList = document.getElementById('testProductList');
    if (testProductList) {
        currentProducts = [...sampleProducts];
        filteredProducts = [...currentProducts];
        
        // 서버 데이터가 없을 때만 JavaScript로 상품 표시
        displayTestProducts();
        updateTestPagination();
        logTest('테스트 상품 초기화 완료: ' + currentProducts.length + '개 상품');
    }
}

// 테스트용 상품 표시 (서버 데이터가 없을 때만 사용)
function displayTestProducts() {
    const testProductList = document.getElementById('testProductList');
    if (!testProductList) return;
    
    testProductList.innerHTML = '';
    
    // 페이지네이션 계산
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const productsToShow = filteredProducts.slice(startIndex, endIndex);
    
    if (productsToShow.length === 0) {
        testProductList.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #666;">검색 결과가 없습니다.</div>';
    } else {
        productsToShow.forEach(product => {
            const productCard = createProductCard(product);
            testProductList.appendChild(productCard);
        });
    }
    
    logTest('테스트 상품 표시 완료: ' + productsToShow.length + '개 상품');
}

// 상품 초기화 (기존 함수 유지)
function initializeProducts() {
    // 서버 데이터가 있으면 서버에서 처리하므로 여기서는 아무것도 하지 않음
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터가 이미 렌더링되어 있음');
        return;
    }
    
    // 테스트 모드일 때만 실행
    currentProducts = [...sampleProducts];
    filteredProducts = [...currentProducts];
    logTest('상품 초기화 완료: ' + currentProducts.length + '개 상품');
}

// 상품 표시 (기존 함수 유지)
function displayProducts() {
    // 서버 데이터가 있으면 서버에서 처리하므로 여기서는 아무것도 하지 않음
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터가 이미 렌더링되어 있음');
        return;
    }
    
    displayTestProducts();
}

// 상품 카드 생성
function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.onclick = () => viewProductDetail(product.id);
    
    card.innerHTML = `
        <div class="product-image">
            <img src="${product.imageUrl || 'https://via.placeholder.com/300x200'}" 
                 alt="${product.title}" 
                 style="width: 100%; height: 100%; object-fit: cover; border-radius: 5px;">
        </div>
        <div class="product-title">${product.title}</div>
        <div class="product-location">📍 ${product.location}</div>
        <div class="product-rating">⭐ ${product.rating} (${product.duration}박${product.duration+1}일)</div>
        <div class="product-price">${formatPrice(product.price)}원</div>
        <div style="font-size: 0.8em; color: #666; margin-top: 5px;">${product.description}</div>
    `;
    
    return card;
}

// 가격 포맷팅
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// 검색 기능 (서버 데이터가 없을 때만 사용)
function searchProducts() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 폼 제출로 처리
        document.getElementById('searchForm').submit();
        return;
    }
    
    const searchTerm = document.getElementById('searchInput').value.trim();
    currentFilters.search = searchTerm;
    
    applyAllFilters();
    logTest('검색 실행: "' + searchTerm + '"');
}

// 검색 초기화
function clearSearch() {
    document.getElementById('searchInput').value = '';
    currentFilters.search = '';
    
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 초기화 URL로 이동
        window.location.href = '/product/list';
    } else {
        applyAllFilters();
    }
    
    logTest('검색 초기화');
}

// 필터 적용 (서버 데이터가 없을 때만 사용)
function applyFilters() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 폼 제출로 처리
        document.getElementById('filterForm').submit();
        return;
    }
    
    currentFilters.location = document.getElementById('locationFilter').value;
    currentFilters.priceRange = document.getElementById('priceFilter').value;
    currentFilters.duration = document.getElementById('durationFilter').value;
    
    applyAllFilters();
    logTest('필터 적용: 지역=' + currentFilters.location + ', 가격=' + currentFilters.priceRange + ', 기간=' + currentFilters.duration);
}

// 필터 초기화
function clearFilters() {
    document.getElementById('locationFilter').value = '';
    document.getElementById('priceFilter').value = '';
    document.getElementById('durationFilter').value = '';
    
    currentFilters.location = '';
    currentFilters.priceRange = '';
    currentFilters.duration = '';
    
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 초기화 URL로 이동
        window.location.href = '/product/list';
    } else {
        applyAllFilters();
    }
    
    logTest('필터 초기화');
}

// 모든 필터 적용 (서버 데이터가 없을 때만 사용)
function applyAllFilters() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 모드에서는 클라이언트 필터링을 사용하지 않음');
        return;
    }
    
    filteredProducts = currentProducts.filter(product => {
        // 검색 필터
        if (currentFilters.search && !product.title.toLowerCase().includes(currentFilters.search.toLowerCase()) &&
            !product.location.toLowerCase().includes(currentFilters.search.toLowerCase()) &&
            !product.description.toLowerCase().includes(currentFilters.search.toLowerCase())) {
            return false;
        }
        
        // 지역 필터
        if (currentFilters.location && product.location !== currentFilters.location) {
            return false;
        }
        
        // 가격 필터
        if (currentFilters.priceRange) {
            const [min, max] = currentFilters.priceRange.split('-').map(Number);
            if (max && (product.price < min || product.price > max)) {
                return false;
            } else if (!max && product.price < min) {
                return false;
            }
        }
        
        // 기간 필터
        if (currentFilters.duration) {
            const [min, max] = currentFilters.duration.split('-').map(Number);
            if (max && (product.duration < min || product.duration > max)) {
                return false;
            } else if (!max && product.duration < min) {
                return false;
            }
        }
        
        return true;
    });
    
    currentPage = 1;
    displayTestProducts();
    updateTestPagination();
}

// 정렬 기능 (서버 데이터가 없을 때만 사용)
function sortProducts() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 폼 제출로 처리
        document.getElementById('sortForm').submit();
        return;
    }
    
    const sortType = document.getElementById('sortSelect').value;
    currentSort = sortType;
    
    filteredProducts.sort((a, b) => {
        switch (sortType) {
            case 'price-low':
                return a.price - b.price;
            case 'price-high':
                return b.price - a.price;
            case 'rating':
                return b.rating - a.rating;
            case 'newest':
                return b.id - a.id;
            case 'recommend':
            default:
                return b.rating - a.rating; // 평점 높은 순
        }
    });
    
    currentPage = 1;
    displayTestProducts();
    updateTestPagination();
    logTest('정렬 적용: ' + sortType);
}

// 페이지 크기 변경 (서버 데이터가 없을 때만 사용)
function changePageSize() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        // 서버 데이터가 있으면 폼 제출로 처리
        document.getElementById('sortForm').submit();
        return;
    }
    
    pageSize = parseInt(document.getElementById('pageSizeSelect').value);
    currentPage = 1;
    displayTestProducts();
    updateTestPagination();
    logTest('페이지 크기 변경: ' + pageSize + '개씩');
}

// 테스트용 페이지네이션 업데이트
function updateTestPagination() {
    const pagination = document.getElementById('pagination');
    const totalPages = Math.ceil(filteredProducts.length / pageSize);
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let paginationHTML = '';
    
    // 이전 버튼
    if (currentPage > 1) {
        paginationHTML += `<button onclick="goToPage(${currentPage - 1})">이전</button>`;
    }
    
    // 페이지 번호
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        const activeClass = i === currentPage ? 'active' : '';
        paginationHTML += `<button class="${activeClass}" onclick="goToPage(${i})">${i}</button>`;
    }
    
    // 다음 버튼
    if (currentPage < totalPages) {
        paginationHTML += `<button onclick="goToPage(${currentPage + 1})">다음</button>`;
    }
    
    pagination.innerHTML = paginationHTML;
}

// 페이지 이동 (서버 데이터가 없을 때만 사용)
function goToPage(page) {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 모드에서는 서버 페이지네이션을 사용함');
        return;
    }
    
    currentPage = page;
    displayTestProducts();
    updateTestPagination();
    logTest('페이지 이동: ' + page + '페이지');
}

// 상품 상세보기 (테스트용)
function viewProductDetail(productId) {
    const product = currentProducts.find(p => p.id === productId);
    if (product) {
        alert(`상품 상세보기 테스트\n\n상품명: ${product.title}\n가격: ${formatPrice(product.price)}원\n지역: ${product.location}\n평점: ${product.rating}\n기간: ${product.duration}박${product.duration+1}일\n\n이 기능은 실제 구현 시 상세 페이지로 이동합니다.`);
        logTest('상품 상세보기: ' + product.title);
    }
}

// 테스트 로그 기록
function logTest(message) {
    const testLog = document.getElementById('testLog');
    if (!testLog) return;
    
    const timestamp = new Date().toLocaleTimeString();
    const logEntry = document.createElement('div');
    logEntry.innerHTML = `<span style="color: #666;">[${timestamp}]</span> ${message}`;
    testLog.appendChild(logEntry);
    
    // 로그가 너무 많아지면 스크롤
    testLog.scrollTop = testLog.scrollHeight;
}

// 키보드 이벤트 처리
document.addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && event.target.id === 'searchInput') {
        searchProducts();
    }
});

// 성능 테스트
function runPerformanceTest() {
    const startTime = performance.now();
    
    // 대량의 데이터로 필터링 테스트
    const testProducts = Array.from({length: 1000}, (_, i) => ({
        id: i + 1,
        title: `테스트 상품 ${i + 1}`,
        price: Math.floor(Math.random() * 500000) + 10000,
        location: ['제주', '부산', '서울', '강릉', '경주'][Math.floor(Math.random() * 5)],
        duration: Math.floor(Math.random() * 10) + 1,
        rating: Math.random() * 2 + 3,
        imageUrl: "https://via.placeholder.com/300x200",
        description: `테스트 상품 ${i + 1}의 설명입니다.`
    }));
    
    const filtered = testProducts.filter(p => p.price < 100000);
    const endTime = performance.now();
    
    logTest(`성능 테스트 완료: 1000개 상품 필터링 - ${(endTime - startTime).toFixed(2)}ms`);
}

// 테스트 실행 버튼들
function addTestButtons() {
    const testResults = document.getElementById('testResults');
    if (!testResults) return;
    
    const testButtons = document.createElement('div');
    testButtons.innerHTML = `
        <div style="margin-top: 15px;">
            <button onclick="runPerformanceTest()">성능 테스트</button>
            <button onclick="testSearchFunction()">검색 기능 테스트</button>
            <button onclick="testFilterFunction()">필터 기능 테스트</button>
            <button onclick="testSortFunction()">정렬 기능 테스트</button>
            <button onclick="clearTestLog()">로그 초기화</button>
        </div>
    `;
    testResults.appendChild(testButtons);
}

// 검색 기능 테스트
function testSearchFunction() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 모드에서는 자동 테스트를 사용할 수 없습니다.');
        return;
    }
    
    logTest('=== 검색 기능 테스트 시작 ===');
    
    // 검색어 입력 테스트
    document.getElementById('searchInput').value = '제주';
    searchProducts();
    
    setTimeout(() => {
        document.getElementById('searchInput').value = '부산';
        searchProducts();
        
        setTimeout(() => {
            clearSearch();
            logTest('=== 검색 기능 테스트 완료 ===');
        }, 1000);
    }, 1000);
}

// 필터 기능 테스트
function testFilterFunction() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 모드에서는 자동 테스트를 사용할 수 없습니다.');
        return;
    }
    
    logTest('=== 필터 기능 테스트 시작 ===');
    
    // 지역 필터 테스트
    document.getElementById('locationFilter').value = '제주';
    applyFilters();
    
    setTimeout(() => {
        // 가격 필터 테스트
        document.getElementById('priceFilter').value = '0-50000';
        applyFilters();
        
        setTimeout(() => {
            clearFilters();
            logTest('=== 필터 기능 테스트 완료 ===');
        }, 1000);
    }, 1000);
}

// 정렬 기능 테스트
function testSortFunction() {
    if (typeof serverData !== 'undefined' && serverData.hasServerProducts) {
        logTest('서버 데이터 모드에서는 자동 테스트를 사용할 수 없습니다.');
        return;
    }
    
    logTest('=== 정렬 기능 테스트 시작 ===');
    
    const sortOptions = ['price-low', 'price-high', 'rating', 'newest'];
    let currentIndex = 0;
    
    function testNextSort() {
        if (currentIndex < sortOptions.length) {
            document.getElementById('sortSelect').value = sortOptions[currentIndex];
            sortProducts();
            currentIndex++;
            setTimeout(testNextSort, 1000);
        } else {
            document.getElementById('sortSelect').value = 'recommend';
            sortProducts();
            logTest('=== 정렬 기능 테스트 완료 ===');
        }
    }
    
    testNextSort();
}

// 로그 초기화
function clearTestLog() {
    const testLog = document.getElementById('testLog');
    if (testLog) {
        testLog.innerHTML = '';
        logTest('테스트 로그 초기화됨');
    }
}

// 페이지 로드 시 테스트 버튼 추가
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(addTestButtons, 1000);
});
