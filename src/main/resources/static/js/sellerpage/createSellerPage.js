/* createSellerPage.js */
document.addEventListener('DOMContentLoaded', function () {
  // 요소 참조
  const nicknameInput = document.getElementById('nickname');
  const checkBtn = document.getElementById('nicknameToggleBtn');
  const msg = document.getElementById('nicknameCheckMsg');
  const form = document.getElementById('sellerCreateForm');
  const introTextarea = document.getElementById('intro');
  const charCountNum = document.getElementById('charCountNum');
  const maxLength = 500;

  let isNicknameAvailable = false;

  // ✅ 닉네임 입력 시 상태 초기화
  if (nicknameInput && msg) {
    nicknameInput.addEventListener('input', () => {
      isNicknameAvailable = false;
      msg.textContent = '';
      msg.classList.remove('success', 'error');
    });
  }

  // ✅ 닉네임 중복 확인 버튼 클릭
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
    });
  }

  // ✅ form 제출 시 닉네임 확인 여부 검사
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

  // ✅ 소개글 글자 수 실시간 표시
  if (introTextarea && charCountNum) {
    introTextarea.addEventListener('input', function () {
      const currentLength = introTextarea.value.length;
      charCountNum.textContent = currentLength;

      // 시각적 경고 (500자 초과 시 빨간색)
      if (currentLength > maxLength) {
        charCountNum.style.color = 'red';
      } else {
        charCountNum.style.color = '';
      }
    });
  }
});
