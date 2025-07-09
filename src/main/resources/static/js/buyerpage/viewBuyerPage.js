// /static/js/buyerpage/viewBuyerPage.js

document.addEventListener('DOMContentLoaded', function () {
  // ✅ 수정 성공 메시지 자동 숨김 처리
  const alertBox = document.querySelector('.alert');
  if (alertBox) {
    setTimeout(() => {
      alertBox.style.display = 'none';
    }, 3000); // 3초 후 사라짐
  }

  // ✅ 역할 전환 버튼 중복 클릭 방지
  const roleChangeForm = document.querySelector('form[action="/mypage/role/to-seller"]');
  if (roleChangeForm) {
    roleChangeForm.addEventListener('submit', function (e) {
      const btn = roleChangeForm.querySelector('button[type="submit"]');
      if (btn) {
        btn.disabled = true;
        btn.textContent = '처리 중...';
      }
    });
  }

  // ✳️ 향후 기능 추가 영역
  // 예) 알림 설정, 프로필 클릭 등
});

