/* buyerMyPageHome.js */
document.addEventListener("DOMContentLoaded", function () {
  console.log("구매자 마이페이지 로드 완료");

  // ✅ 1. 역할 전환 버튼 클릭 시 확인 알림
  const switchBtn = document.querySelector("form[action='/mypage/switch-role'] button");
  if (switchBtn) {
    switchBtn.addEventListener("click", function (e) {
      const confirmSwitch = confirm("판매자 마이페이지로 이동하시겠습니까?");
      if (!confirmSwitch) {
        e.preventDefault(); // 전환 취소
      }
    });
  }

  // ✅ 2. 메뉴 항목 클릭 시 하이라이트 효과
  const menuLinks = document.querySelectorAll(".menu-grid .col a");
  menuLinks.forEach(link => {
    link.addEventListener("click", function () {
      menuLinks.forEach(l => l.classList.remove("active"));
      this.classList.add("active");
    });
  });

  // ✅ 3. 최근 본 코스 < > 슬라이드 버튼 기능 추가
  const prevBtn = document.querySelector('.prev-btn');
  const nextBtn = document.querySelector('.next-btn');
  const courseList = document.querySelector('.recent-course-list');

  if (prevBtn && nextBtn && courseList) {
    // 왼쪽 버튼 클릭 시 → 왼쪽으로 스크롤
    prevBtn.addEventListener('click', () => {
      courseList.scrollBy({ left: -220, behavior: 'smooth' });
    });

    // 오른쪽 버튼 클릭 시 → 오른쪽으로 스크롤
    nextBtn.addEventListener('click', () => {
      courseList.scrollBy({ left: 220, behavior: 'smooth' });
    });
  }

  // // ✅ 4. 스크롤 시 버튼 활성/비활성 처리 (선택사항)
  // if (courseList && prevBtn && nextBtn) {
  //   courseList.addEventListener('scroll', () => {
  //     prevBtn.disabled = courseList.scrollLeft <= 0;
  //     const maxScroll = courseList.scrollWidth - courseList.clientWidth;
  //     nextBtn.disabled = courseList.scrollLeft >= maxScroll;
  //   });
  // }
});
