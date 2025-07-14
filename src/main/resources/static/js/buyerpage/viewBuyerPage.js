document.addEventListener('DOMContentLoaded', () => {
  /* ================================
   * 1. 3초 뒤 알림 박스 자동 숨김
   * ================================ */
  const alertBox = document.querySelector('.alert');
  if (alertBox) {
    setTimeout(() => alertBox.style.display = 'none', 3000);
  }

  /* ==========================================
   * 2. 역할 전환 버튼 중복 클릭 방지
   *    - 존재 여부 먼저 확인
   *    - disable + 텍스트 변경
   * ========================================== */
  const roleChangeForm = document.querySelector('form[action="/mypage/role/to-seller"]');
  roleChangeForm?.addEventListener('submit', (e) => {
    const submitBtn = roleChangeForm.querySelector('button[type="submit"]');
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = '처리 중...';
    }
  });

  /* ==========================================
   * 3. (TODO) 향후 기능 추가 지점
   *    - 여기서 DOM을 사용할 때는 항상
   *      const el = document.querySelector(...);
   *      el && el.addEventListener(...);
   *      형태로 null-check 후 사용
   * ========================================== */
});
