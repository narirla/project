// 전역 변수
let map;
let marker;
let infowindow;

// 필수 데이터 검증
function validateRestaurantData() {
    console.log('=== 데이터 검증 시작 ===');
    console.log('restaurant 객체 존재:', !!restaurant);

    if (!restaurant) {
        console.error('restaurant 객체가 없습니다.');
        return false;
    }

    // 제목 확인 (대문자/소문자 모두 지원)
    const title = restaurant.mainTitle || restaurant.MAIN_TITLE;
    console.log('제목 확인:', title);

    if (!title) {
        console.error('맛집 제목이 없습니다.');
        return false;
    }

    // 좌표 확인 (대문자/소문자 모두 지원)
    const lat = restaurant.lat || restaurant.LAT;
    const lng = restaurant.lng || restaurant.LNG;
    console.log('좌표 확인:', { lat, lng });

    if (!lat || !lng) {
        console.error('좌표 정보가 없습니다.');
        return false;
    }

    console.log('=== 데이터 검증 통과 ===');
    return true;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    if (!validateRestaurantData()) {
        showToast('맛집 정보를 불러오는 중 오류가 발생했습니다.');
        return;
    }

    initializeComponents();
    setupEventListeners();
    increaseViewCount();
    setTimeout(checkFavoriteStatus, 100);
});

/**
 * 컴포넌트 초기화
 */
function initializeComponents() {
    initializeMap();
    setupImageHandlers();
    setupModalHandlers();
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeAllModals();
        }
    });

    // 브라우저 뒤로가기 시 모달 닫기
    window.addEventListener('popstate', function() {
        closeAllModals();
    });

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        const shareModal = document.getElementById('shareModal');
        if (event.target === shareModal) {
            closeShareModal();
        }
    });
}

/**
 * 조회수 증가 기능
 */
function increaseViewCount() {
    const restaurantId = restaurant.ucSeq || restaurant.UC_SEQ || restaurant.id;
    if (!restaurantId) return;

    try {
        const viewCountKey = `restaurant_view_${restaurantId}`;
        const lastViewTime = localStorage.getItem(viewCountKey);
        const currentTime = new Date().getTime();

        // 24시간 중복 방지
        if (!lastViewTime || (currentTime - parseInt(lastViewTime)) > 86400000) {
            localStorage.setItem(viewCountKey, currentTime.toString());
        }
    } catch (error) {
        console.log('조회수 증가 실패:', error);
    }
}

/**
 * 카카오맵 초기화
 */
function initializeMap() {
    console.log('=== 지도 초기화 시작 ===');
    console.log('Restaurant 객체:', restaurant);

    // 서버에서 전달되는 데이터 구조에 맞게 수정 (camelCase)
    const lat = restaurant.lat || restaurant.LAT;
    const lng = restaurant.lng || restaurant.LNG;

    console.log('좌표 확인:');
    console.log('- lat:', lat, typeof lat);
    console.log('- lng:', lng, typeof lng);

    if (!restaurant) {
        console.error('restaurant 객체가 없습니다.');
        showMapError('맛집 정보를 불러올 수 없습니다.');
        return;
    }

    if (!lat || !lng) {
        console.error('좌표 정보가 없습니다. lat:', lat, 'lng:', lng);
        // restaurant 객체의 모든 속성을 확인
        console.log('restaurant 객체의 모든 키:', Object.keys(restaurant));
        showMapError('위치 정보를 불러올 수 없습니다.');
        return;
    }

    if (typeof kakao === 'undefined' || !kakao.maps) {
        console.log('카카오맵 API 로딩 대기 중...');
        setTimeout(initializeMap, 1000);
        return;
    }

    try {
        const mapContainer = document.getElementById('restaurantMap');
        if (!mapContainer) {
            console.error('지도 컨테이너를 찾을 수 없습니다.');
            return;
        }

        const latitude = parseFloat(lat);
        const longitude = parseFloat(lng);

        console.log('파싱된 좌표:', latitude, longitude);

        if (isNaN(latitude) || isNaN(longitude)) {
            console.error('좌표 파싱 실패:', { latitude, longitude });
            showMapError('잘못된 위치 정보입니다.');
            return;
        }

        const mapOption = {
            center: new kakao.maps.LatLng(latitude, longitude),
            level: 3,
            draggable: true,
            scrollwheel: true,
            disableDoubleClick: false
        };

        console.log('지도 생성 시작...');
        map = new kakao.maps.Map(mapContainer, mapOption);
        console.log('지도 생성 완료');

        createMarker(latitude, longitude);
        addMapControls();

        console.log('=== 지도 초기화 완료 ===');

    } catch (error) {
        console.error('지도 초기화 오류:', error);
        showMapError('지도를 불러오는 중 오류가 발생했습니다.');
    }
}

/**
 * 마커 생성
 */
function createMarker(lat, lng) {
    const markerPosition = new kakao.maps.LatLng(lat, lng);

    marker = new kakao.maps.Marker({
        position: markerPosition,
        title: restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집',
        clickable: true
    });

    marker.setMap(map);

    const infoContent = createInfoWindowContent();
    infowindow = new kakao.maps.InfoWindow({
        content: infoContent,
        removable: true
    });

    kakao.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map, marker);
    });

    setTimeout(() => {
        infowindow.open(map, marker);
    }, 500);
}

/**
 * 인포윈도우 내용 생성
 */
function createInfoWindowContent() {
    const title = restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집';
    const address = restaurant.addr1 || restaurant.ADDR1 || '';

    return `
        <div style="width: 250px; padding: 16px; font-family: 'Pretendard', sans-serif;">
            <h3 style="margin: 0 0 8px 0; font-size: 16px; font-weight: 600; color: #333; line-height: 1.3;">
                ${title}
            </h3>
            <p style="margin: 0; font-size: 13px; color: #666; line-height: 1.4;">
                ${address}
            </p>
        </div>
    `;
}

/**
 * 지도 컨트롤 추가
 */
function addMapControls() {
    const zoomControl = new kakao.maps.ZoomControl();
    map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    const mapTypeControl = new kakao.maps.MapTypeControl();
    map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);
}

/**
 * 지도 에러 표시
 */
function showMapError(message) {
    const mapContainer = document.getElementById('restaurantMap');
    if (mapContainer) {
        mapContainer.innerHTML = `
            <div style="display:flex;align-items:center;justify-content:center;height:100%;color:#666;font-size:14px;">
                <div style="text-align:center;">
                    <i class="fas fa-exclamation-triangle" style="font-size:24px;margin-bottom:8px;"></i><br>
                    ${message}
                </div>
            </div>
        `;
    }
}

/**
 * 이미지 핸들러 설정
 */
function setupImageHandlers() {
    const mainImage = document.querySelector('.main-image img');
    if (mainImage) {
        mainImage.addEventListener('error', function() {
            this.src = '/img/default-restaurant.png';
            this.alt = '기본 맛집 이미지';
        });
    }

    const relatedImages = document.querySelectorAll('.related-image img');
    relatedImages.forEach(img => {
        img.addEventListener('error', function() {
            this.src = '/img/default-restaurant.png';
            this.alt = '기본 맛집 이미지';
        });
    });
}

/**
 * 모달 핸들러 설정
 */
function setupModalHandlers() {
    // 필요시 추가 모달 핸들링 로직
}

/**
 * 공유 모달 열기
 */
function shareRestaurant() {
    const modal = document.getElementById('shareModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        history.pushState({modal: 'share'}, '', '');
    }
}

/**
 * 공유 모달 닫기
 */
function closeShareModal() {
    const modal = document.getElementById('shareModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

/**
 * 모든 모달 닫기
 */
function closeAllModals() {
    closeShareModal();
}

/**
 * 길찾기 기능
 */
function showDirections() {
    const lat = restaurant.lat || restaurant.LAT;
    const lng = restaurant.lng || restaurant.LNG;

    console.log('길찾기 좌표 확인:', { lat, lng });

    if (!lat || !lng) {
        console.error('길찾기용 좌표가 없습니다.');
        showToast('위치 정보가 없어서 길찾기를 할 수 없습니다.');
        return;
    }

    const title = encodeURIComponent(restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집');
    const kakaoMapUrl = `https://map.kakao.com/link/to/${title},${lat},${lng}`;

    try {
        window.open(kakaoMapUrl, '_blank', 'noopener,noreferrer');
        showToast('카카오맵에서 길찾기를 시작합니다.');
    } catch (error) {
        console.error('길찾기 실행 오류:', error);
        showToast('길찾기를 실행할 수 없습니다.');
    }
}

/**
 * 소셜 공유 기능들
 */
function shareKakao() {
    if (typeof Kakao !== 'undefined' && Kakao.Share) {
        Kakao.Share.sendDefault({
            objectType: 'location',
            address: restaurant.addr1 || restaurant.ADDR1 || '',
            addressTitle: restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집',
            content: {
                title: restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집',
                description: restaurant.rprsnTvMenu || restaurant.RPRSNTV_MENU || '맛있는 맛집을 소개합니다.',
                imageUrl: restaurant.mainImgNormal || restaurant.MAIN_IMG_NORMAL || '',
                link: {
                    mobileWebUrl: window.location.href,
                    webUrl: window.location.href
                }
            }
        });
    } else {
        showToast('카카오톡 공유 기능은 준비 중입니다.');
    }
}

function shareFacebook() {
    const url = encodeURIComponent(window.location.href);
    const shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}`;
    window.open(shareUrl, 'facebook-share-dialog', 'width=626,height=436,resizable=yes,scrollbars=yes');
}

function shareTwitter() {
    const url = encodeURIComponent(window.location.href);
    const text = encodeURIComponent(`${restaurant.mainTitle || restaurant.MAIN_TITLE || '맛집'} - 부산 맛집 추천`);
    const hashtags = encodeURIComponent('부산맛집,맛집추천');
    const shareUrl = `https://twitter.com/intent/tweet?url=${url}&text=${text}&hashtags=${hashtags}`;
    window.open(shareUrl, 'twitter-share-dialog', 'width=626,height=436,resizable=yes,scrollbars=yes');
}

function copyLink() {
    const currentUrl = window.location.href;
    copyToClipboard(currentUrl);
    closeShareModal();
}

/**
 * 클립보드 복사
 */
function copyToClipboard(text) {
    if (!text) {
        showToast('복사할 내용이 없습니다.');
        return;
    }

    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showToast('주소가 복사되었습니다!');
        }).catch(err => {
            fallbackCopyToClipboard(text);
        });
    } else {
        fallbackCopyToClipboard(text);
    }
}

/**
 * 클립보드 복사 폴백 방법
 */
function fallbackCopyToClipboard(text) {
    try {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        const successful = document.execCommand('copy');
        document.body.removeChild(textArea);

        if (successful) {
            showToast('주소가 복사되었습니다!');
        } else {
            showToast('복사에 실패했습니다.');
        }
    } catch (err) {
        showToast('복사 기능을 지원하지 않는 브라우저입니다.');
    }
}

/**
 * 찜하기 통계 업데이트 - 0부터 시작하도록 수정
 */
function updateFavoriteCount() {
    try {
        const favorites = JSON.parse(localStorage.getItem('restaurant_favorites') || '[]');
        const restaurantId = restaurant.ucSeq || restaurant.UC_SEQ || restaurant.id;

        // 0부터 시작하도록 수정 (567 제거)
        const favoriteCountKey = `restaurant_favorite_count_${restaurantId}`;
        let totalFavorites = parseInt(localStorage.getItem(favoriteCountKey) || '0');

        const isCurrentlyFavorite = favorites.some(fav => fav.id === restaurantId);
        const wasAlreadyCounted = localStorage.getItem(`user_favorited_${restaurantId}`);

        if (isCurrentlyFavorite && !wasAlreadyCounted) {
            totalFavorites += 1;
            localStorage.setItem(favoriteCountKey, totalFavorites.toString());
            localStorage.setItem(`user_favorited_${restaurantId}`, 'true');
        } else if (!isCurrentlyFavorite && wasAlreadyCounted) {
            totalFavorites = Math.max(0, totalFavorites - 1);
            localStorage.setItem(favoriteCountKey, totalFavorites.toString());
            localStorage.removeItem(`user_favorited_${restaurantId}`);
        }

        const heartStatItem = document.querySelector('#favoriteCountStat span');
        if (heartStatItem) {
            heartStatItem.textContent = `찜 ${totalFavorites.toLocaleString()}`;
        }

    } catch (error) {
        console.log('찜하기 카운트 업데이트 실패:', error);
    }
}

/**
 * 찜하기 기능
 */
function toggleFavorite() {
    try {
        const favorites = JSON.parse(localStorage.getItem('restaurant_favorites') || '[]');
        const restaurantId = restaurant.ucSeq || restaurant.UC_SEQ || restaurant.id;

        if (!restaurantId) {
            showToast('찜하기에 실패했습니다.');
            return;
        }

        const isAlreadyFavorite = favorites.some(fav => fav.id === restaurantId);

        if (isAlreadyFavorite) {
            // 찜 해제
            const updatedFavorites = favorites.filter(fav => fav.id !== restaurantId);
            localStorage.setItem('restaurant_favorites', JSON.stringify(updatedFavorites));
            updateFavoriteButton(false);
            updateFavoriteCount();
            showToast('찜하기가 해제되었습니다.');
        } else {
            // 찜 추가
            const newFavorite = {
                id: restaurantId,
                title: restaurant.mainTitle || restaurant.MAIN_TITLE,
                image: restaurant.mainImgThumb || restaurant.MAIN_IMG_THUMB,
                address: restaurant.addr1 || restaurant.ADDR1,
                addedAt: new Date().toISOString()
            };
            favorites.push(newFavorite);
            localStorage.setItem('restaurant_favorites', JSON.stringify(favorites));
            updateFavoriteButton(true);
            updateFavoriteCount();
            showToast('찜하기에 추가되었습니다.');
        }
    } catch (error) {
        console.error('찜하기 기능 오류:', error);
        showToast('찜하기 기능에 오류가 발생했습니다.');
    }
}

/**
 * 찜하기 버튼 상태 업데이트
 */
function updateFavoriteButton(isFavorite) {
    const favoriteBtn = document.querySelector('.btn-outline');
    if (favoriteBtn) {
        const icon = favoriteBtn.querySelector('i');

        if (isFavorite) {
            icon.className = 'fas fa-heart';
            favoriteBtn.style.background = '#17a2a9';
            favoriteBtn.style.color = 'white';
            favoriteBtn.style.borderColor = '#17a2a9';
        } else {
            icon.className = 'far fa-heart';
            favoriteBtn.style.background = '#1EC4CC';
            favoriteBtn.style.color = 'white';
            favoriteBtn.style.borderColor = '#1EC4CC';
        }
    }
}

/**
 * 페이지 로드 시 찜하기 상태 확인
 */
function checkFavoriteStatus() {
    try {
        const favorites = JSON.parse(localStorage.getItem('restaurant_favorites') || '[]');
        const restaurantId = restaurant.ucSeq || restaurant.UC_SEQ || restaurant.id;
        const isFavorite = favorites.some(fav => fav.id === restaurantId);

        // 찜하기 버튼 상태 업데이트
        updateFavoriteButton(isFavorite);

        // 찜 카운트를 0으로 초기화
        const heartStatItem = document.querySelector('#favoriteCountStat span');
        if (heartStatItem) {
            heartStatItem.textContent = '찜 0';
        }

        // 조회수도 0으로 초기화
        const viewStatItem = document.querySelector('#viewCountStat span');
        if (viewStatItem) {
            viewStatItem.textContent = '조회수 0';
        }

        updateFavoriteCount();
    } catch (error) {
        console.log('찜하기 상태 확인 실패:', error);
    }
}

/**
 * 토스트 메시지 표시
 */
function showToast(message, duration = 3000) {
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.textContent = message;
    toast.className = 'toast show';

    if (toast.timeoutId) {
        clearTimeout(toast.timeoutId);
    }

    toast.timeoutId = setTimeout(() => {
        toast.className = 'toast';
        delete toast.timeoutId;
    }, duration);
}

/**
 * 디버그 정보 출력
 */
function debugInfo() {
    console.log('=== 맛집 상세 페이지 디버그 정보 ===');
    console.log('Restaurant:', restaurant);
    console.log('좌표 정보:');
    console.log('- lat/LAT:', restaurant.lat || restaurant.LAT);
    console.log('- lng/LNG:', restaurant.lng || restaurant.LNG);
    console.log('Kakao Maps:', typeof kakao !== 'undefined' ? '로드됨' : '로드 안됨');
    console.log('Map:', map);
    console.log('Marker:', marker);
    console.log('=====================================');
}

// 개발 모드에서 디버그 함수를 전역으로 노출
if (typeof window !== 'undefined') {
    window.debugRestaurantDetail = debugInfo;
}