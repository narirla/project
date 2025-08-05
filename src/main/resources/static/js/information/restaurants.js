
// 전역 변수
let map;
let markers = [];
const elements = {}; // DOM 요소들을 담는 객체

document.addEventListener('DOMContentLoaded', function() {
    cacheElements(); // 1. DOM 요소 미리 찾기
    setupEventListeners(); // 2. 버튼 클릭 등 이벤트 설정

    const dataToUse = getDataToUse(); // 3. 사용할 데이터 결정
    if (hasValidData(dataToUse)) {
        initializeDashboard(dataToUse); // 4. 지도와 차트 생성
    }
});

// DOM 요소 캐싱
function cacheElements() {
    elements.searchInput = document.getElementById('searchInput');
    elements.filterBtn = document.getElementById('filterBtn');
    elements.districtFilter = document.getElementById('districtFilter');
    elements.categoryFilter = document.getElementById('categoryFilter');
    elements.restaurantGrid = document.getElementById('restaurantGrid');
}

// 사용할 데이터 결정 (필터링된 데이터 우선)
function getDataToUse() {
    const hasFilteredData = typeof filteredRestaurants !== 'undefined' &&
                           hasValidData(filteredRestaurants);
    return hasFilteredData ? filteredRestaurants : allRestaurants;
}

// 데이터 유효성 검사
function hasValidData(data) {
    return data && Array.isArray(data) && data.length > 0;
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 검색창 엔터키 검색
    if (elements.searchInput) {
        elements.searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                applyFilters();
            }
        });
    }

    // 필터 버튼 검색
    if (elements.filterBtn) {
        elements.filterBtn.addEventListener('click', applyFilters);
    }

    // 구군 필터 변경
    if (elements.districtFilter) {
        elements.districtFilter.addEventListener('change', applyFilters);
    }

    // 카테고리 필터 변경
    if (elements.categoryFilter) {
        elements.categoryFilter.addEventListener('change', applyFilters);
    }

    // 자동완성 기능
    setupAutocomplete();

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeRestaurantModal();
        }
    });
}

// 필터 적용 및 페이지 이동
function applyFilters() {
    const search = elements.searchInput ? elements.searchInput.value.trim() : '';
    const district = elements.districtFilter ? elements.districtFilter.value : '';
    const cuisineType = elements.cuisineTypeFilter ? elements.cuisineTypeFilter.value : '';

    // URL 파라미터 구성
    const params = new URLSearchParams();
    params.set('page', '1');
    params.set('size', '15');
    if (search) params.set('search', search);
    if (district) params.set('district', district);
    if (cuisineType) params.set('category', category);

    window.location.href = `/information?${params.toString()}`;
}

// 대시보드 초기화 (지도 + 차트)
function initializeDashboard(restaurantData = allRestaurants) {
    initializeMapWhenReady(restaurantData);
    initializeCharts(restaurantData);
}

// 카카오 지도 API 로딩 대기 후 초기화
function initializeMapWhenReady(restaurantData = allRestaurants) {
    if (typeof kakao !== 'undefined' && kakao.maps) {
        initializeMap();
        loadMapData(restaurantData);
    } else {
        // 1초마다 체크
        const checkKakao = setInterval(() => {
            if (typeof kakao !== 'undefined' && kakao.maps) {
                clearInterval(checkKakao);
                initializeMap();
                loadMapData(restaurantData);
            }
        }, 1000);

        // 10초 후 타임아웃
        setTimeout(() => {
            clearInterval(checkKakao);
        }, 10000);
    }
}

// 카카오 지도 초기화
function initializeMap() {
    try {
        const mapContainer = document.getElementById('map');
        if (!mapContainer) {
            return;
        }

        const mapOption = {
            center: new kakao.maps.LatLng(35.1796, 129.0756), // 부산 중심좌표
            level: 8 // 확대 정도
        };

        map = new kakao.maps.Map(mapContainer, mapOption); // 지도 생성

    } catch (error) {
        console.error('지도 초기화 실패:', error);
    }
}

// 지도 데이터 로드 및 마커 생성
function loadMapData(restaurantData = allRestaurants) {
    // 1. 좌표가 유효한 맛집만 필터링
    const mapRestaurants = restaurantData.filter(restaurant => {
        const lat = restaurant.LAT;
        const lng = restaurant.LNG;

        // 좌표가 유효한지 확인
        return lat !== null && lat !== undefined &&
                       !isNaN(parseFloat(lat)) && !isNaN(parseFloat(lng));
    });

    // 2. 마커 생성
    addMarkersToMap(mapRestaurants);

    // 3. 검색 결과면 지도 범위 조정
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('search') || urlParams.get('district') || urlParams.get('cuisineType')) {
        // 검색한 경우 → 모든 결과가 보이도록 지도 범위 조정
        setTimeout(() => {
            if (mapRestaurants.length > 1) {
                const bounds = new kakao.maps.LatLngBounds();
                mapRestaurants.forEach(restaurant => {
                    bounds.extend(new kakao.maps.LatLng(restaurant.LAT, restaurant.LNG));
                });
                map.setBounds(bounds, 50, 50, 50, 50); // 여백 50px
            }
        }, 300);
    }
}

// 지도에 마커 추가
function addMarkersToMap(restaurants) {
    // 기존 마커 제거
    clearMarkers();

    restaurants.forEach(restaurant => {
        const lat = parseFloat(restaurant.LAT);
        const lng = parseFloat(restaurant.LNG);

        if (isNaN(lat) || isNaN(lng)) return;

        try {
            const marker = createMarker(restaurant, lat, lng);
            markers.push(marker);
        } catch (error) {
            console.error('마커 생성 실패:', error);
        }
    });
}

// 기존 마커 제거
function clearMarkers() {
    markers.forEach(marker => marker.setMap(null));
    markers = [];
}

// 마커 생성
function createMarker(restaurant, lat, lng) {
    const markerPosition = new kakao.maps.LatLng(lat, lng);
    const marker = new kakao.maps.Marker({
        position: markerPosition,
        title: restaurant.MAIN_TITLE || '맛집'
    });

    marker.setMap(map);

    // 마커 클릭 이벤트
    kakao.maps.event.addListener(marker, 'click', function() {
        showRestaurantModal(restaurant.UC_SEQ);
    });

    return marker;
}

// 맛집 상세 모달 표시
function showRestaurantModal(restaurantId) {
    const restaurant = allRestaurants.find(r => r.UC_SEQ == restaurantId);
    if (!restaurant) return;

    closeRestaurantModal();

    // 이미지 URL이 null이거나 비어있으면 대체 이미지 경로를 사용
    const imageUrl = restaurant.MAIN_IMG_THUMB || '/img/default-restaurant.png';
    const modalHtml = createModalHtml(restaurant, imageUrl);
    document.body.insertAdjacentHTML('beforeend', modalHtml);
}

// 모달 HTML 생성
function createModalHtml(restaurant, imageUrl) {
    const name = restaurant.MAIN_TITLE || '맛집';
    const address = restaurant.ADDR1 || '주소 정보 없음';
    const menu = restaurant.RPRSNTV_MENU;
    const content = restaurant.ITEMCNTNTS;

    return `
        <div id="restaurant-detail-modal" class="modal-overlay" style="animation: fadeIn 0.3s ease;">
            <div class="restaurant-card modal-content" style="animation: slideUp 0.3s ease;">
                <div class="restaurant-image">
                    <img src="${imageUrl}" alt="${name}">
                    <button onclick="closeRestaurantModal()" class="modal-close-btn">×</button>
                </div>

                <div class="restaurant-info">
                    <h3 class="restaurant-name">${name}</h3>
                    <p class="restaurant-address">${address}</p>

                    ${menu ? `
                        <div class="modal-info-section">
                            <span class="modal-info-label">대표메뉴:</span>
                            <span class="modal-info-value">${menu}</span>
                        </div>
                    ` : ''}

                    ${content ? `
                        <div class="modal-info-section">
                            <span class="modal-info-label">상세정보:</span>
                            <p style="margin: 4px 0 0 0; color: #666; font-size: 13px; line-height: 1.4;">
                                ${sanitizeHtml(content).substring(0, 200)}${content.length > 200 ? '...' : ''}
                            </p>
                        </div>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

// HTML 태그 제거
function sanitizeHtml(html) {
    return html.replace(/<[^>]*>/g, '');
}

// 모달 닫기
function closeRestaurantModal() {
    const modal = document.getElementById('restaurant-detail-modal');
    if (modal) {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    }
}

// 차트 초기화
function initializeCharts(restaurantData = allRestaurants) {
    if (!hasValidData(restaurantData)) return;
    if (typeof Chart === 'undefined') return;

    // 데이터 집계
    const districtStats = aggregateByDistrict(restaurantData);
    const categoryStats = aggregateByCategory(restaurantData);

    // 차트 생성
    if (Object.keys(districtStats).length > 0) {
        createDistrictChart(districtStats);
    }

    if (Object.keys(categoryStats).length > 0) {
        createCategoryChart(categoryStats);
    }
}

// 구군별 데이터 집계
function aggregateByDistrict(restaurants) {
    const stats = {};

    restaurants.forEach(restaurant => {
        const district = restaurant.GUGUN_NM;
        if (district && district.trim()) {
            stats[district] = (stats[district] || 0) + 1;
        }
    });

    // 상위 8개만 반환
    const sortedEntries = Object.entries(stats)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 8);

    const topStats = {};
    sortedEntries.forEach(([district, count]) => {
        topStats[district] = count;
    });

    return topStats;
}

// 카테고리별 데이터 집계
function aggregateByCategory(restaurants) {
    const stats = {};

    restaurants.forEach(restaurant => {
        const category = getCategoryFromServer(restaurant);
        if (category && category !== '기타') {
            stats[category] = (stats[category] || 0) + 1;
        }
    });

    return stats;
}

// 카테고리 분류 로직
function getCategoryFromServer(restaurant) {
    const itemCntnts = restaurant.ITEMCNTNTS;
    const mainTitle = restaurant.MAIN_TITLE;
    const rprsnTvMenu = restaurant.RPRSNTV_MENU;

    const allText = [itemCntnts, mainTitle, rprsnTvMenu]
        .filter(text => text && text.trim())
        .join(' ');

    if (!allText) return '기타';

    const cleaned = allText.replace(/<[^>]*>/g, '').toLowerCase().trim();

    if (cleaned.includes('한식') || cleaned.includes('갈비') || cleaned.includes('국') || cleaned.includes('탕')) return '한식';
    if (cleaned.includes('중식') || cleaned.includes('짜장') || cleaned.includes('짬뽕')) return '중식';
    if (cleaned.includes('일식') || cleaned.includes('초밥') || cleaned.includes('라멘') || cleaned.includes('우동')) return '일식';
    if (cleaned.includes('양식') || cleaned.includes('파스타') || cleaned.includes('피자') || cleaned.includes('스테이크')) return '양식';
    if (cleaned.includes('카페') || cleaned.includes('커피') || cleaned.includes('디저트')) return '카페';
    if (cleaned.includes('치킨') || cleaned.includes('호프') || cleaned.includes('맥주')) return '치킨/호프';
    if (cleaned.includes('해산물') || cleaned.includes('회') || cleaned.includes('생선')) return '해산물';
    if (cleaned.includes('분식') || cleaned.includes('떡볶이') || cleaned.includes('김밥')) return '분식';

    return '기타';
}

// 구군별 도넛 차트 생성
function createDistrictChart(districtStats) {
    const ctx = document.getElementById('districtChart');
    if (!ctx) return;

    const labels = Object.keys(districtStats);
    const data = Object.values(districtStats);

    if (labels.length === 0) return;

    destroyExistingChart(ctx);

    try {
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',
                        '#9966FF', '#FF9F40', '#1EC4CC', '#C9CBCF'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: ${value}개 (${percentage}%)`;
                            }
                        }
                    }
                },
                animation: {
                    duration: 1000
                }
            }
        });
    } catch (error) {
        console.error('구군별 차트 생성 실패:', error);
    }
}

// 카테고리별 막대 차트 생성
function createCategoryChart(categoryStats) {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;

    const sortedEntries = Object.entries(categoryStats)
        .sort(([,a], [,b]) => b - a);

    const labels = sortedEntries.map(([category]) => category);
    const data = sortedEntries.map(([,count]) => count);

    if (labels.length === 0) return;

    destroyExistingChart(ctx);

    try {
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: '맛집 수',
                    data: data,
                    backgroundColor: 'rgba(30, 196, 204, 0.8)',
                    borderColor: '#1EC4CC',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('카테고리별 차트 생성 실패:', error);
    }
}

// 기존 차트 제거
function destroyExistingChart(ctx) {
    const existingChart = Chart.getChart(ctx);
    if (existingChart) {
        existingChart.destroy();
    }
}

// 자동완성 기능 설정
function setupAutocomplete() {
    let timeoutId;
    const cache = new Map(); // 이전 검색 결과 저장소

    elements.searchInput.addEventListener('input', function() {
        const query = this.value.trim(); // 입력한 글자

        if (query.length < 2) {
            hideAutocomplete(); // 2글자 미만이면 숨기기
            return;
        }

        // 캐시 확인
        if (cache.has(query)) {
            showAutocomplete(cache.get(query));
            return;
        }

        // API 요청
        timeoutId = setTimeout(async () => {
            try {
                const response = await fetch(`/api/search/autocomplete?query=${encodeURIComponent(query)}`);
                if (response.ok) {
                    const suggestions = await response.json();

                    // 캐시 저장 (최대 50개)
                    if (cache.size >= 50) {
                        const firstKey = cache.keys().next().value;
                        cache.delete(firstKey);
                    }
                    cache.set(query, suggestions);

                    showAutocomplete(suggestions);
                }
            } catch (error) {
                console.error('자동완성 요청 실패:', error);
            }
        }, 50);
    });

    elements.searchInput.addEventListener('blur', function() {
        setTimeout(hideAutocomplete, 200);
    });
}

// 자동완성 결과 표시
function showAutocomplete(suggestions) {
    if (!elements.searchInput || !suggestions.length) return;

    hideAutocomplete();

    const autocompleteContainer = createAutocompleteContainer();

    suggestions.forEach(suggestion => {
        const item = createAutocompleteItem(suggestion);
        autocompleteContainer.appendChild(item);
    });

    const parent = elements.searchInput.parentElement;
    parent.style.position = 'relative';
    parent.appendChild(autocompleteContainer);
}

// 자동완성 컨테이너 생성
function createAutocompleteContainer() {
    const container = document.createElement('div');
    container.id = 'autocomplete-container';
    container.className = 'autocomplete-container';
    return container;
}

// 자동완성시 지도 확대 기능
function createAutocompleteItem(suggestion) {
    const item = document.createElement('div');
    item.textContent = suggestion;
    item.className = 'autocomplete-item';

    item.addEventListener('click', function() {
        elements.searchInput.value = suggestion; // 검색창에 입력
        hideAutocomplete(); // 자동완성 숨기기

        // 해당 맛집으로 지도 이동
        const restaurant = allRestaurants.find(r =>
            r.MAIN_TITLE && r.MAIN_TITLE.includes(suggestion)
        );
        if (restaurant && restaurant.LAT && restaurant.LNG && map) {
            const position = new kakao.maps.LatLng(restaurant.LAT, restaurant.LNG);
            map.panTo(position);
            map.setLevel(3);
        }

        setTimeout(() => applyFilters(), 500); // 0.5초 후 검색 페이지로
    });

    return item;
}

// 자동완성 숨기기
function hideAutocomplete() {
    const container = document.getElementById('autocomplete-container');
    if (container) {
        container.remove();
    }



}