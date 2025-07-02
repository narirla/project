/*buyerMyPageHome.js*/
document.addEventListener("DOMContentLoaded", function () {
  console.log("구매자 마이페이지 로드 완료");

  // 역할 전환 버튼 클릭 시 확인 알림 (선택 사항)
  const switchBtn = document.querySelector("form[action='/mypage/switch-role'] button");
  if (switchBtn) {
    switchBtn.addEventListener("click", function (e) {
      const confirmSwitch = confirm("판매자 마이페이지로 이동하시겠습니까?");
      if (!confirmSwitch) {
        e.preventDefault(); // 전환 취소
      }
    });
  }

  // 메뉴 항목 클릭 시 하이라이트 효과 (선택사항 확장 가능)
  const menuLinks = document.querySelectorAll(".menu-grid .col a");
  menuLinks.forEach(link => {
    link.addEventListener("click", function () {
      menuLinks.forEach(l => l.classList.remove("active"));
      this.classList.add("active");
    });
  });
});
