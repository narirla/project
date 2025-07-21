/*createSellerPage.js*/
document.addEventListener('DOMContentLoaded', function () {
  const nicknameInput = document.getElementById('nickname');
  const checkBtn = document.getElementById('nicknameToggleBtn');
  const msg = document.getElementById('nicknameCheckMsg');

  if (nicknameInput && checkBtn && msg) {
    checkBtn.addEventListener('click', () => {
      const nickname = nicknameInput.value.trim();

      if (!nickname) {
        msg.textContent = '닉네임을 입력해주세요.';
        msg.className = 'form-text error';
        nicknameInput.focus();
        return;
      }

      fetch(`/mypage/seller/nickname-check?nickname=${encodeURIComponent(nickname)}`)
        .then(res => res.json())
        .then(data => {
          if (data.available) {
            msg.textContent = '사용 가능한 닉네임입니다.';
            msg.className = 'form-text success';
          } else {
            msg.textContent = '이미 사용 중인 닉네임입니다.';
            msg.className = 'form-text error';
            nicknameInput.focus();
          }
        })
        .catch(() => {
          msg.textContent = '중복 확인 중 오류가 발생했습니다.';
          msg.className = 'form-text error';
        });
    });
  }
});
