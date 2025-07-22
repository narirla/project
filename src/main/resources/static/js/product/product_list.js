document.addEventListener("DOMContentLoaded", function() {
  // 1.공통 카테고리 코드-한글 변환
    const categoryMap = {
      "all": "전체",
      "area": "지역",
      "pet": "반려동물",
      "restaurant": "맛집",
      "culture_history": "문화/역사",
      "season_nature": "계절/자연",
      "silver_disables": "실버/교통약자"
    };

    // 1-1. breadcrumb
    const categorySpan = document.getElementById("category-select");
    if(categorySpan) {
      const categoryCode = categorySpan.dataset.category;
      categorySpan.textContent = categoryMap[categoryCode] || "지역";
      // 1-2. header h1(.category-select)도 같이 변환
      const h1 = document.querySelector(".category-header .category-select");
      if(h1) h1.textContent = categoryMap[categoryCode] || "지역";
    }

  // 2. custom-dropdown 기능 구현 (토글, 선택 레이블 변경, 바깥 클릭 시 닫기)
    document.querySelectorAll('.custom-dropdown').forEach(dropdown => {
      // dropdown-toggle 버튼은 .custom-dropdown의 부모인 .service-controls 안에 있습니다.
      const serviceControls = dropdown.closest('.service-controls');
      if (!serviceControls) {
        console.error(".service-controls 부모 요소를 찾을 수 없습니다.");
        return; // 부모가 없으면 스크립트 실행 중단
      }

      const btn = serviceControls.querySelector('.dropdown-toggle');
      const labelSpan = btn.querySelector('.selected-label');

      // 토글 열기/닫기
      btn.addEventListener('click', e => {
        e.stopPropagation(); // 열자마자 닫히는 현상 방지
        dropdown.classList.toggle('open');
      });

      // 옵션 클릭 → 레이블 교체 + 자동 닫기
      dropdown.querySelectorAll('input[type="radio"]').forEach(radio => {
        radio.addEventListener('change', () => {
          // 선택된 라디오 버튼의 부모 label의 텍스트를 가져와 selected-label에 표시합니다.
          // 이 과정에서 'expand_button' 프래그먼트가 삽입한 내용은 사라질 수 있습니다.
          // 만약 확장 버튼을 유지하려면, labelSpan의 텍스트만 업데이트하고 버튼은 건드리지 않도록
          // selected-label 내부 구조를 더 세분화하거나, 버튼을 다시 추가하는 로직이 필요합니다.
          // 예: labelSpan.textContent = radio.parentElement.textContent.trim();
          // 만약 화살표를 다시 넣고 싶다면: labelSpan.textContent = radio.parentElement.textContent.trim() + ' \u25BC';
          labelSpan.textContent = radio.parentElement.textContent.trim(); // 현재 선택된 옵션 텍스트로 변경

          dropdown.classList.remove('open'); // 드롭다운 닫기
        });
      });
    });

    // 바깥 클릭 시 열려있는 드롭다운 닫기
    document.addEventListener('click', e => {
      document.querySelectorAll('.custom-dropdown.open').forEach(d => {
        // 드롭다운 메뉴 자체나, 메뉴를 여는 버튼을 클릭한 경우에는 닫지 않도록 합니다.
        const serviceControlsParent = d.closest('.service-controls');
        const toggleButton = serviceControlsParent ? serviceControlsParent.querySelector('.dropdown-toggle') : null;

        // 클릭된 대상이 드롭다운 메뉴 안도 아니고, 토글 버튼 안도 아니라면 드롭다운을 닫습니다.
        if (!d.contains(e.target) && (!toggleButton || !toggleButton.contains(e.target))) {
          d.classList.remove('open');
        }
      });
    });

    // 좋아요 버튼 클릭 토글 (예시 - 드롭다운과 무관)
    document.addEventListener('click', e => {
      if (e.target.classList.contains('btn-like')) {
        e.target.classList.toggle('is-active');
      }
    });

});