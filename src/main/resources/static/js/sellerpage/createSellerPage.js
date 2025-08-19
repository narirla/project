/* createSellerPage.js */
document.addEventListener('DOMContentLoaded', function () {
  const nicknameInput = document.getElementById('nickname');
  const checkBtn = document.getElementById('nicknameToggleBtn');
  const msg = document.getElementById('nicknameCheckMsg');
  const form = document.getElementById('sellerCreateForm');
  const introTextarea = document.getElementById('intro');
  const charCountNum = document.getElementById('charCountNum');
  const imageInput = document.getElementById('imageFile');
  const previewImg = document.getElementById('profilePreview');
  const removeBtn = document.getElementById('removeImageBtn');
  const placeholderText = document.getElementById('placeholderText');
  const dropzone = document.querySelector('.profile-preview-box');

  const maxLength = 150;
  let isNicknameAvailable = false;

  // ▼▼ 추가: 드롭 직후 클릭 억제 타이머(재오픈 방지)
  const DROP_SUPPRESS_MS = 500;           // 억제 지속 시간(ms) - 필요 시 300~800 조정
  let suppressClickUntil = 0;             // 이 시각 전까지 dropzone 클릭/키보드 오픈 무시
  // ▲▲ 추가 끝

  // ---------- 닉네임 입력 관련 ----------
  if (nicknameInput && msg) {
    nicknameInput.addEventListener('input', () => {
      isNicknameAvailable = false;
      msg.textContent = '';
      msg.classList.remove('success', 'error');
      nicknameInput.setCustomValidity('');
    });
  }

  // 닉네임 중복 확인
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
            nicknameInput.setCustomValidity('');
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

  // ---------- 제출 검증(공백 → 중복확인 순) ----------
  if (form && nicknameInput && msg) {
    form.addEventListener('submit', function (e) {
      const nickname = nicknameInput.value.trim();

      if (!nickname) {
        e.preventDefault();
        msg.textContent = '닉네임을 입력해주세요.';
        msg.classList.remove('success');
        msg.classList.add('form-text', 'error');
        nicknameInput.setCustomValidity('닉네임을 입력해주세요.');
        nicknameInput.reportValidity();
        nicknameInput.focus();
        return;
      } else {
        nicknameInput.setCustomValidity('');
      }

      if (!isNicknameAvailable) {
        e.preventDefault();
        msg.textContent = '닉네임 중복 확인을 먼저 해주세요.';
        msg.classList.remove('success');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
        return;
      }
    });
  }

  // ---------- 소개글 글자 수 ----------
  if (introTextarea && charCountNum) {
    introTextarea.addEventListener('input', function () {
      const currentLength = introTextarea.value.length;
      charCountNum.textContent = currentLength;
      charCountNum.style.color = currentLength > maxLength ? 'red' : '';
    });
  }

  // ---------- 이미지 업로드/삭제 ----------
  if (imageInput && previewImg && removeBtn && placeholderText) {

    // 공통 유효성/적용 함수
    function applyFile(file) {
      if (!file) return;

      const maxSize = 2 * 1024 * 1024; // 2MB
      const allowedExt = ['jpg', 'jpeg', 'png', 'gif']; // 서버 정책과 일치

      const name = (file.name || '').toLowerCase();
      const ext = name.includes('.') ? name.split('.').pop() : '';
      const isValidExt = allowedExt.includes(ext);
      const isValidSize = file.size <= maxSize;

      if (!isValidExt) { alert('jpg, jpeg, png, gif 형식만 허용됩니다.'); resetImage(); return; }
      if (!isValidSize) { alert('이미지 파일은 2MB 이하만 허용됩니다.'); resetImage(); return; }

      // input[type=file]에 파일 주입 (폼 제출 호환)
      const dt = new DataTransfer();
      dt.items.add(file);
      imageInput.files = dt.files;

      const reader = new FileReader();
      reader.onload = (e) => {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
        placeholderText.style.display = 'none';
        removeBtn.style.display = 'flex';
      };
      reader.readAsDataURL(file);
    }

    imageInput.addEventListener('change', function () {
      applyFile(imageInput.files?.[0]);
    });

    removeBtn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      resetImage();
    });

    function resetImage() {
      imageInput.value = '';
      previewImg.src = '';
      previewImg.style.display = 'none';
      placeholderText.style.display = 'block';
      removeBtn.style.display = 'none';
    }

    // === 드래그 앤 드롭 + 파일 선택 ===
    if (dropzone) {
      // 클릭/키보드로 파일 선택 (드롭 직후 억제 로직 추가)
      dropzone.addEventListener('click', (e) => {
        if (Date.now() < suppressClickUntil) {
          e.preventDefault();
          e.stopPropagation();
          return; // 드롭 직후 자동 클릭 방지
        }
        imageInput.click();
      });

      dropzone.addEventListener('keydown', (e) => {
        if (Date.now() < suppressClickUntil) {
          e.preventDefault();
          return; // 드롭 직후 키보드 오픈 방지
        }
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          imageInput.click();
        }
      });

      // 브라우저 기본 동작 방지
      ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(evt => {
        dropzone.addEventListener(evt, (e) => {
          e.preventDefault();
          e.stopPropagation();
        });
      });

      dropzone.addEventListener('dragenter', () => dropzone.classList.add('drag-over'));
      dropzone.addEventListener('dragover',  () => dropzone.classList.add('drag-over'));
      dropzone.addEventListener('dragleave', () => dropzone.classList.remove('drag-over'));
      dropzone.addEventListener('drop', (e) => {
        dropzone.classList.remove('drag-over');

        const file = e.dataTransfer?.files?.[0];
        if (!file) return;

        // ▼▼ 추가: 드롭 이후 일정 시간 클릭 억제(파일창 재오픈 방지)
        suppressClickUntil = Date.now() + DROP_SUPPRESS_MS;
        // ▲▲ 추가 끝

        applyFile(file);
      });
    }

    // (옵션) 클립보드 이미지 붙여넣기 지원
    document.addEventListener('paste', (e) => {
      const item = [...(e.clipboardData?.items || [])].find(i => i.type.startsWith('image/'));
      if (!item) return;
      const file = item.getAsFile();
      applyFile(file);
    });
  }
});
