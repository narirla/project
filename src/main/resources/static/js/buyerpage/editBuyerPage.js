/*editBuyerPage.js*/
document.addEventListener('DOMContentLoaded', () => {
  const form          = document.querySelector('form');
  const nicknameInput = document.querySelector('#nickname');
  const nicknameBtn   = document.querySelector('#nicknameToggleBtn');
  const resultSpan    = document.querySelector('#nicknameCheckMsg');
  const introTextarea = document.querySelector('#intro');
  const imageInput    = document.querySelector('#imageFile');
  const saveBtn       = document.querySelector('#saveBtn');

  /* ───────────────────── 공통 값 ───────────────────── */
  const csrfInput   = document.querySelector('input[name="_csrf"]');
  const csrfToken   = csrfInput?.value;
  const csrfHeader  = 'X-CSRF-TOKEN';

  let isNicknameChecked = false;
  let checkedNickname   = '';
  const originalNickname = nicknameInput.value.trim();
  let isEditing = false;
  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;

  /* ─────────────── ❶ 닉네임 중복 확인 ─────────────── */
  nicknameBtn.addEventListener('click', async () => {
    const current = nicknameInput.value.trim();

    if (!isEditing) {
      nicknameInput.readOnly = false;
      nicknameInput.focus();
      nicknameBtn.textContent = '중복 확인';
      resultSpan.textContent = '';
      resultSpan.className = 'form-text ms-2';
      isEditing = true;
      isNicknameChecked = false;
      return;
    }

    if (!current) return showError('닉네임을 입력해주세요.');
    if (current.length < 2 || current.length > 30)
      return showError('닉네임은 2자 이상 30자 이하로 입력해주세요.');
    if (!nicknameRegex.test(current))
      return showError('닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.');

    try {
      const res = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(current)}`, {
        method: 'GET',
        credentials: 'same-origin'
      });
      const duplicated = await res.json();
      if (!duplicated) {
        resultSpan.textContent = '사용 가능한 닉네임입니다.';
        resultSpan.className   = 'form-text ms-2 text-success';
        isNicknameChecked = true;
        checkedNickname   = current;

        nicknameInput.readOnly = true;
        nicknameBtn.textContent = '변경하기';
        isEditing = false;
      } else {
        showError('이미 사용 중인 닉네임입니다.');
      }
    } catch (e) {
      showError('중복 확인 중 오류 발생');
      console.error(e);
    }
  });

  nicknameInput.addEventListener('input', () => {
    isNicknameChecked = false;
    resultSpan.textContent = '';
    resultSpan.className = 'form-text ms-2';
  });

  /* ─────────────── ❷ 회원정보 저장(POST) ─────────────── */
  saveBtn.addEventListener('click', async () => {
    const nickname = nicknameInput.value.trim();

    if (!nickname) return alertAndFocus('닉네임을 입력해주세요.');
    if (nickname.length < 2 || nickname.length > 30)
      return alertAndFocus('닉네임은 2자 이상 30자 이하로 입력해주세요.');
    if (!nicknameRegex.test(nickname))
      return alertAndFocus('닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.');
    if (nickname !== originalNickname && (!isNicknameChecked || checkedNickname !== nickname))
      return alertAndFocus('닉네임 중복 확인을 해주세요.');
    if (introTextarea.value.length > 500)
      return alertAndFocus('자기소개는 최대 500자까지 입력할 수 있습니다.', introTextarea);

    if (imageInput.files.length > 0) {
      const file = imageInput.files[0];
      const allowed = ['jpg', 'jpeg', 'png', 'gif'];
      const ext = file.name.split('.').pop().toLowerCase();
      const max = 2 * 1024 * 1024;

      if (!allowed.includes(ext)) {
        alert('이미지는 jpg, jpeg, png, gif 형식만 가능합니다.');
        imageInput.value = '';
        return;
      }

      if (file.size > max) {
        alert('이미지 크기는 최대 2MB까지 가능합니다.');
        imageInput.value = '';
        return;
      }
    }

    /* ➋ fetch 로 전송 */
    const fd = new FormData(form);
    try {
      const res = await fetch(form.action, {
        method      : 'POST',
        body        : fd,
        headers     : { [csrfHeader]: csrfToken },
        credentials : 'same-origin'
      });
      if (res.redirected) {
        location.href = res.url;
      } else if (res.ok) {
        alert('수정이 완료되었습니다.');
        location.reload();
      } else {
        alert('오류 발생 (status ' + res.status + ')');
      }
    } catch (e) {
      console.error(e);
      alert('네트워크 오류가 발생했습니다.');
    }
  });

  /* ──────────────── 헬퍼 함수 ──────────────── */
  function showError(msg) {
    resultSpan.textContent = msg;
    resultSpan.className   = 'form-text ms-2 text-danger';
    nicknameInput.focus();
  }

  function alertAndFocus(msg, el = nicknameInput) {
    alert(msg);
    el.focus();
  }
});
