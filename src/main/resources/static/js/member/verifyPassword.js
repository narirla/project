/* /js/member/verifyPassword.js */

(() => {
  const $  = (s,p=document)=>p.querySelector(s);

  document.addEventListener('DOMContentLoaded', () => {
    const wrap   = $('.input-wrap');
    const input  = $('#currentPassword');
    const toggle = $('.toggle-btn', wrap);
    const clear  = $('.clear-btn', wrap);
    const icon   = toggle?.querySelector('i');

    if (!wrap || !input || !toggle || !icon) return;

    // 초기 상태 동기화 (기본: 숨김)
    const sync = () => {
      const visible = input.type === 'text';
      toggle.setAttribute('aria-pressed', String(visible));
      toggle.setAttribute('aria-label', visible ? '비밀번호 숨기기' : '비밀번호 표시');
      icon.classList.remove('fa-regular','fa-solid','fa-eye','fa-eye-slash');
      // ✅ 상태 기준: 보임=eye, 숨김=eye-slash
      icon.classList.add('fa-regular', visible ? 'fa-eye' : 'fa-eye-slash');

      // X 버튼 가시성
      if (clear) clear.hidden = !input.value;
    };
    sync();

    // 눈 토글
    toggle.addEventListener('click', () => {
      const willShow = input.type === 'password';
      input.type = willShow ? 'text' : 'password';
      sync();
    });

    // X(지우기)
    clear?.addEventListener('click', () => {
      input.value = '';
      // 지우면 자동으로 숨김 유지
      if (input.type === 'text') { input.type = 'password'; }
      input.focus();
      sync();
    });

    // 입력 중 X 버튼 표시 업데이트
    input.addEventListener('input', sync);
  });
})();
