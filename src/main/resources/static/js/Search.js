document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input-buyer');
    const searchButton = document.getElementById('search-button-buyer');
    const searchDropdown = document.getElementById('search-dropdown-buyer');

    // 로컬 스토리지에서 자동완성 상태를 불러오거나 기본값(true) 설정
    let isAutocompleteEnabled = JSON.parse(localStorage.getItem('isAutocompleteEnabled')) !== false;

    // --- [ 드롭다운 UI 동적 생성 및 업데이트 함수 ] ---

    // 자동완성 on/off 버튼을 생성하고 이벤트 리스너를 추가하는 함수
    function createAutocompleteToggleButton() {
        const toggleBtn = document.createElement('button');
        toggleBtn.id = 'autocomplete-toggle-btn';
        toggleBtn.className = 'dropdown-toggle-btn';
        toggleBtn.textContent = isAutocompleteEnabled ? '자동완성 끄기' : '자동완성 켜기';

        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // 드롭다운 숨김 이벤트 방지
            isAutocompleteEnabled = !isAutocompleteEnabled;
            localStorage.setItem('isAutocompleteEnabled', isAutocompleteEnabled);
            toggleBtn.textContent = isAutocompleteEnabled ? '자동완성 끄기' : '자동완성 켜기';
            searchDropdown.style.display = 'none'; // 버튼 클릭 시 드롭다운 숨기기
        });

        return toggleBtn;
    }

    // 로컬 스토리지에서 검색 기록을 불러와서 드롭다운에 표시
    function loadSearchHistory() {
        searchDropdown.innerHTML = '';
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];

        if (history.length > 0) {
            const historyHeader = document.createElement('div');
            historyHeader.className = 'dropdown-header';
            historyHeader.innerHTML = '<strong>최근 검색어</strong>';

            const clearBtn = document.createElement('span');
            clearBtn.className = 'clear-btn';
            clearBtn.textContent = '전체 삭제';
            clearBtn.addEventListener('click', clearSearchHistory);

            historyHeader.appendChild(clearBtn);
            historyHeader.appendChild(createAutocompleteToggleButton());
            searchDropdown.appendChild(historyHeader);

            const historyList = document.createElement('ul');
            historyList.className = 'history-list';
            history.forEach(item => {
                const li = document.createElement('li');
                li.textContent = item;
                li.addEventListener('click', () => {
                    searchInput.value = item;
                    performSearch(item);
                });
                historyList.appendChild(li);
            });
            searchDropdown.appendChild(historyList);
            searchDropdown.style.display = 'block';
        } else {
            searchDropdown.style.display = 'none';
        }
    }

    // 자동완성 제안을 드롭다운에 표시
    function displayAutocompleteSuggestions(suggestions) {
        searchDropdown.innerHTML = '';

        if (suggestions.length > 0) {
            const header = document.createElement('div');
            header.className = 'dropdown-header';
            header.innerHTML = '<strong>자동완성</strong>';
            header.appendChild(createAutocompleteToggleButton());
            searchDropdown.appendChild(header);

            const suggestionList = document.createElement('ul');
            suggestionList.className = 'suggestion-list';
            suggestions.forEach(item => {
                const li = document.createElement('li');
                li.textContent = item;
                li.addEventListener('click', () => {
                    searchInput.value = item;
                    performSearch(item);
                });
                suggestionList.appendChild(li);
            });
            searchDropdown.appendChild(suggestionList);
            searchDropdown.style.display = 'block';
        } else {
            searchDropdown.style.display = 'none';
        }
    }

    // --- [ 로직 관련 함수 ] ---

    // 검색어를 로컬 스토리지에 저장
    function saveSearchHistory(keyword) {
        if (!keyword.trim()) return;
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];
        const normalizedKeyword = keyword.trim().toLowerCase();
        const updatedHistory = [normalizedKeyword, ...history.filter(item => item !== normalizedKeyword)];
        localStorage.setItem('searchHistory', JSON.stringify(updatedHistory.slice(0, 5)));
    }

    // 로컬 스토리지 검색 기록 전체 삭제
    function clearSearchHistory() {
        localStorage.removeItem('searchHistory');
        loadSearchHistory();
    }

    // 백엔드 API를 호출하여 자동완성 키워드 가져오기
    function fetchAutocompleteSuggestions(keyword) {
        if (!isAutocompleteEnabled) {
            searchDropdown.style.display = 'none';
            return;
        }
        fetch(`/api/autocomplete/keywords?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => displayAutocompleteSuggestions(data))
            .catch(error => console.error('Error fetching autocomplete data:', error));
    }

    // 검색 실행 함수
    function performSearch(keyword) {
        saveSearchHistory(keyword);
        window.location.href = `/product/search?keyword=${encodeURIComponent(keyword)}`;
    }

    // --- [ 이벤트 리스너 ] ---

    // 검색 버튼 클릭 시 검색 실행
    searchButton.addEventListener('click', () => performSearch(searchInput.value));

    // input 필드에서 Enter 키 입력 시 검색 실행
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch(searchInput.value);
        }
    });

    // input 필드에 입력할 때마다 자동완성 실행
    searchInput.addEventListener('input', (e) => {
        const keyword = e.target.value.trim();
        if (keyword.length > 0) {
            fetchAutocompleteSuggestions(keyword);
        } else {
            loadSearchHistory(); // 입력 내용이 없으면 검색 기록 표시
        }
    });

    // input 필드에 포커스될 때 검색 기록 표시
    searchInput.addEventListener('focus', () => {
        const keyword = searchInput.value.trim();
        if (keyword.length === 0) {
            loadSearchHistory();
        }
    });

    // 드롭다운 외부를 클릭하면 숨기기
    document.addEventListener('click', (e) => {
        if (!searchDropdown.contains(e.target) && !searchInput.contains(e.target)) {
            searchDropdown.style.display = 'none';
        }
    });

    // 초기 로드 시 검색 기록 불러오기 (검색창이 이미 포커스될 경우 대비)
    loadSearchHistory();
});