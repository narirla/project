document.addEventListener("DOMContentLoaded", function() {
  const slides = document.querySelectorAll(".slide");     // 모든 슬라이드 선택
  const prevBtn = document.getElementById("prevBtn");     // 이전 버튼
  const nextBtn = document.getElementById("nextBtn");     // 다음 버튼
  let currentIndex = 0;                                    // 현재 보여지는 인덱스

  // 슬라이드 보이고 숨기는 함수
  function showSlide(index) {
    slides.forEach((slide, i) => {
      slide.classList.toggle("active", i === index);
    });
  }

  // 이전 버튼 클릭
  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === 0) ? slides.length - 1 : currentIndex - 1;  // 처음이면 마지막으로
    showSlide(currentIndex);
  });

  // 다음 버튼 클릭
  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === slides.length - 1) ? 0 : currentIndex + 1;  // 마지막이면 처음으로
    showSlide(currentIndex);
  });

  // 처음 슬라이드 표시
  showSlide(currentIndex);

  // 숫자 포매팅
  const priceElems = document.querySelectorAll(".allPrice");
  priceElems.forEach(priceElem => {
    const originalText = priceElem.textContent;
    const onlyNumber = originalText.match(/[\d.-]+/g);  // 숫자 및 마이너스, 점만 추출
    if (onlyNumber) {
      const numberValue = Number(onlyNumber.join(""));
      if (!isNaN(numberValue)) {
        const formattedNumber = numberValue.toLocaleString();
        const newText = originalText.replace(/[\d.,-]+/, formattedNumber);
        priceElem.textContent = newText;
      }
    }
  });

  // ---------------------------------
  // 탭 메뉴 active 클래스 처리 추가
  const tabLinks = document.querySelectorAll(".tab-menu a");
  tabLinks.forEach(link => {
    link.addEventListener("click", function(event) {
      event.preventDefault();  // 기본 앵커 이동 막기 (필요 시 생략 가능)
      // 모든 탭에서 active 클래스 제거
      tabLinks.forEach(item => item.classList.remove("active"));
      // 클릭한 탭에 active 클래스 추가
      this.classList.add("active");

      // 선택한 탭에 해당하는 섹션으로 부드럽게 스크롤 이동 (필요하면)
      const targetId = this.getAttribute("href").substring(1);
      const targetElement = document.getElementById(targetId);
      if(targetElement) {
        targetElement.scrollIntoView({ behavior: "smooth" });
      }
    });
  });

// expand_button 프래그먼트가 포함된 요소 클래스명 확인 필요
  const expandButtons = document.querySelectorAll('.ex_btn');

  expandButtons.forEach(button => {
    button.style.cursor = 'pointer'; // 버튼임을 명시적 표시

    button.addEventListener('click', () => {
      const policyMenu = button.closest('.policy-menu');
      if (!policyMenu) return;

      const content = policyMenu.nextElementSibling;
      if (!content || !content.classList.contains('policy-content')) return;

      content.classList.toggle('active'); // 내용 표시 토글
      button.classList.toggle('active');  // 버튼 상태 토글 (화살표 회전 등 CSS 연동)
    });
  });
});