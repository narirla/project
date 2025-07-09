// 상품 업로드 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeImageUpload();
    initializeTagInput();
    initializeFormValidation();
    initializeKakaoMap();

    // 달력 아이콘 클릭 시 input 포커스
    document.querySelectorAll('.date-input-wrap i').forEach(function(icon) {
      icon.addEventListener('click', function() {
        this.parentElement.querySelector('input[type="date"]').focus();
      });
    });
});

// 이미지 업로드 초기화
function initializeImageUpload() {
    // 이미지 업로드 영역 클릭 시 파일 선택 다이얼로그 표시
    document.querySelectorAll('.image-upload').forEach(upload => {
        upload.addEventListener('click', function() {
            this.querySelector('input[type="file"]').click();
        });
    });

    // 이미지 미리보기
    function previewImages() {
      const input = document.getElementById('productImages');
      renderPreviews(input.files);
    }

    function handleDrop(event) {
      event.preventDefault();
      const input = document.getElementById('productImages');
      input.files = event.dataTransfer.files;
      renderPreviews(input.files);
    }

    function renderPreviews(fileList) {
      const previewArea = document.getElementById('imagePreviewArea');
      const min = 5, max = 10; // 최소 5, 최대 10장
      previewArea.innerHTML = '';
      document.getElementById('imageCountWarn').textContent = '';

      if (fileList.length < min) {
        document.getElementById('imageCountWarn').textContent = `최소 ${min}장 이상의 이미지를 선택해야 합니다.`;
      }
      if (fileList.length > max) {
        document.getElementById('imageCountWarn').textContent = `최대 ${max}장까지만 업로드 가능합니다.`;
        return;
      }

      for (let i = 0; i < fileList.length; i++) {
        const file = fileList[i];
        if (file.type.startsWith('image/')) {
          const reader = new FileReader();
          reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.width = "120px";
            img.style.height = "120px";
            img.style.objectFit = "cover";
            img.style.border = "1px solid #eee";
            img.style.borderRadius = "8px";
            previewArea.appendChild(img);
          }
          reader.readAsDataURL(file);
        }
      }
    }

    // 이미지 제거 기능
    document.addEventListener('click', function(e) {
        if (e.target.closest('.remove-image')) {
            const uploadArea = e.target.closest('.image-upload');
            const input = uploadArea.querySelector('input[type="file"]');
            const preview = uploadArea.querySelector('.image-preview');
            const icon = uploadArea.querySelector('i.fa-camera');
            const text = uploadArea.querySelector('p');
            
            input.value = '';
            preview.style.display = 'none';
            icon.style.display = 'block';
            text.style.display = 'block';
            e.target.closest('.remove-image').style.display = 'none';
        }
    });
}

// 태그 입력 초기화
function initializeTagInput() {
    const tagInput = document.querySelector('.tag-input input');
    const tagContainer = document.querySelector('.tag-input');
    
    if (tagInput) {
        tagInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const tag = this.value.trim();
                if (tag && !tag.startsWith('#')) {
                    addTag('#' + tag);
                    this.value = '';
                } else if (tag) {
                    addTag(tag);
                    this.value = '';
                }
            }
        });
    }
    
    // 기존 태그 제거 기능
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('fa-times')) {
            e.target.parentElement.remove();
        }
    });
}

// 태그 추가
function addTag(tagText) {
    const tagContainer = document.querySelector('.tag-input');
    const tag = document.createElement('div');
    tag.className = 'tag';
    tag.innerHTML = `${tagText} <i class="fas fa-times"></i>`;
    tagContainer.insertBefore(tag, tagContainer.querySelector('input'));
}

// 폼 검증 초기화
function initializeFormValidation() {
    const form = document.getElementById('productForm');
    
    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
        }
    });
}

// 폼 검증
function validateForm() {
    let isValid = true;
    
    // 상품명 검증
    const productName = document.getElementById('productName');
    if (!productName.value.trim()) {
        showError(productName, '상품명을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productName);
    }
    
    // 가격 검증
    const productPrice = document.getElementById('productPrice');
    if (!productPrice.value || productPrice.value <= 0) {
        showError(productPrice, '올바른 가격을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productPrice);
    }
    
    // 카테고리 검증
    const productCategory = document.getElementById('productCategory');
    if (!productCategory.value) {
        showError(productCategory, '카테고리를 선택해주세요.');
        isValid = false;
    } else {
        hideError(productCategory);
    }
    
    // 여행 기간 검증
    const startDate = document.getElementById('startDate');
    const endDate = document.getElementById('endDate');
    if (!startDate.value || !endDate.value) {
        showError(startDate, '여행 기간을 설정해주세요.');
        isValid = false;
    } else if (new Date(startDate.value) >= new Date(endDate.value)) {
        showError(endDate, '종료일은 시작일보다 늦어야 합니다.');
        isValid = false;
    } else {
        hideError(startDate);
        hideError(endDate);
    }
    
    // 상품 설명 검증
    const productDescription = document.getElementById('productDescription');
    if (!productDescription.value.trim()) {
        showError(productDescription, '상품 설명을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productDescription);
    }
    
    // 최대 참가 인원 검증
    const maxParticipants = document.getElementById('maxParticipants');
    if (!maxParticipants.value || maxParticipants.value <= 0) {
        showError(maxParticipants, '올바른 최대 참가 인원을 입력해주세요.');
        isValid = false;
    } else {
        hideError(maxParticipants);
    }

    // 필요 금액 검증
    const reqMoney = document.getElementById('reqMoney');
    if (reqMoney && reqMoney.value && reqMoney.value < 0) {
        showError(reqMoney, '필요 금액은 0 이상이어야 합니다.');
        isValid = false;
    } else if (reqMoney) {
        hideError(reqMoney);
    }

    // 나이 검증
    const age = document.getElementById('age');
    if (age && age.value && age.value <= 0) {
        showError(age, '나이는 1 이상이어야 합니다.');
        isValid = false;
    } else if (age) {
        hideError(age);
    }

    // 대표 이미지 업로드 검증
    const productImage = document.getElementById('productImage');
    if (!productImage.files || productImage.files.length === 0) {
        alert('대표 이미지를 업로드해주세요.');
        isValid = false;
    }

    return isValid;
}

// 에러 메시지 표시
function showError(element, message) {
    const errorDiv = element.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
    element.style.borderColor = '#e74c3c';
}

// 에러 메시지 숨기기
function hideError(element) {
    const errorDiv = element.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
    element.style.borderColor = '#e0e0e0';
}

// 가격 포맷팅
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function initializeKakaoMap() {
    var mapContainer = document.getElementById('map');
    if (!mapContainer || typeof kakao === 'undefined' || !kakao.maps) return;
    var mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
    };
    var map = new kakao.maps.Map(mapContainer, mapOption);
    var places = new kakao.maps.services.Places();

    var startMarker = null;
    var endMarker = null;
    var realRoutePolyline = null;
    var startLatLng = null;
    var endLatLng = null;

    // 검색 결과 리스트 UI
    function showSearchResults(results, inputId, onSelect) {
        var input = document.getElementById(inputId);
        var resultBoxId = inputId + '-results';
        var oldBox = document.getElementById(resultBoxId);
        if (oldBox) oldBox.remove();
        var box = document.createElement('div');
        box.id = resultBoxId;
        box.style.position = 'absolute';
        box.style.background = '#fff';
        box.style.border = '1.5px solid #ccc';
        box.style.zIndex = 1000;
        box.style.width = input.offsetWidth + 'px';
        box.style.maxHeight = '180px';
        box.style.overflowY = 'auto';
        box.style.left = input.offsetLeft + 'px';
        box.style.top = (input.offsetTop + input.offsetHeight + 2) + 'px';
        results.forEach(function(place) {
            var item = document.createElement('div');
            item.textContent = place.place_name;
            item.style.padding = '8px 12px';
            item.style.cursor = 'pointer';
            item.addEventListener('mousedown', function(e) {
                e.preventDefault();
                onSelect(place);
                box.remove();
            });
            box.appendChild(item);
        });
        // route-search-box 기준으로 append
        var parentBox = input.closest('.route-search-box');
        parentBox.style.position = 'relative';
        parentBox.appendChild(box);
        // 외부 클릭 시 닫기
        document.addEventListener('mousedown', function handler(e) {
            if (!box.contains(e.target) && e.target !== input) {
                box.remove();
                document.removeEventListener('mousedown', handler);
            }
        });
    }

    // 디바운스 유틸
    function debounce(fn, delay) {
        let timer = null;
        return function(...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), delay);
        };
    }

    // 출발지 검색
    var startInput = document.getElementById('startSearch');
    startInput.addEventListener('input', debounce(function() {
        var query = this.value;
        if (!query.trim()) return;
        places.keywordSearch(query, function(data, status) {
            if (status === kakao.maps.services.Status.OK) {
                showSearchResults(data, 'startSearch', function(place) {
                    if (startMarker) startMarker.setMap(null);
                    var latlng = new kakao.maps.LatLng(place.y, place.x);
                    startMarker = new kakao.maps.Marker({ position: latlng, map: map });
                    map.setCenter(latlng);
                    startLatLng = latlng;
                    document.getElementById('latitude').value = latlng.getLat();
                    document.getElementById('longitude').value = latlng.getLng();
                });
                // 출발지 검색 input에서 엔터 시 form submit, 페이지 이동 모두 방지
                startInput.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                });
            }
        });
    }, 400));
    // 도착지 검색
    var endInput = document.getElementById('endSearch');
    endInput.addEventListener('input', debounce(function() {
        var query = this.value;
        if (!query.trim()) return;
        places.keywordSearch(query, function(data, status) {
            if (status === kakao.maps.services.Status.OK) {
                showSearchResults(data, 'endSearch', function(place) {
                    if (endMarker) endMarker.setMap(null);
                    var latlng = new kakao.maps.LatLng(place.y, place.x);
                    endMarker = new kakao.maps.Marker({ position: latlng, map: map });
                    map.setCenter(latlng);
                    endLatLng = latlng;
                });
                // 도착지 검색 input에서 엔터 시 form submit, 페이지 이동 모두 방지
                endInput.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                });
            }
        });
    }, 400));

    // 실제 경로 그리기 버튼
    var drawBtn = document.getElementById('drawRealRouteBtn');
    if (drawBtn) {
        drawBtn.addEventListener('click', function() {
            if (!startLatLng || !endLatLng) {
                alert('출발지와 도착지를 모두 검색/선택하세요!');
                return;
            }
            var routeType = document.getElementById('routeType').value;
            getRouteFromKakao(startLatLng.getLat(), startLatLng.getLng(), endLatLng.getLat(), endLatLng.getLng(), routeType, function(path, summary, steps) {
                if (realRoutePolyline) realRoutePolyline.setMap(null);
                realRoutePolyline = new kakao.maps.Polyline({
                    path: path,
                    strokeWeight: 5,
                    strokeColor: '#ff9800',
                    strokeOpacity: 0.9,
                    strokeStyle: 'solid'
                });
                realRoutePolyline.setMap(map);
                // 경로 좌표를 input에 JSON으로 저장
                var routeInput = document.getElementById('routePoints');
                if (routeInput) {
                    routeInput.value = JSON.stringify(
                        path.map(function(ll) {
                            return {lat: ll.getLat(), lng: ll.getLng()};
                        })
                    );
                }
                // 요약 정보 표시
                var summaryDiv = document.getElementById('routeSummary');
                if (summaryDiv) {
                    summaryDiv.innerHTML =
                        `총 거리: ${(summary.distance/1000).toFixed(2)}km, 예상 시간: ${(summary.duration/60).toFixed(0)}분, 예상 요금: ${summary.fare.taxi ? summary.fare.taxi+'원' : 'N/A'}`;
                }
                // 상세 안내 표시
                var stepsDiv = document.getElementById('routeSteps');
                if (stepsDiv) {
                    stepsDiv.innerHTML = steps.map(function(step, idx) {
                        return `<div style="margin-bottom:4px;">${idx+1}. ${step.description} <span style='color:#888;'>(${(step.distance/1000).toFixed(2)}km)</span></div>`;
                    }).join('');
                }
            });
        });
    }

    // 경로 초기화 버튼
    var clearBtn = document.getElementById('clearRouteBtn');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            if (startMarker) startMarker.setMap(null);
            if (endMarker) endMarker.setMap(null);
            startMarker = null;
            endMarker = null;
            startLatLng = null;
            endLatLng = null;
            if (realRoutePolyline) realRoutePolyline.setMap(null);
            document.getElementById('latitude').value = '';
            document.getElementById('longitude').value = '';
            document.getElementById('routePoints').value = '';
            document.getElementById('startSearch').value = '';
            document.getElementById('endSearch').value = '';
            var summaryDiv = document.getElementById('routeSummary');
            if (summaryDiv) summaryDiv.innerHTML = '';
            var stepsDiv = document.getElementById('routeSteps');
            if (stepsDiv) stepsDiv.innerHTML = '';
        });
    }
    // 뒤로가기(undo)는 검색 기반에서는 비활성화(또는 필요시 최근 마커만 삭제)
    var backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            // 최근 마커/좌표만 삭제 (출발→도착 순)
            if (endMarker) {
                endMarker.setMap(null);
                endMarker = null;
                endLatLng = null;
                document.getElementById('endSearch').value = '';
            } else if (startMarker) {
                startMarker.setMap(null);
                startMarker = null;
                startLatLng = null;
                document.getElementById('startSearch').value = '';
                document.getElementById('latitude').value = '';
                document.getElementById('longitude').value = '';
            }
            if (realRoutePolyline) realRoutePolyline.setMap(null);
            document.getElementById('routePoints').value = '';
            var summaryDiv = document.getElementById('routeSummary');
            if (summaryDiv) summaryDiv.innerHTML = '';
            var stepsDiv = document.getElementById('routeSteps');
            if (stepsDiv) stepsDiv.innerHTML = '';
        });
    }
}

// 카카오 길찾기 REST API로 실제 경로 요청 (교통수단/요약/상세 안내 포함)
function getRouteFromKakao(startLat, startLng, endLat, endLng, routeType, callback) {
    fetch(`https://apis-navi.kakaomobility.com/v1/directions?origin=${startLng},${startLat}&destination=${endLng},${endLat}&priority=${routeType}`, {
        method: 'GET',
        headers: {
            'Authorization': 'KakaoAK d0310bbc4a43cd1cf52c4a884e737fee'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (!data.routes || !data.routes[0]) {
            alert('경로를 찾을 수 없습니다.');
            return;
        }
        const route = data.routes[0].sections[0].roads.flatMap(road => road.vertexes);
        const path = [];
        for (let i = 0; i < route.length; i += 2) {
            path.push(new kakao.maps.LatLng(route[i+1], route[i]));
        }
        // 요약 정보
        const summary = data.routes[0].summary;
        // 상세 안내(턴 바이 턴)
        const steps = data.routes[0].sections[0].roads.map(road => ({
            description: road.name ? road.name + ' ' + road.description : road.description,
            distance: road.distance
        }));
        callback(path, summary, steps);
    });
}   
