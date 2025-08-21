document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input-buyer');
    const searchButton = document.getElementById('search-button-buyer');
    const searchDropdown = document.getElementById('search-dropdown-buyer');
    const searchList = document.getElementById('search-list-buyer');

    // 요소들이 HTML에 제대로 로드되었는지 확인하는 로그
    console.log("searchInput:", searchInput);
    console.log("searchButton:", searchButton);
    console.log("searchDropdown:", searchDropdown);
    console.log("searchList:", searchList);

    // 검색 관련 요소가 하나라도 없으면 여기서 스크립트 실행을 중단합니다.
    if (!searchInput || !searchButton || !searchDropdown || !searchList) {
        console.error("검색 관련 요소를 찾을 수 없습니다. HTML 구조(id)를 확인하세요.");
        return;
    }

    // 로컬 스토리지에서 자동완성 상태를 불러오거나 기본값(true) 설정
    let isAutocompleteEnabled = JSON.parse(localStorage.getItem('isAutocompleteEnabled')) !== false;

    // --- [ 드롭다운 UI 동적 생성 및 업데이트 함수 ] ---

    // 자동완성 on/off 스위치를 생성하고 이벤트 리스너를 추가하는 함수
    function createAutocompleteToggleSwitch() {
        const toggleContainer = document.createElement('div');
        toggleContainer.className = 'search-options-container';

        toggleContainer.innerHTML = `
            <div class="search-options">
                <span>자동완성</span>
                <label class="switch">
                    <input type="checkbox" id="autocompleteToggle" ${isAutocompleteEnabled ? 'checked' : ''}>
                    <span class="slider round"></span>
                </label>
            </div>
        `;

        const toggleInput = toggleContainer.querySelector('#autocompleteToggle');
        toggleInput.addEventListener('change', (e) => {
            isAutocompleteEnabled = e.target.checked;
            localStorage.setItem('isAutocompleteEnabled', isAutocompleteEnabled);
            updateDropdownContent(searchInput.value.trim());
        });

        searchDropdown.appendChild(toggleContainer);
    }

    // 이전에 검색한 기록을 로컬 스토리지에 저장하는 함수
    function saveSearchHistory(keyword) {
        console.log("saveSearchHistory 함수 호출. 저장할 키워드:", keyword);

        if (!keyword) return;
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];
        const newHistory = [keyword, ...history.filter(item => item !== keyword)];
        localStorage.setItem('searchHistory', JSON.stringify(newHistory.slice(0, 5))); // 최신 5개만 저장

        console.log("로컬 스토리지에 저장된 검색 기록:", localStorage.getItem('searchHistory'));
    }

    // 로컬 스토리지에서 검색 기록을 불러와 드롭다운에 표시하는 함수
    function loadSearchHistory() {
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];
        searchList.innerHTML = ''; // 기존 내용 초기화

        // ✅ [수정] 헤더가 이미 있으면 제거
        const existingHeader = searchDropdown.querySelector('.search-header');
        if (existingHeader) {
            existingHeader.remove();
        }

        // ✅ [추가] 안내 라벨과 전체 삭제 버튼을 포함하는 헤더 div 생성
        const historyHeader = document.createElement('div');
        historyHeader.className = 'search-header';
        historyHeader.innerHTML = `
            <span class="header-label">최근 검색어</span>
            <button class="delete-all-btn">전체 삭제</button>
        `;
        searchDropdown.prepend(historyHeader); // 드롭다운 최상단에 추가

        // 전체 삭제 버튼 이벤트 리스너 추가
        historyHeader.querySelector('.delete-all-btn').addEventListener('click', () => {
            localStorage.removeItem('searchHistory');
            loadSearchHistory();
        });


        if (history.length > 0) {
            history.forEach(item => {
                const li = document.createElement('li');
                li.className = 'search-item history-item';
                li.innerHTML = `<span>${item}</span><button class="delete-history-btn">삭제</button>`;
                li.addEventListener('click', (e) => {
                    e.stopPropagation(); // li 클릭 이벤트가 삭제 버튼 클릭에도 영향 미치지 않도록
                    performSearch(item);
                });
                li.querySelector('.delete-history-btn').addEventListener('click', (e) => {
                    e.stopPropagation();
                    deleteSearchHistory(item);
                });
                searchList.appendChild(li);
            });
        } else {
            const li = document.createElement('li');
            li.className = 'search-item no-history';
            li.textContent = '최근 검색 기록이 없습니다.';
            searchList.appendChild(li);
        }
    }

    // 검색 기록 삭제 함수
    function deleteSearchHistory(keywordToDelete) {
        let history = JSON.parse(localStorage.getItem('searchHistory')) || [];
        history = history.filter(item => item !== keywordToDelete);
        localStorage.setItem('searchHistory', JSON.stringify(history));
        loadSearchHistory(); // 삭제 후 목록 새로고침
    }

    // 자동완성 제안을 서버에서 가져와 드롭다운에 표시하는 함수
    function fetchAutocompleteSuggestions(keyword) {
        // ✅ [수정] 헤더가 이미 있으면 제거
        const existingHeader = searchDropdown.querySelector('.search-header');
        if (existingHeader) {
            existingHeader.remove();
        }

        // ✅ [추가] 자동완성 안내 라벨을 포함하는 헤더 div 생성
        const autocompleteHeader = document.createElement('div');
        autocompleteHeader.className = 'search-header';
        autocompleteHeader.innerHTML = `<span class="header-label">추천 검색어</span>`;
        searchDropdown.prepend(autocompleteHeader);

        fetch(`/api/autocomplete?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => {
                searchList.innerHTML = '';
                if (data.length > 0) {
                    data.forEach(item => {
                        const li = document.createElement('li');
                        li.className = 'search-item autocomplete-item';
                        li.textContent = item;
                        li.addEventListener('click', () => {
                            performSearch(item);
                        });
                        searchList.appendChild(li);
                    });
                } else {
                    // ✅ 수정: 자동완성 결과가 없으면 최근 검색 기록을 보여줌
                    searchList.innerHTML = ''; // 기존 내용 초기화
                    autocompleteHeader.remove(); // 추천 검색어 헤더 제거
                    loadSearchHistory();
                }
            })
            .catch(error => {
                console.error('자동완성 데이터를 가져오는 중 오류 발생:', error);
                // ✅ 수정: 에러 발생 시에도 최근 검색 기록을 보여줌
                searchList.innerHTML = '';
                autocompleteHeader.remove(); // 추천 검색어 헤더 제거
                loadSearchHistory();
            });
    }

    // 검색 실행 함수
    function performSearch(keyword) {
        console.log("performSearch 함수 호출. 검색어:", keyword);
        saveSearchHistory(keyword);
        window.location.href = `/product/search?keyword=${encodeURIComponent(keyword)}`;
    }

    // ✨✨✨ 드롭다운 내용 업데이트를 위한 단일 함수 추가 ✨✨✨
    function updateDropdownContent(keyword) {
        // ✅ 수정: 키워드가 있으면 자동완성(상품명)을, 없으면 최근 검색 기록을 보여줌
        if (keyword.length > 0 && isAutocompleteEnabled) {
            fetchAutocompleteSuggestions(keyword);
        } else {
            loadSearchHistory();
        }
    }

    // --- [ 이벤트 리스너 ] ---

    // 검색 버튼 클릭 시 검색 실행
    searchButton.addEventListener('click', () => {
        performSearch(searchInput.value);
    });

    // input 필드에서 Enter 키 입력 시 검색 실행
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch(searchInput.value);
        }
    });

    // 검색창 포커스 시 드롭다운 표시 및 내용 업데이트
    searchInput.addEventListener('focus', () => {
        searchDropdown.style.display = 'block';
        updateDropdownContent(searchInput.value.trim());
    });

    // 검색창 입력 시 드롭다운 내용 업데이트
    searchInput.addEventListener('input', (e) => {
        const keyword = e.target.value.trim();
        if (keyword.length > 0) {
            updateDropdownContent(keyword);
        } else {
            loadSearchHistory();
        }
    });

    // 드롭다운 외부를 클릭하면 드롭다운 숨기기
    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !searchDropdown.contains(e.target)) {
            searchDropdown.style.display = 'none';
        }
    });

    // 드롭다운 내부에 클릭 이벤트가 발생했을 때 버블링 막기
    searchDropdown.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    // 페이지 로드 시 기존 자동완성 on/off 스위치 생성
    createAutocompleteToggleSwitch();
});