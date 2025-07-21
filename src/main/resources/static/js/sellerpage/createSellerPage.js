/*createSellerPage.js*/
document.addEventListener('DOMContentLoaded', function () {
  const nicknameInput = document.getElementById('nickname');
  const checkBtn = document.getElementById('nicknameToggleBtn');
  const msg = document.getElementById('nicknameCheckMsg');
  const form = document.getElementById('sellerCreateForm');

  let isNicknameAvailable = false;

  if (nicknameInput && checkBtn && msg) {
    checkBtn.addEventListener('click', () => {
      const nickname = nicknameInput.value.trim();

      if (!nickname) {
        msg.textContent = '닉네임을 입력해주세요.';
        msg.classList.remove('success', 'error');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
        isNicknameAvailable = false;
        return;
      }

      fetch(`/mypage/seller/nickname-check?nickname=${encodeURIComponent(nickname)}`)
        .then(res => res.json())
        .then(data => {
          msg.classList.remove('success', 'error');
          if (data.available) {
            msg.textContent = '사용 가능한 닉네임입니다.';
            msg.classList.add('form-text', 'success');
            isNicknameAvailable = true;
          } else {
            msg.textContent = '이미 사용 중인 닉네임입니다.';
            msg.classList.add('form-text', 'error');
            nicknameInput.focus();
            isNicknameAvailable = false;
          }
        })
        .catch(() => {
          msg.textContent = '중복 확인 중 오류가 발생했습니다.';
          msg.classList.remove('success', 'error');
          msg.classList.add('form-text', 'error');
          isNicknameAvailable = false;
        });

      // 입력값이 변경되면 중복 확인 다시 하도록 설정
      nicknameInput.addEventListener('input', () => {
        isNicknameAvailable = false;
        msg.textContent = '';
        msg.classList.remove('success', 'error');
      });
    });
  }

  if (form) {
    form.addEventListener('submit', function (e) {
      if (!isNicknameAvailable) {
        e.preventDefault();
        msg.textContent = '닉네임 중복 확인을 먼저 해주세요.';
        msg.classList.remove('success', 'error');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
      }
    });
  }
});
