document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input-buyer');
    const searchButton = document.getElementById('search-button-buyer');
    const searchDropdown = document.getElementById('search-dropdown-buyer');
    const searchList = document.getElementById('search-list-buyer');

    if (!searchInput || !searchButton || !searchDropdown || !searchList) {
        console.error("검색 관련 요소를 찾을 수 없습니다. HTML 구조(id)를 확인하세요.");
        return;
    }

    let isAutocompleteEnabled = JSON.parse(localStorage.getItem('isAutocompleteEnabled')) !== false;

    // --- [ 드롭다운 UI 동적 생성 및 업데이트 함수 ] ---

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

        if (!searchDropdown.querySelector('.search-options-container')) {
             searchDropdown.appendChild(toggleContainer);
        }
    }

    function saveSearchHistory(keyword) {
        if (!keyword) return;
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];
        const newHistory = [keyword, ...history.filter(item => item !== keyword)];
        localStorage.setItem('searchHistory', JSON.stringify(newHistory.slice(0, 5)));
    }

    function removeHeader() {
        const existingHeader = searchDropdown.querySelector('.search-header');
        if (existingHeader) {
            existingHeader.remove();
        }
    }

    function loadSearchHistory() {
        removeHeader();
        const history = JSON.parse(localStorage.getItem('searchHistory')) || [];

        const historyHeader = document.createElement('div');
        historyHeader.className = 'search-header';
        historyHeader.innerHTML = `
            <span class="header-label">최근 검색어</span>
            <button class="delete-all-btn">전체 삭제</button>
        `;
        searchDropdown.prepend(historyHeader);

        let historyHtml = '';
        if (history.length > 0) {
            historyHtml += history.map(item => `
                <li class="search-item history-item">
                    <span class="history-keyword">${item}</span>
                    <button class="delete-history-btn" data-keyword="${item}">삭제</button>
                </li>
            `).join('');
        } else {
            historyHtml += `<li class="search-item no-history">최근 검색 기록이 없습니다.</li>`;
        }
        searchList.innerHTML = historyHtml;
    }

    async function fetchAutocompleteSuggestions(keyword) {
        try {
            removeHeader();
            searchList.innerHTML = `<li class="loading-message">검색 중...</li>`;

            const response = await fetch(`/api/autocomplete?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            const suggestions = await response.json();

            if (suggestions.length === 0) {
                searchList.innerHTML = `<li class="search-item no-results">일치하는 결과가 없습니다.</li>`;
                return;
            }

            const suggestionsHeader = document.createElement('div');
            suggestionsHeader.className = 'search-header';
            suggestionsHeader.innerHTML = `<span class="header-label">추천 검색어</span>`;
            searchDropdown.prepend(suggestionsHeader);

            const suggestionsHtml = suggestions.map(item => `
                <li class="search-item suggestion-item">
                    <span class="suggestion-keyword">${item}</span>
                </li>
            `).join('');

            searchList.innerHTML = suggestionsHtml;

        } catch (error) {
            console.error('자동완성 제안을 가져오는 중 오류 발생:', error);
            searchList.innerHTML = `<li class="search-item no-results">검색 중 오류가 발생했습니다.</li>`;
        }
    }

    function updateDropdownContent(keyword) {
        if (keyword.length === 0) {
            loadSearchHistory();
            searchDropdown.style.display = 'block';
            return;
        }

        if (isAutocompleteEnabled) {
            fetchAutocompleteSuggestions(keyword);
            searchDropdown.style.display = 'block';
        } else {
            searchDropdown.style.display = 'none';
        }
    }

    function performSearch(keyword) {
        const trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length > 0) {
            saveSearchHistory(trimmedKeyword);
            // ✅ 공백을 포함한 검색어는 URL 인코딩을 적용하여 전송합니다.
            window.location.href = `/product/search?keyword=${encodeURIComponent(trimmedKeyword)}`;
        }
    }

    // --- [ 이벤트 리스너 ] ---

    searchButton.addEventListener('click', () => {
        performSearch(searchInput.value);
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch(searchInput.value);
        }
    });

    searchInput.addEventListener('focus', () => {
        updateDropdownContent(searchInput.value.trim());
    });

    searchInput.addEventListener('input', (e) => {
        updateDropdownContent(e.target.value.trim());
    });

    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !searchDropdown.contains(e.target)) {
            searchDropdown.style.display = 'none';
        }
    });

    searchDropdown.addEventListener('click', (e) => {
        e.stopPropagation();

        if (e.target.classList.contains('delete-all-btn')) {
            localStorage.removeItem('searchHistory');
            loadSearchHistory();
            return;
        }

        const deleteButton = e.target.closest('.delete-history-btn');
        if (deleteButton) {
            const keywordToDelete = deleteButton.dataset.keyword;
            let history = JSON.parse(localStorage.getItem('searchHistory')) || [];
            history = history.filter(item => item !== keywordToDelete);
            localStorage.setItem('searchHistory', JSON.stringify(history));
            loadSearchHistory();
            return;
        }

        const searchItem = e.target.closest('.search-item');
        if (searchItem) {
            const keyword = searchItem.querySelector('span')?.textContent || searchItem.textContent;
            searchInput.value = keyword;
            performSearch(keyword);
        }
    });

    createAutocompleteToggleSwitch();
    loadSearchHistory();
});